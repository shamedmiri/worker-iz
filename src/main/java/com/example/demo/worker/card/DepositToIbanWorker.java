package com.example.demo.worker.card;

import com.example.demo.config.ApiUrlsProperties;
import com.example.demo.error.ErrorMessagesProperties;
import com.example.demo.service.card.DepositToIbanService;
import jakarta.annotation.PostConstruct;
import org.camunda.bpm.client.ExternalTaskClient;
import org.camunda.spin.Spin;
import org.camunda.spin.json.SpinJsonNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;
@Component
public class DepositToIbanWorker {
    private static final String TOPIC_NAME = "DepositToIbanWorker";
    private static final String SUCCESS_CODE = "100";

    @Autowired
    private ErrorMessagesProperties errorMessages;

    private final ApiUrlsProperties properties;
    private final DepositToIbanService apiService;

    public DepositToIbanWorker(ApiUrlsProperties properties, DepositToIbanService apiService) {
        this.properties = properties;
        this.apiService = apiService;
    }

    @PostConstruct
    public void subscribe() {
        ExternalTaskClient client = ExternalTaskClient.create()
                .baseUrl(properties.getCamunda())
                .asyncResponseTimeout(20000)
                .build();

        client.subscribe(TOPIC_NAME)
                .lockDuration(30000)
                .handler((externalTask, externalTaskService) -> {
                    String depositNumber = externalTask.getVariable("depositNumber");
                    String[] parts = depositNumber.split("-");
                    String branchCode=parts[0];
                    String depositType=parts[1];
                    String customerNumber=parts[2];
                    String serialNumber=parts[3];

                    try {
                        Map<String, Object> responseMap = apiService.callUserApi(branchCode, depositType,customerNumber,serialNumber, depositNumber);
                        int statusCode = (int) responseMap.get("statusCode");

                        if (statusCode == 200 || statusCode == 201) {
                            handleSuccessResponse(externalTaskService, externalTask, responseMap);
                        } else {
                            externalTaskService.handleBpmnError(externalTask, "Error_END", errorMessages.get("ERROR_SERVICE"), responseMap);
                        }

                    } catch (Exception e) {
                        externalTaskService.handleFailure(externalTask, "API error", e.getMessage(), 0, 0);
                    }
                })
                .open();
    }

    private void handleSuccessResponse(org.camunda.bpm.client.task.ExternalTaskService externalTaskService,
                                       org.camunda.bpm.client.task.ExternalTask externalTask,
                                       Map<String, Object> responseMap) {

        SpinJsonNode jsonNode = Spin.JSON(responseMap.get("Output"));
        String responseCode = jsonNode.prop("ResponseCode").toString();

        if (SUCCESS_CODE.equals(responseCode)) {
            String  ibanNumber = jsonNode.prop("Deposit").prop("IBAN").stringValue();
            String  bankName = "ایران زمین";
            String  title = jsonNode.prop("Deposit").prop("Title").stringValue();
            String  depositNumber = jsonNode.prop("Deposit").prop("DepositNumber").stringValue();
            Map<String, Object> variables = Map.of(
                    "iBanId", ibanNumber,
                    "Name", title,
                    "BankName", bankName,
                    "AccountNumber", depositNumber
            );
            externalTaskService.complete(externalTask, variables);
        } else {
            Map<String, Object> variables = Map.of(
                    "errorMsg", jsonNode.prop("ResponseMessage").toString()
            );
            externalTaskService.handleBpmnError(externalTask, "Error_End", errorMessages.get("ERROR_SERVICE"), variables);
        }
    }
}



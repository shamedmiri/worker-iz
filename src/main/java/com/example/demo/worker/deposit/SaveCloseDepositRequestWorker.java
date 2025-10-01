package com.example.demo.worker.deposit;

import com.example.demo.config.ApiUrlsProperties;
import com.example.demo.error.ErrorMessagesProperties;
import com.example.demo.service.deposit.SaveCloseDepositRequestService;
import jakarta.annotation.PostConstruct;
import org.camunda.bpm.client.ExternalTaskClient;
import org.camunda.spin.Spin;
import org.camunda.spin.json.SpinJsonNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class SaveCloseDepositRequestWorker {
    private static final String TOPIC_NAME = "SaveCloseDepositRequestWorker";
    private static final String SUCCESS_CODE = "100";

    @Autowired
    private ErrorMessagesProperties errorMessages;

    private final ApiUrlsProperties properties;
    private final SaveCloseDepositRequestService apiService;

    public SaveCloseDepositRequestWorker(ApiUrlsProperties properties, SaveCloseDepositRequestService apiService) {
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
                    String CustomerNumber = externalTask.getVariable("");
                    String DepositNumber = externalTask.getVariable("");
                    String FullName = externalTask.getVariable("");
                    String Mobile = externalTask.getVariable("");
                    String ApplicationKey = externalTask.getVariable("");
                    String DestinationDeposit = externalTask.getVariable("");
                    String NationalCode = externalTask.getVariable("");
                    String NationalCardSerial = externalTask.getVariable("");
                    String InstanceId = externalTask.getVariable("");
                    String DefinitionId = externalTask.getVariable("");
                    String StatusCode = externalTask.getVariable("");
                    String StatusDescription = externalTask.getVariable("");
                    String RequestTypeId = externalTask.getVariable("");


                    try {
                        Map<String, Object> responseMap = apiService.callUserApi(CustomerNumber, DepositNumber, FullName, Mobile, ApplicationKey, DestinationDeposit, NationalCode, NationalCardSerial, InstanceId, DefinitionId, StatusCode, StatusDescription, RequestTypeId);
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
            Map<String, Object> variables = Map.of(
                    "resultMessage", "درخواست با موفقیت ثبت گردبد"
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



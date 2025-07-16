package com.example.demo.worker.cheque.transfer;

import com.example.demo.config.ApiUrlsProperties;
import com.example.demo.error.ErrorMessagesProperties;
import com.example.demo.service.cheque.transfer.TransferChequeService;
import com.example.demo.service.cheque.transfer.TransferChequeUpdateStatusService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.camunda.bpm.client.ExternalTaskClient;
import org.camunda.spin.Spin;
import org.camunda.spin.json.SpinJsonNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;


@Component
public class TransferChequeUpdateStatusWorker {

    private static final String TOPIC_NAME = "TransferChequeUpdateStatusWorker";
    private static final String SUCCESS_CODE = "100";

    @Autowired
    private ErrorMessagesProperties errorMessages;

    private final ApiUrlsProperties properties;
    private final TransferChequeUpdateStatusService apiService;

    public TransferChequeUpdateStatusWorker(ApiUrlsProperties properties, TransferChequeUpdateStatusService apiService) {
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
                    String receivers = externalTask.getVariable("ReceiverTable");
                    ObjectMapper objectMapper = new ObjectMapper();
                    String ReceiverNationalCode="";
                    try {
                        List<Map<String, Object>> receiversList = objectMapper.readValue(
                                receivers,
                                new TypeReference<List<Map<String, Object>>>() {}
                        );
                        for (Map<String, Object> receiver : receiversList) {
                            ReceiverNationalCode = receiver.get("Identifier").toString()+",";
                            // استفاده از fullName و ...
                        }
                    }catch (Exception e) {
                        e.printStackTrace();
                    }



                    String sayadId = externalTask.getVariable("SayadId");
                    String serialNumber = externalTask.getVariable("SerialNumber");
                    String userIdentification = externalTask.getVariable("Identifier");
                    String amount = externalTask.getVariable("Amount");
                    String dueDate = externalTask.getVariable("DueDate");
                    String description = externalTask.getVariable("Description");
                    String issuerIbanNumber = externalTask.getVariable("IssuerIbanNumber");
                    String receiverNationalCode = ReceiverNationalCode;
                    String receiverName = externalTask.getVariable("ReceiverName");
                    if(receiverName==null)
                        receiverName="";

                    try {
                        Map<String, Object> responseMap = apiService.callUserApi(sayadId, serialNumber, userIdentification, amount, dueDate,
                                description, issuerIbanNumber, receiverNationalCode, receiverName);
                        int statusCode = (int) responseMap.get("statusCode");

                        if (statusCode == 200 || statusCode == 201) {
                            handleSuccessResponse(externalTaskService, externalTask, responseMap);
                        } else {
                            externalTaskService.handleBpmnError(externalTask, "Error_END", "خطای سرویس", responseMap);
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
                    "outputServiceTransfer", jsonNode
            );
            externalTaskService.complete(externalTask, variables);
        } else {
            Map<String, Object> variables = Map.of(
                    "errorMsg", jsonNode.prop("ResponseMessage").toString()
            );
            externalTaskService.handleBpmnError(externalTask, "Error_Return", "خطای سرویس", variables);
        }
    }
}

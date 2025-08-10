package com.example.demo.worker.cheque.issue;

import com.example.demo.config.ApiUrlsProperties;
import com.example.demo.error.ErrorMessagesProperties;
import com.example.demo.service.cheque.issue.CaSignService;
import com.example.demo.service.cheque.issue.SaveIssueRequestService;
import jakarta.annotation.PostConstruct;
import org.camunda.bpm.client.ExternalTaskClient;
import org.camunda.spin.Spin;
import org.camunda.spin.json.SpinJsonNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static org.camunda.spin.Spin.JSON;

@Component
public class SaveIssueRequestWorker {
    @Autowired
    private ErrorMessagesProperties errorMessages;
    private final ApiUrlsProperties properties;
    private final SaveIssueRequestService apiService;

    public SaveIssueRequestWorker(ApiUrlsProperties properties, SaveIssueRequestService apiService) {
        this.properties = properties;
        this.apiService = apiService;
    }

    @PostConstruct
    public void subscribe() {
        ExternalTaskClient client = ExternalTaskClient.create()
                .baseUrl(properties.getCamunda())
                .asyncResponseTimeout(20000)
                .build();
        client.subscribe("SaveIssueRequestWorker") // تاپیک جدید در BPMN
                .lockDuration(30000)
                .handler((externalTask, externalTaskService) -> {
                    String fullName = externalTask.getVariable("fullName");
                    String MSSP_TransID = externalTask.getVariable("MSSP_TransID");
                    ArrayList<String> receivers = externalTask.getVariable("ReceiverTableList");
                    String identifier = externalTask.getVariable("nationalCode");
                    String shahabId = externalTask.getVariable("shahabId");
                    String dueDate = externalTask.getVariable("dueDate");
                    String amount = externalTask.getVariable("Amount");
                    String sayadId = externalTask.getVariable("sayadId");
                    String fromIban = externalTask.getVariable("toIban");
                    String saveDescription = externalTask.getVariable("saveDescription");
                    String reasonCode = externalTask.getVariable("reason");
                    String customerNumber = externalTask.getVariable("customerNumber");
                    String serial = externalTask.getVariable("serial");
                    String seriesNo = externalTask.getVariable("seriesNo");

                    try {
                        Map<String, Object> variables = apiService.callUserApi(fullName, receivers, identifier, shahabId, dueDate, sayadId, serial, seriesNo
                                , fromIban, amount, saveDescription, reasonCode, customerNumber, MSSP_TransID);
                        int statusCode = (int) variables.get("statusCode");
                        if (statusCode == 200 || statusCode == 201) {
                            Object outputRaw = Spin.JSON(variables.get("Output"));
                            if (outputRaw == null) {
                                throw new RuntimeException("متغیر 'Output' نال است.");
                            }

                            SpinJsonNode outputJson = JSON(outputRaw.toString());
                            int statusCode1 = outputJson.prop("Status").prop("StatusCode").prop("Value").numberValue().intValue();
                            if (statusCode1 == 100) {
                                Map<String, Object> result = new HashMap<>();
                                result.put("MSSP_TransID", outputJson.prop("MSSP_TransID").toString());
                                result.put("CaStatus", outputJson.prop("Status").prop("StatusDetail").toString());
                                externalTaskService.complete(externalTask, result);
                            } else {
                                Map<String, Object> result = new HashMap<>();
                                result.put("errorMSG", outputJson.prop("Status").prop("StatusDetail").toString());
                                externalTaskService.handleBpmnError(externalTask, "Error_End", "خطای سرویس", result);
                            }
                        } else {
                            Map<String, Object> result = new HashMap<>();
                            result.put("errorMsg", errorMessages.get("SERVICE_IS_UNAVAILABLE"));
                            externalTaskService.handleBpmnError(externalTask, "Error_End", "خطای سرویس", result);
                        }
                    } catch (Exception e) {
                        externalTaskService.handleFailure(externalTask, "API error", e.getMessage(), 0, 0);
                    }
                })
                .open();
    }
}

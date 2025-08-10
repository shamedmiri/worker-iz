package com.example.demo.worker.cheque.issue;

import com.example.demo.config.ApiUrlsProperties;
import com.example.demo.error.ErrorMessagesProperties;
import com.example.demo.service.cheque.issue.CaSignService;
import com.example.demo.service.cheque.issue.GetChequeInquiryService;
import jakarta.annotation.PostConstruct;
import org.camunda.bpm.client.ExternalTaskClient;
import org.camunda.spin.Spin;
import org.camunda.spin.SpinList;
import org.camunda.spin.json.SpinJsonNode;
import org.json.JSONArray;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

import static org.camunda.spin.Spin.JSON;

@Component
public class CaSignWorker {
    @Autowired
    private ErrorMessagesProperties errorMessages;
    private final ApiUrlsProperties properties;
    private final CaSignService apiService;

    public CaSignWorker(ApiUrlsProperties properties, CaSignService apiService) {
        this.properties = properties;
        this.apiService = apiService;
    }

    @PostConstruct
    public void subscribe() {
        ExternalTaskClient client = ExternalTaskClient.create()
                .baseUrl(properties.getCamunda())
                .asyncResponseTimeout(20000)
                .build();
        client.subscribe("CaSignWorker") // تاپیک جدید در BPMN
                .lockDuration(30000)
                .handler((externalTask, externalTaskService) -> {
                    String sayadId = externalTask.getVariable("sayadId");
                    String nationalCode = externalTask.getVariable("nationalCode");
                    ArrayList<String> receivers = externalTask.getVariable("ReceiverTableList");
                    String DueDate2 = externalTask.getVariable("dueDate");
                    String shahabId = externalTask.getVariable("shahabId");
                    String saveDescription = externalTask.getVariable("saveDescription");
                    String amount = externalTask.getVariable("Amount");

                    try {
                        Map<String, Object> variables = apiService.callUserApi(nationalCode, receivers, sayadId, DueDate2,
                                amount, saveDescription, shahabId);
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
                            result.put("errorMSG", errorMessages.get("SERVICE_IS_UNAVAILABLE"));
                            externalTaskService.handleBpmnError(externalTask, "Error_End", "خطای سرویس", result);
                        }
                    } catch (Exception e) {
                        externalTaskService.handleFailure(externalTask, "API error", e.getMessage(), 0, 0);
                    }
                })
                .open();
    }
}

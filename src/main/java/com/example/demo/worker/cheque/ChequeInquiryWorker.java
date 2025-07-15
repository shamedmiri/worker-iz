package com.example.demo.worker.cheque;

import com.example.demo.config.ApiUrlsProperties;
import com.example.demo.error.ErrorMessagesProperties;
import com.example.demo.service.cheque.ChequeHolderInquiryService;
import com.example.demo.service.cheque.ChequeIssuerInquiryService;
import org.camunda.bpm.client.ExternalTaskClient;
import org.camunda.spin.Spin;
import org.camunda.spin.json.SpinJsonNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import jakarta.annotation.PostConstruct;

import java.util.HashMap;
import java.util.Map;

@Component
public class ChequeInquiryWorker {
    @Autowired
    private ErrorMessagesProperties errorMessages;
    private final ApiUrlsProperties properties;
    private final ChequeHolderInquiryService apiServiceHolder;
    private final ChequeIssuerInquiryService apiServiceIssuesr;

    public ChequeInquiryWorker(ApiUrlsProperties properties, ChequeHolderInquiryService apiServiceHolder, ChequeIssuerInquiryService apiServiceIssuesr) {
        this.properties = properties;
        this.apiServiceHolder = apiServiceHolder;
        this.apiServiceIssuesr = apiServiceIssuesr;
    }

    @PostConstruct
    public void subscribe() {
        ExternalTaskClient client = ExternalTaskClient.create()
                .baseUrl(properties.getCamunda())
                .asyncResponseTimeout(20000)
                .build();
        String chequeInquiryWorker = "chequeInquiryWorker";
        client.subscribe(chequeInquiryWorker) // تاپیک جدید در BPMN
                .lockDuration(30000)
                .handler((externalTask, externalTaskService) -> {
                    String sayadId = externalTask.getVariable("sayadId");
                    String idCode = externalTask.getVariable("Identifier");
                    String shahabId = externalTask.getVariable("shahabId");
                    try {
                        Map<String, Object> variables = apiServiceHolder.callUserApi(sayadId, idCode);
                        int statusCode = (int) variables.get("statusCode");
                        if (statusCode == 200 || statusCode == 201) {
                            SpinJsonNode jsonNode = Spin.JSON((variables.get("Output")));
                            String responseCode = jsonNode.prop("ResponseCode").toString();
                            if (responseCode.equals("100")) {
                                Map<String, Object> result = new HashMap<>();
                                result.put("HoldertList", jsonNode.prop("Holders").toString());
                                result.put("SayadId", jsonNode.prop("SayadId").toString());
                                result.put("SerialNumber", jsonNode.prop("SerialNumber").toString());
                                result.put("SeriesNumber", jsonNode.prop("SeriesNumber").toString());
                                result.put("Amount", jsonNode.prop("Amount").toString());
                                result.put("DueDate", jsonNode.prop("DueDate").toString());
                                result.put("Description", jsonNode.prop("Description").toString());
                                result.put("BlockStatus", jsonNode.prop("BlockStatus").toString());
                                result.put("ChequeStatus", jsonNode.prop("ChequeStatus").toString());
                                externalTaskService.complete(externalTask, result);
                            } else if (responseCode.equals("104")) {
                                Map<String, Object> variablesIssuer = apiServiceIssuesr.callUserApi(sayadId, idCode, shahabId);
                                int statusCodeIssuer = (int) variablesIssuer.get("statusCode");
                                if (statusCodeIssuer == 200 || statusCodeIssuer == 201) {
                                    SpinJsonNode jsonNodeIssuer = Spin.JSON((variablesIssuer.get("Output")));
                                    String responseCodeIssuer = jsonNodeIssuer.prop("ResponseCode").toString();
                                    if (responseCodeIssuer.equals("100")) {
                                        Map<String, Object> result = new HashMap<>();
                                        result.put("ReceiverList", jsonNodeIssuer.prop("Receivers").toString());
                                        result.put("SayadId", jsonNodeIssuer.prop("SayadId").toString());
                                        result.put("SerialNumber", jsonNodeIssuer.prop("SerialNumber").toString());
                                        result.put("SeriesNumber", jsonNodeIssuer.prop("SeriesNumber").toString());
                                        result.put("Amount", jsonNodeIssuer.prop("Amount").toString());
                                        result.put("DueDate", jsonNodeIssuer.prop("DueDate").toString());
                                        result.put("Description", jsonNodeIssuer.prop("Description").toString());
                                        result.put("BlockStatus", jsonNodeIssuer.prop("BlockStatus").toString());
                                        result.put("ChequeStatus", jsonNodeIssuer.prop("ChequeStatus").toString());
                                        externalTaskService.complete(externalTask, result);
                                    } else {
                                        Map<String, Object> result = new HashMap<>();
                                        result.put("errorMsg", jsonNode.prop("ResponseMessage").toString());
                                        externalTaskService.handleBpmnError(externalTask, "Error_Return", "خطای سرویس", result);

                                    }
                                } else {
                                    Map<String, Object> result = new HashMap<>();
                                    result.put("errorMSG", errorMessages.get("SERVICE_IS_UNAVAILABLE"));
                                    externalTaskService.handleBpmnError(externalTask, "Error_End", "خطای سرویس", result);

                                }
                            } else {
                                Map<String, Object> result = new HashMap<>();
                                result.put("errorMsg", jsonNode.prop("ResponseMessage").toString());
                                externalTaskService.handleBpmnError(externalTask, "Error_Return", "خطای سرویس", result);
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

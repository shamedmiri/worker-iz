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
public class ChequeHolderInquiryWorker {
    @Autowired
    private ErrorMessagesProperties errorMessages;
    private final ApiUrlsProperties properties;
    private final ChequeHolderInquiryService apiServiceHolder;

    public ChequeHolderInquiryWorker(ApiUrlsProperties properties, ChequeHolderInquiryService apiServiceHolder) {
        this.properties = properties;
        this.apiServiceHolder = apiServiceHolder;
    }

    @PostConstruct
    public void subscribe() {
        ExternalTaskClient client = ExternalTaskClient.create()
                .baseUrl(properties.getCamunda())
                .asyncResponseTimeout(20000)
                .build();
        String chequeInquiryTransferWorker = "chequeInquiryTransferWorker";
        client.subscribe(chequeInquiryTransferWorker) // تاپیک جدید در BPMN
                .lockDuration(30000)
                .handler((externalTask, externalTaskService) -> {
                    String sayadId = externalTask.getVariable("sayadId");
                    String idCode = externalTask.getVariable("Identifier");

                    try {
                        Map<String, Object> variables = apiServiceHolder.callUserApi(sayadId, idCode);
                        int statusCode = (int) variables.get("statusCode");
                        if (statusCode == 200 || statusCode == 201) {
                            SpinJsonNode jsonNode = Spin.JSON((variables.get("Output")));
                            String responseCode = jsonNode.prop("ResponseCode").toString();
                            if (responseCode.equals("100")) {
                                SpinJsonNode holdersArray = jsonNode.prop("Holders");
                                String fullName = holdersArray.elements().get(0).prop("FullName").stringValue();
                                String Identifier = holdersArray.elements().get(1).prop("Identifier").stringValue();

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
                                result.put("ReceiverIdentifier", Identifier);
                                result.put("CurrentDescription", jsonNode.prop("ChequeStatus").toString());
                                result.put("ReceiverName", fullName);
                                result.put("HolderList", jsonNode.prop("ChequeStatus").toString());
                                result.put("Reason", jsonNode.prop("ChequeStatus").toString());
                                result.put("ToIban", jsonNode.prop("ChequeStatus").toString());


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
                    } catch (Exception e) {
                        externalTaskService.handleFailure(externalTask, "API error", e.getMessage(), 0, 0);
                    }
                })
                .open();
    }
}

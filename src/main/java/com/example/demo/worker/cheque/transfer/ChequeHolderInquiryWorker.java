package com.example.demo.worker.cheque.transfer;
import com.example.demo.config.ApiUrlsProperties;
import com.example.demo.error.ErrorMessagesProperties;
import com.example.demo.service.cheque.transfer.ChequeHolderInquiryService;
import com.example.demo.service.customer.GetCustomerInfoService;
import jakarta.annotation.PostConstruct;
import org.camunda.bpm.client.ExternalTaskClient;
import org.camunda.bpm.client.task.ExternalTaskService;
import org.camunda.spin.Spin;
import org.camunda.spin.json.SpinJsonNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class ChequeHolderInquiryWorker {

    private final ErrorMessagesProperties errorMessages;
    private final ApiUrlsProperties properties;
    private final ChequeHolderInquiryService apiServiceHolder;
    private final GetCustomerInfoService getCustomerInfoService;

    @Autowired
    public ChequeHolderInquiryWorker(ApiUrlsProperties properties,
                                     ChequeHolderInquiryService apiServiceHolder,
                                     GetCustomerInfoService getCustomerInfoService,
                                     ErrorMessagesProperties errorMessages) {
        this.properties = properties;
        this.apiServiceHolder = apiServiceHolder;
        this.getCustomerInfoService = getCustomerInfoService;
        this.errorMessages = errorMessages;
    }

    @PostConstruct
    public void subscribe() {
        ExternalTaskClient client = ExternalTaskClient.create()
                .baseUrl(properties.getCamunda())
                .asyncResponseTimeout(20000)
                .build();

        client.subscribe("chequeInquiryTransferWorker")
                .lockDuration(30000)
                .handler((externalTask, externalTaskService) -> {
                    String sayadId = externalTask.getVariable("sayadId");
                    String shahabId = externalTask.getVariable("shahabId");
                    String idCode = externalTask.getVariable("Identifier");
                    String customerNumber = externalTask.getVariable("customerNumber");

                    try {
                        Map<String, Object> chequeResponse = apiServiceHolder.callUserApi(sayadId, shahabId,idCode);
                        int chequeStatus = (int) chequeResponse.get("statusCode");

                        if (chequeStatus != 200 && chequeStatus != 201) {
                            handleServiceError(externalTaskService, externalTask, "SERVICE_IS_UNAVAILABLE");
                            return;
                        }

                        SpinJsonNode chequeJson = Spin.JSON(chequeResponse.get("Output"));
                        String responseCode = chequeJson.prop("ResponseCode").toString();

                        if (!"100".equals(responseCode)) {
                            handleBusinessError(externalTaskService, externalTask, chequeJson.prop("ResponseMessage").stringValue());
                            return;
                        }

                        Map<String, Object> result = buildChequeResult(chequeJson);

                        Map<String, Object> customerResponse = getCustomerInfoService.callUserApi(customerNumber);
                        int customerStatus = (int) customerResponse.get("statusCode");

                        if (customerStatus == 200 || customerStatus == 201) {
                            SpinJsonNode customerJson = Spin.JSON(customerResponse.get("Output"));
                            String Identifier= customerJson.prop("Customer").prop("NationalId").stringValue();
                             shahabId= customerJson.prop("Customer").prop("ShahabCode").stringValue();
                            String fullName= customerJson.prop("Customer").prop("FirstName").stringValue()+" "+
                                    customerJson.prop("Customer").prop("LastName").stringValue();


                            List<Map<String, Object>> signerList = new ArrayList<>();
                            Map<String, Object> signersInerMap = new HashMap<>();
                            signersInerMap.put("FullName",fullName );
                            signersInerMap.put("ShahabId",shahabId );
                            signersInerMap.put("IdentifierType", 1);
                            signersInerMap.put("Identifier", Identifier);
                            Map<String, Object> signersOutMap = new HashMap<>();
                            signersOutMap.put("LegalStamp", "0");
                            signersOutMap.put("Signer", signersInerMap);
                            signersOutMap.put("SignGrantor", null);
                            signerList.add(signersOutMap);

                            result.put("Identifier", Identifier);
                            result.put("fullName", fullName);
                            result.put("shahabId", shahabId);
                            result.put("signerList", signerList);
                            externalTaskService.complete(externalTask, result);
                        } else {
                            handleServiceError(externalTaskService, externalTask, "SERVICE_IS_UNAVAILABLE", result);
                        }

                    } catch (Exception e) {
                        externalTaskService.handleFailure(externalTask, "Unhandled Error", e.getMessage(), 0, 0);
                    }
                })
                .open();
    }

    private Map<String, Object> buildChequeResult(SpinJsonNode json) {
        Map<String, Object> result = new HashMap<>();
        result.put("HolderList", json.prop("Holders").toString());
        result.put("SayadId", json.prop("SayadId").stringValue());
        result.put("SerialNumber", json.prop("SerialNumber").stringValue());
        result.put("SeriesNumber", json.prop("SeriesNumber").stringValue());
        result.put("Amount", json.prop("Amount").stringValue());
        result.put("DueDate", json.prop("DueDate").stringValue());
        result.put("Description", json.prop("Description").stringValue());
        result.put("BlockStatus", json.prop("BlockStatus").stringValue());
        result.put("ChequeStatus", json.prop("ChequeStatus").stringValue());
        result.put("CurrentDescription", json.prop("ChequeStatus").stringValue()); // duplicate?
        result.put("Signers", json.prop("Signers").toString());
        result.put("Reason", json.prop("Reason").stringValue());
        return result;
    }

    private void handleServiceError(ExternalTaskService service, org.camunda.bpm.client.task.ExternalTask task, String messageKey) {
        handleServiceError(service, task, messageKey, new HashMap<>());
    }

    private void handleServiceError(ExternalTaskService service, org.camunda.bpm.client.task.ExternalTask task, String messageKey, Map<String, Object> context) {
        context.put("errorMSG", errorMessages.get(messageKey));
        service.handleBpmnError(task, "Error_End", "خطای سرویس", context);
    }

    private void handleBusinessError(ExternalTaskService service, org.camunda.bpm.client.task.ExternalTask task, String errorMessage) {
        Map<String, Object> context = new HashMap<>();
        context.put("errorMsg", errorMessage);
        service.handleBpmnError(task, "Error_Return", "خطای سرویس", context);
    }
}

package com.example.demo.worker.cheque.issue;

import com.example.demo.config.ApiUrlsProperties;
import com.example.demo.error.ErrorMessagesProperties;
import com.example.demo.service.cheque.issue.GetChequeInquiryService;
import com.example.demo.service.customer.GetCustomerInfoByNationalCodeService;
import jakarta.annotation.PostConstruct;
import org.camunda.bpm.client.ExternalTaskClient;
import org.camunda.spin.Spin;
import org.camunda.spin.SpinList;
import org.camunda.spin.json.SpinJsonNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.camunda.spin.Spin.JSON;

@Component
public class GetChequeInquiryWorker {
    @Autowired
    private ErrorMessagesProperties errorMessages;
    private final ApiUrlsProperties properties;
    private final GetChequeInquiryService apiService;

    public GetChequeInquiryWorker(ApiUrlsProperties properties, GetChequeInquiryService apiService) {
        this.properties = properties;
        this.apiService = apiService;
    }

    @PostConstruct
    public void subscribe() {
        ExternalTaskClient client = ExternalTaskClient.create()
                .baseUrl(properties.getCamunda())
                .asyncResponseTimeout(20000)
                .build();
        client.subscribe("GetChequeInquiryWorker") // تاپیک جدید در BPMN
                .lockDuration(30000)
                .handler((externalTask, externalTaskService) -> {
                    String SayadId = externalTask.getVariable("chequeSerial");
                    try {
                        Map<String, Object> variables = apiService.callUserApi(SayadId);
                        int statusCode = (int) variables.get("statusCode");
                        if (statusCode == 200 || statusCode == 201) {
                            SpinJsonNode jsonNode = Spin.JSON(variables.get("Output"));
                            Map<String, Object> result = new HashMap<>();
                            result.put("serial", jsonNode.prop("SeriesNo").stringValue());
                            result.put("saveDescription", "fhdfhdfjhfdhd");
                            result.put("dueDate", jsonNode.prop("IssuanceDate").stringValue());
                            Map<String, Object> obj = new HashMap<>();
                            List<Map<String, Object>> simplifiedObjects = new ArrayList<>();
                            SpinList<SpinJsonNode> xx = jsonNode.prop("AccountOwnerInquiryStatus").elements();
                            List<Map<String, Object>> Receivers = new ArrayList<>();
                            Map<String, Object> objReceivers = new HashMap<>();
                            for (int i = 0; i < xx.size(); i++) {
                                objReceivers.put("Identifier", xx.get(i).prop("Identifier").stringValue());
                                objReceivers.put("FullName", xx.get(i).prop("Name").stringValue());
                            }
                            Receivers.add(objReceivers);
                            obj.put("ReceiversList", Receivers);
                            obj.put("select", false);
                            simplifiedObjects.add(obj);
                            SpinJsonNode jsonArrayNode = JSON(simplifiedObjects);
                            result.put("ReceiverTable", jsonArrayNode.toString());


                            result.put("sayadId", SayadId);
                            result.put("reason", jsonNode.prop("SeriesNo").stringValue());
                            result.put("toIban", jsonNode.prop("IBAN").stringValue());
                            result.put("seriesNo", jsonNode.prop("SeriesNo").stringValue());
                            externalTaskService.complete(externalTask, result);
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

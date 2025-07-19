package com.example.demo.worker.cheque.kartabl;

import com.example.demo.config.ApiUrlsProperties;
import com.example.demo.error.ErrorMessagesProperties;
import com.example.demo.service.cheque.kartabl.IssuedChequeService;
import com.example.demo.service.cheque.kartabl.ReceiveredChequeKartablService;
import jakarta.annotation.PostConstruct;
import org.camunda.bpm.client.ExternalTaskClient;
import org.camunda.spin.Spin;
import org.camunda.spin.json.SpinJsonNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class ReceivedChequeKartablWorker {
    @Autowired
    private ErrorMessagesProperties errorMessages;
    private final ApiUrlsProperties properties;
    private final ReceiveredChequeKartablService apiService;

    public ReceivedChequeKartablWorker(ApiUrlsProperties properties, ReceiveredChequeKartablService apiService) {
        this.properties = properties;
        this.apiService = apiService;
    }

    @PostConstruct
    public void subscribe() {
        ExternalTaskClient client = ExternalTaskClient.create()
                .baseUrl(properties.getCamunda())
                .asyncResponseTimeout(20000)
                .build();
        String receivedChequeKartablWorker = "ReceivedChequeKartablWorker";
        client.subscribe(receivedChequeKartablWorker) // تاپیک جدید در BPMN
                .lockDuration(30000)
                .handler((externalTask, externalTaskService) -> {
                    String customerNumber = externalTask.getVariable("customerNumber");
                    String identifier = externalTask.getVariable("Identifier");
                    String shahabId = externalTask.getVariable("shahabId");
                    try {
                        Map<String, Object> variables = apiService.callUserApi(shahabId, identifier,customerNumber);
                        int statusCode = (int) variables.get("statusCode");
                        if (statusCode == 200 || statusCode == 201) {
                            SpinJsonNode jsonNode = Spin.JSON((variables.get("Output")));
                            String responseCode = jsonNode.prop("ResponseCode").toString();
                            if (responseCode.equals("100")) {
                                Map<String, Object> result = new HashMap<>();
                                result.put("outputReceivedChequeKartabl", jsonNode.prop("Response").toString());
                                externalTaskService.complete(externalTask, result);
                            }else{
                                Map<String, Object> result = new HashMap<>();
                                result.put("errorMsg", jsonNode.prop("ResponseMessage").toString());
                                externalTaskService.handleBpmnError(externalTask, "Error_Return", "خطای سرویس", result);
                            }
                            } else {
                                externalTaskService.handleBpmnError(externalTask, "Error_END", "خطای سرویس", variables);
                            }
                        } catch(Exception e){
                            externalTaskService.handleFailure(externalTask, "API error", e.getMessage(), 0, 0);
                        }
                    })
                .open();
                }
    }

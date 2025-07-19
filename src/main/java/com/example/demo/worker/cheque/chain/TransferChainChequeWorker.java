package com.example.demo.worker.cheque.chain;

import com.example.demo.config.ApiUrlsProperties;
import com.example.demo.error.ErrorMessagesProperties;
import com.example.demo.service.cheque.chain.TransferChequeChainService;
import org.camunda.bpm.client.ExternalTaskClient;
import org.camunda.spin.Spin;
import org.camunda.spin.json.SpinJsonNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import jakarta.annotation.PostConstruct;

import java.util.HashMap;
import java.util.Map;

@Component
public class TransferChainChequeWorker {
    @Autowired
    private ErrorMessagesProperties errorMessages;
    private final ApiUrlsProperties properties;
    private final TransferChequeChainService apiService;

    public TransferChainChequeWorker(ApiUrlsProperties properties, TransferChequeChainService apiService) {
        this.properties = properties;
        this.apiService = apiService;
    }

    @PostConstruct
    public void subscribe() {
        ExternalTaskClient client = ExternalTaskClient.create()
                .baseUrl(properties.getCamunda())
                .asyncResponseTimeout(20000)
                .build();
        String transferChainWorker = "transferChainWorker";
        client.subscribe(transferChainWorker) // تاپیک جدید در BPMN
                .lockDuration(30000)
                .handler((externalTask, externalTaskService) -> {
                    String sayadId = externalTask.getVariable("sayadId");
                    String idCode = externalTask.getVariable("idCode");

                    try {
                        Map<String, Object> variables = apiService.callUserApi(sayadId, idCode);
                        int statusCode = (int) variables.get("statusCode");
                        if (statusCode == 200 || statusCode == 201) {
                            SpinJsonNode jsonNode = Spin.JSON((variables.get("Output")));
                            String responseCode = jsonNode.prop("ResponseCode").toString();
                            if (responseCode.equals("100")) {
                                Map<String, Object> result = new HashMap<>();
                                result.put("outputServiceChain", jsonNode.prop("chain").toString());
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

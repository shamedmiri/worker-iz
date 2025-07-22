package com.example.demo.worker.cheque.kartabl;

import com.example.demo.config.ApiUrlsProperties;
import com.example.demo.error.ErrorMessagesProperties;
import com.example.demo.service.cheque.kartabl.IssuedChequeService;
import jakarta.annotation.PostConstruct;
import org.camunda.bpm.client.ExternalTaskClient;
import org.camunda.spin.json.SpinJsonNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.camunda.spin.Spin.JSON;

@Component
public class SelectChequeKartablWorker {

    @Autowired
    private ErrorMessagesProperties errorMessages;

    private final ApiUrlsProperties properties;


    public SelectChequeKartablWorker(ApiUrlsProperties properties, IssuedChequeService apiService) {
        this.properties = properties;
    }

    @PostConstruct
    public void subscribe() {
        ExternalTaskClient client = ExternalTaskClient.create()
                .baseUrl(properties.getCamunda())
                .asyncResponseTimeout(20000)
                .build();

        client.subscribe("SelectChequeKartablWorker")
                .lockDuration(30000)
                .handler((externalTask, externalTaskService) -> {
                    boolean receiverIsEmpty=false;
                    boolean senderIsEmpty=false;
                    String ChequeType = externalTask.getVariable("ChequeType");
                    if (ChequeType.equals("Receiver")) {
                        String jsonInput  = externalTask.getVariable("filteredChequeInfoReceive").toString();
                        SpinJsonNode arrayNode = JSON(jsonInput);
                        int size = arrayNode.elements().size();
                        if(size>0) {
                            for (SpinJsonNode item : arrayNode.elements()) {
                                SpinJsonNode simplified = JSON("{ }");
                                boolean seletcItem = item.prop("select").boolValue();
                                if (seletcItem) {
                                    Map<String, Object> result = new HashMap<>();
                                    result.put("SayadId", item.prop("sayadId").toString());
                                    result.put("serialNo", item.prop("serialNo").toString());
                                    externalTaskService.complete(externalTask, result);
                                }

                            }
                        }else{
                            receiverIsEmpty=true;
                        }
                    } else {
                        String jsonInput = externalTask.getVariable("filteredChequeInfo").toString();
                        SpinJsonNode arrayNode = JSON(jsonInput);
                        int size = arrayNode.elements().size();
                        if(size>0){
                            for (SpinJsonNode item : arrayNode.elements()) {
                                SpinJsonNode simplified = JSON("{ }");
                                boolean seletcItem= item.prop("select").boolValue();
                                if (seletcItem) {
                                    Map<String, Object> result = new HashMap<>();
                                    result.put("SayadId", item.prop("SayadId").toString());
                                    result.put("SerialNumber", item.prop("SerialNumber").toString());
                                    externalTaskService.complete(externalTask, result);
                                }
                            }
                        }else{
                            senderIsEmpty=true;
                        }

                    }
                    if (senderIsEmpty||receiverIsEmpty) {
                        Map<String, Object> result = new HashMap<>();
                        result.put("ErrorMsg", "شما هیچ گونه درخواستی ندارید");
                        externalTaskService.handleBpmnError(externalTask, "Error_End", "خطای سرویس",result );
                    }
                })
                .open();
    }
}

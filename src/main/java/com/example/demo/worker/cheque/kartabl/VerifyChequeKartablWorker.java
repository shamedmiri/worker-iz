package com.example.demo.worker.cheque.kartabl;

import com.example.demo.config.ApiUrlsProperties;
import com.example.demo.error.ErrorMessagesProperties;
import com.example.demo.service.cheque.kartabl.IssuedChequeService;
import jakarta.annotation.PostConstruct;
import org.camunda.bpm.client.ExternalTaskClient;
import org.camunda.spin.json.SpinJsonNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

import static org.camunda.spin.Spin.JSON;

@Component
public class VerifyChequeKartablWorker {

    @Autowired
    private ErrorMessagesProperties errorMessages;

    private final ApiUrlsProperties properties;


    public VerifyChequeKartablWorker(ApiUrlsProperties properties, IssuedChequeService apiService) {
        this.properties = properties;
    }

    @PostConstruct
    public void subscribe() {
        ExternalTaskClient client = ExternalTaskClient.create()
                .baseUrl(properties.getCamunda())
                .asyncResponseTimeout(20000)
                .build();

        client.subscribe("VerifyChequeKartablWorker")
                .lockDuration(30000)
                .handler((externalTask, externalTaskService) -> {
                    boolean receiverIsEmpty=false;
                    boolean senderIsEmpty=false;

                        String jsonInput  = externalTask.getVariable("filteredChequeInfoReceive").toString();
                        SpinJsonNode arrayNode = JSON(jsonInput);
                        int size = arrayNode.elements().size();
                       if (size==0)
                            receiverIsEmpty=true;

                        String jsonInputIssuer = externalTask.getVariable("filteredChequeInfo").toString();
                        SpinJsonNode arrayNodeIssuer = JSON(jsonInputIssuer);
                        int sizeIssuer = arrayNodeIssuer.elements().size();
                        if(sizeIssuer==0)
                            senderIsEmpty=true;

                    if (senderIsEmpty&&receiverIsEmpty) {
                        Map<String, Object> result = new HashMap<>();
                        result.put("ErrorMsg", "شما هیچ گونه درخواستی ندارید");
                        externalTaskService.handleBpmnError(externalTask, "Error_End", "خطای سرویس",result );
                    }else{
                        Map<String, Object> result = new HashMap<>();
                        externalTaskService.complete(externalTask, result);
                    }
                })
                .open();
    }
}

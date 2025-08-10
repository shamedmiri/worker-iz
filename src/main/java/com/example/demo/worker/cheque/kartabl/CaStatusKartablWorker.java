package com.example.demo.worker.cheque.kartabl;

import com.example.demo.config.ApiUrlsProperties;
import com.example.demo.error.ErrorMessagesProperties;
import com.example.demo.service.cheque.kartabl.CaStatusService;
import com.example.demo.service.cheque.kartabl.IssuedChequeService;
import jakarta.annotation.PostConstruct;
import org.camunda.bpm.client.ExternalTaskClient;
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
public class CaStatusKartablWorker {

    @Autowired
    private ErrorMessagesProperties errorMessages;

    private final ApiUrlsProperties properties;
    private final CaStatusService apiService;

    public CaStatusKartablWorker(ApiUrlsProperties properties, CaStatusService apiService) {
        this.properties = properties;
        this.apiService = apiService;
    }

    @PostConstruct
    public void subscribe() {
        ExternalTaskClient client = ExternalTaskClient.create()
                .baseUrl(properties.getCamunda())
                .asyncResponseTimeout(20000)
                .build();

        client.subscribe("CaStatusKartablWorker")
                .lockDuration(30000)
                .handler((externalTask, externalTaskService) -> {
                    String TransactionId = externalTask.getVariable("TransactionId");

                    try {
                        Map<String, Object> apiResponse = apiService.callUserApi(TransactionId);
                        int statusCode = (int) apiResponse.get("statusCode");

                        if (statusCode != 200 && statusCode != 201) {
                            externalTaskService.handleBpmnError(externalTask, "Error_End", "خطای سرویس", apiResponse);
                            return;
                        }

                        Object outputRaw = apiResponse.get("Output");
                        if (outputRaw == null) {
                            throw new RuntimeException("متغیر 'Output' نال است.");
                        }

                        SpinJsonNode outputJson = JSON(outputRaw.toString());

                        if (!outputJson.hasProp("Status")) {
                            throw new RuntimeException("فیلد 'Status' در JSON وجود ندارد.");
                        }
                        int statusCode1 = outputJson.prop("Status").prop("StatusCode").prop("Value").numberValue().intValue();

                        if (statusCode1==502) {
                            Map<String, Object> result = new HashMap<>();
                            result.put("Sign",true);
                            externalTaskService.complete(externalTask, result);
                        }else{
                            Map<String, Object> result = new HashMap<>();
                            String responseRaw = outputJson.prop("Status").prop("StatusDetail").toString();
                            result.put("ErrorMsg",responseRaw);
                            result.put("Sign",false);
                            externalTaskService.complete(externalTask, result);
                        }

                    } catch (Exception e) {
                        externalTaskService.handleFailure(
                                externalTask,
                                "API error",
                                e.getMessage(),
                                0,
                                0
                        );
                    }
                })
                .open();
    }
}

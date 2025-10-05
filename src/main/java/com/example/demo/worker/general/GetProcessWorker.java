package com.example.demo.worker.general;

import com.example.demo.config.ApiUrlsProperties;
import com.example.demo.error.ErrorMessagesProperties;
import com.example.demo.service.general.GetProcessService;
import com.example.demo.service.general.SendSmsService;
import jakarta.annotation.PostConstruct;
import org.camunda.bpm.client.ExternalTaskClient;
import org.camunda.spin.Spin;
import org.camunda.spin.json.SpinJsonNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.configurationprocessor.json.JSONArray;
import org.springframework.boot.configurationprocessor.json.JSONException;
import org.springframework.boot.configurationprocessor.json.JSONObject;
import org.springframework.stereotype.Component;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.camunda.spin.Spin.JSON;


@Component
public class GetProcessWorker {

    private static final String TOPIC_NAME = "GetProcessWorker";
    private static final String SUCCESS_CODE = "100";

    @Autowired
    private ErrorMessagesProperties errorMessages;

    private final ApiUrlsProperties properties;
    private final GetProcessService apiService;

    public GetProcessWorker(ApiUrlsProperties properties, GetProcessService apiService) {
        this.properties = properties;
        this.apiService = apiService;
    }

    @PostConstruct
    public void subscribe() {
        ExternalTaskClient client = ExternalTaskClient.create()
                .baseUrl(properties.getCamunda())
                .asyncResponseTimeout(20000)
                .build();

        client.subscribe(TOPIC_NAME)
                .lockDuration(3000)
                .handler((externalTask, externalTaskService) -> {
                    String processName = externalTask.getVariable("processName");
                    try {
                        Map<String, Object> responseMap = apiService.callUserApi(processName);
                        int statusCode = (int) responseMap.get("statusCode");

                        if (statusCode == 200 || statusCode == 201) {
                            handleSuccessResponse(externalTaskService, externalTask, responseMap);
                        } else {
                            externalTaskService.handleBpmnError(externalTask, "Error_END", "خطای سرویس", responseMap);
                        }

                    } catch (Exception e) {
                        externalTaskService.handleFailure(externalTask, "API error", e.getMessage(), 0, 0);
                    }
                })
                .open();
    }

    private void handleSuccessResponse(org.camunda.bpm.client.task.ExternalTaskService externalTaskService,
                                       org.camunda.bpm.client.task.ExternalTask externalTask,
                                       Map<String, Object> responseMap) {

        SpinJsonNode jsonNode = Spin.JSON(responseMap.get("Output"));
        String responseCode = jsonNode.prop("ResponseCode").toString();

        if (SUCCESS_CODE.equals(responseCode)) {
            try {
                Object jsonObject = responseMap.get("Output");
                JSONObject x = new JSONObject(String.valueOf(jsonObject));
                JSONArray processServicesFee = x.getJSONArray("ProcessServicesFee");
                long totalAmount=0;
                String displayName="";
                boolean outPutPayfee=true;
                for (int i = 0; i < processServicesFee.length(); i++) {
                    JSONObject fee = processServicesFee.getJSONObject(i);
                    double amount = fee.getDouble("Amount");
                    totalAmount += amount;
                    displayName += fee.getString("DisplayName")+",";
                }
                Map<String, Object> variables = Map.of(
                        "totalAmount", totalAmount,
                        "displayName", displayName,
                        "outputPayfee", outPutPayfee
                );
                externalTaskService.complete(externalTask, variables);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }



        } else {
            Map<String, Object> variables = Map.of(
                    "errorMsg", jsonNode.prop("ResponseMessage").toString()
            );
            externalTaskService.handleBpmnError(externalTask, "Error_End", "خطای سرویس", variables);
        }
    }
}

package com.example.demo.worker.cheque.kartabl;

import com.example.demo.config.ApiUrlsProperties;
import com.example.demo.error.ErrorMessagesProperties;
import com.example.demo.service.cheque.kartabl.IssuedChequeService;
import jakarta.annotation.PostConstruct;
import org.camunda.bpm.client.ExternalTaskClient;
import org.camunda.bpm.client.task.ExternalTaskService;
import org.camunda.spin.json.SpinJsonNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.lang.reflect.Array;
import java.util.*;

import static org.camunda.spin.Spin.JSON;

@Component
public class IssuedChequeKartablWorker {

    @Autowired
    private ErrorMessagesProperties errorMessages;

    private final ApiUrlsProperties properties;
    private final IssuedChequeService apiService;

    public IssuedChequeKartablWorker(ApiUrlsProperties properties, IssuedChequeService apiService) {
        this.properties = properties;
        this.apiService = apiService;
    }

    @PostConstruct
    public void subscribe() {
        ExternalTaskClient client = ExternalTaskClient.create()
                .baseUrl(properties.getCamunda())
                .asyncResponseTimeout(20000)
                .build();

        client.subscribe("IssuedChequeKartablWorker")
                .lockDuration(30000)
                .handler((externalTask, externalTaskService) -> {
                    String customerNumber = externalTask.getVariable("customerNumber");

                    try {
                        Map<String, Object> apiResponse = apiService.callUserApi(customerNumber);
                        int statusCode = (int) apiResponse.get("statusCode");

                        if (statusCode != 200 && statusCode != 201) {
                            externalTaskService.handleBpmnError(externalTask, "Error_END", "خطای سرویس", apiResponse);
                            return;
                        }

                        Object outputRaw = apiResponse.get("Output");
                        if (outputRaw == null) {
                            throw new RuntimeException("متغیر 'Output' نال است.");
                        }

                        SpinJsonNode outputJson = JSON(outputRaw.toString());

                        if (!outputJson.hasProp("Requests")) {
                            throw new RuntimeException("فیلد 'Requests' در JSON وجود ندارد.");
                        }

                        String responseCode = outputJson.prop("ResponseCode").toString();
//                        if (!"100".equals(responseCode)) {
//                            Map<String, Object> errorVars = new HashMap<>();
//                            errorVars.put("errorMsg", outputJson.prop("ResponseMessage").stringValue());
//                            externalTaskService.handleBpmnError(externalTask, "Error_Return", "خطای سرویس", errorVars);
//                            return;
//                        }
                        if ("100".equals(responseCode)) {
                            SpinJsonNode requests = outputJson.prop("Requests");
                            List<Map<String, Object>> simplifiedObjects = new ArrayList<>();

                            for (SpinJsonNode item : requests.elements()) {
                                Map<String, Object> obj = new HashMap<>();
                                obj.put("SayadId", item.prop("SayadId").stringValue());
                                obj.put("SerialNumber", item.prop("SerialNumber").stringValue());
                                obj.put("ChequeMedia", item.prop("ChequeMedia").value());
                                obj.put("select", false);
                                simplifiedObjects.add(obj);
                            }
                            SpinJsonNode jsonArrayNode = JSON(simplifiedObjects);
                            Map<String, Object> result = new HashMap<>();
                            result.put("filteredChequeInfo", jsonArrayNode.toString());
                            externalTaskService.complete(externalTask, result);
                        }else{
                            List<Map<String, Object>> simplifiedObjects = new ArrayList<>();
                            SpinJsonNode jsonArrayNode = JSON(simplifiedObjects);
                            Map<String, Object> result = new HashMap<>();
                            result.put("filteredChequeInfo", jsonArrayNode.toString());
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

package com.example.demo.worker.agents;

import com.example.demo.config.ApiUrlsProperties;
import com.example.demo.error.ErrorMessagesProperties;
import com.example.demo.service.agents.GetAgentInfoService;
import com.example.demo.service.customer.GetCustomerInfoService;
import jakarta.annotation.PostConstruct;
import org.camunda.bpm.client.ExternalTaskClient;
import org.camunda.spin.Spin;
import org.camunda.spin.json.SpinJsonNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class GetAgentInfoWorker {
    @Autowired
    private ErrorMessagesProperties errorMessages;
    private final ApiUrlsProperties properties;
    private final GetAgentInfoService apiService;
    public GetAgentInfoWorker(ApiUrlsProperties properties, GetAgentInfoService apiService) {
        this.properties = properties;
        this.apiService = apiService;
    }
    @PostConstruct
    public void subscribe() {
        ExternalTaskClient client = ExternalTaskClient.create()
                .baseUrl(properties.getCamunda())
                .asyncResponseTimeout(20000)
                .build();
        client.subscribe("getInfoAgent") // تاپیک جدید در BPMN
                .lockDuration(30000)
                .handler((externalTask, externalTaskService) -> {
                    String customerNumber = externalTask.getVariable("customerNumber");
                    try {
                        Map<String, Object> variables = apiService.callUserApi(customerNumber);
                        int statusCode = (int) variables.get("statusCode");
                        if (statusCode == 200 || statusCode == 201) {
                            SpinJsonNode jsonNode = Spin.JSON(variables.get("Output"));
                            Map<String, Object> result = new HashMap<>();
                            result.put("Identifier", jsonNode.prop("Customer").prop("NationalId").stringValue());
                            result.put("shahabId", jsonNode.prop("Customer").prop("ShahabCode").stringValue());
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

package com.example.demo.worker.inquiry;

import com.example.demo.config.ApiUrlsProperties;
import com.example.demo.error.ErrorMessagesProperties;
import com.example.demo.service.customer.GetCustomerInfoService;
import com.example.demo.service.inquiry.PostalInquiryService;
import jakarta.annotation.PostConstruct;
import org.camunda.bpm.client.ExternalTaskClient;
import org.camunda.spin.Spin;
import org.camunda.spin.json.SpinJsonNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class PostalInquiryWorker {
    @Autowired
    private ErrorMessagesProperties errorMessages;
    private final ApiUrlsProperties properties;
    private final PostalInquiryService apiService;
    public PostalInquiryWorker(ApiUrlsProperties properties, PostalInquiryService apiService) {
        this.properties = properties;
        this.apiService = apiService;
    }
    @PostConstruct
    public void subscribe() {
        ExternalTaskClient client = ExternalTaskClient.create()
                .baseUrl(properties.getCamunda())
                .asyncResponseTimeout(20000)
                .build();
        client.subscribe("PostalInquiryWorker") // تاپیک جدید در BPMN
                .lockDuration(30000)
                .handler((externalTask, externalTaskService) -> {
                    String postalCode = externalTask.getVariable("PostalCode");
                    try {
                        Map<String, Object> variables = apiService.callUserApi(postalCode);
                        int statusCode = (int) variables.get("statusCode");
                        if (statusCode == 200 || statusCode == 201) {
                            SpinJsonNode jsonNode = Spin.JSON(variables.get("Output"));
                            Map<String, Object> result = new HashMap<>();
                            result.put("Address", jsonNode.prop("Address").stringValue());
                            externalTaskService.complete(externalTask, result);
                        } else {
                            Map<String, Object> result = new HashMap<>();
                            result.put("errorMSG", errorMessages.get("SERVICE_IS_UNAVAILABLE"));
                            externalTaskService.handleBpmnError(externalTask, "Error_Return", "خطای سرویس", result);
                        }
                    } catch (Exception e) {
                        externalTaskService.handleFailure(externalTask, "API error", e.getMessage(), 0, 0);
                    }
                })
                .open();
    }
}

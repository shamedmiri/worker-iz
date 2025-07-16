package com.example.demo.worker.cheque.transfer;

import com.example.demo.config.ApiUrlsProperties;
import com.example.demo.error.ErrorMessagesProperties;
import com.example.demo.service.cheque.transfer.ChequeInquiryReceiversService;
import jakarta.annotation.PostConstruct;
import org.camunda.bpm.client.ExternalTaskClient;
import org.camunda.spin.Spin;
import org.camunda.spin.json.SpinJsonNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class ChequeInquiryReceiversWorker {

    private static final String TOPIC_NAME = "ChequeInquiryReceiversWorker";
    private static final String SUCCESS_CODE = "100";

    @Autowired
    private ErrorMessagesProperties errorMessages;

    private final ApiUrlsProperties properties;
    private final ChequeInquiryReceiversService apiService;

    public ChequeInquiryReceiversWorker(ApiUrlsProperties properties, ChequeInquiryReceiversService apiService) {
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
                .lockDuration(30000)
                .handler((externalTask, externalTaskService) -> {
                    String sayadId = externalTask.getVariable("sayadId");
                    String receiversDataInput = externalTask.getVariable("ReceiverTable");
                    try {
                        Map<String, Object> responseMap = apiService.callUserApi(sayadId, receiversDataInput);
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
            Map<String, Object> variables = Map.of(
                    "outputServiceReceiverInquiry", jsonNode.prop("ReceiversInfo").toString()
            );
            externalTaskService.complete(externalTask, variables);
        } else {
            Map<String, Object> variables = Map.of(
                    "errorMsg", jsonNode.prop("ResponseMessage").toString()
            );
            externalTaskService.handleBpmnError(externalTask, "Error_Return", "خطای سرویس", variables);
        }
    }
}

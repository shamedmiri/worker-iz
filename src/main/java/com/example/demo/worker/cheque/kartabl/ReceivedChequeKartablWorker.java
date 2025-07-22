package com.example.demo.worker.cheque.kartabl;

import com.example.demo.config.ApiUrlsProperties;
import com.example.demo.error.ErrorMessagesProperties;
import com.example.demo.service.cheque.kartabl.ReceiveredChequeKartablService;
import jakarta.annotation.PostConstruct;
import org.camunda.bpm.client.ExternalTaskClient;
import org.camunda.spin.Spin;
import org.camunda.spin.json.SpinJsonNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.camunda.spin.Spin.JSON;

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

        String topicName = "ReceivedChequeKartablWorker";

        client.subscribe(topicName)
                .lockDuration(30000)
                .handler((externalTask, externalTaskService) -> {
                    String customerNumber = externalTask.getVariable("customerNumber");
                    String identifier = externalTask.getVariable("Identifier");
                    String shahabId = externalTask.getVariable("shahabId");

                    try {
                        Map<String, Object> apiResponse = apiService.callUserApi(shahabId, identifier, customerNumber);
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

                        if (!outputJson.hasProp("Response")) {
                            throw new RuntimeException("فیلد 'Response' در JSON وجود ندارد.");
                        }

                        String responseCode = outputJson.prop("ResponseCode").toString();
//                        if (!"100".equals(responseCode)) {
//                            Map<String, Object> errorVars = new HashMap<>();
//                            errorVars.put("errorMsg", outputJson.prop("ResponseMessage").stringValue());
//                            externalTaskService.handleBpmnError(externalTask, "Error_Return", "خطای سرویس", errorVars);
//                            return;
//                        }

                        // دریافت رشته‌ی JSON آرایه‌ای
                        String responseRaw = outputJson.prop("Response").stringValue();
                        SpinJsonNode requests = JSON(responseRaw);

                        if (!requests.isArray()) {
                            throw new RuntimeException("'Response' is not a JSON array.");
                        }

                        if ("100".equals(responseCode)) {
                            List<SpinJsonNode> jsonItems = new ArrayList<>();
                            for (SpinJsonNode item : requests.elements()) {
                                SpinJsonNode simplified = JSON("{ }");
                                simplified.prop("sayadId", item.prop("sayadId"));
                                simplified.prop("serialNo", item.prop("serialNo"));
                                simplified.prop("seriesNo", item.prop("seriesNo"));
                                simplified.prop("amount", item.prop("amount"));
                                simplified.prop("dueDate", item.prop("dueDate"));
                                simplified.prop("description", item.prop("description"));

                                String statusCheck = item.prop("chequeStatus").toString();
                                String detailstatusCheck = "";
                                if (statusCheck.equals("1")) {
                                    detailstatusCheck = "ثبت شده با تائید گیرنده";
                                } else if (statusCheck.equals("2")) {
                                    detailstatusCheck = "نقد شده است";
                                }
                                simplified.prop("chequeStatus", detailstatusCheck);
                                simplified.prop("select", false);
                                jsonItems.add(simplified);
                            }

                            SpinJsonNode resultArray = JSON("[]");
                            for (SpinJsonNode item : jsonItems) {
                                resultArray.append(item);
                            }

                            Map<String, Object> result = new HashMap<>();
                            result.put("filteredChequeInfoReceive", resultArray.toString());
                            externalTaskService.complete(externalTask, result);
                        }else{
                            Map<String, Object> result = new HashMap<>();
                            result.put("filteredChequeInfoReceive", "");
                            externalTaskService.complete(externalTask, result);
                        }

                    } catch (Exception e) {
                        externalTaskService.handleFailure(externalTask, "API error", e.getMessage(), 0, 0);
                    }
                })
                .open();
    }
}

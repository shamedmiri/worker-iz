package com.example.demo.worker.cheque.hamoon;
import com.example.demo.config.ApiUrlsProperties;
import com.example.demo.error.ErrorMessagesProperties;
import com.example.demo.service.cheque.hamoon.DeactiveHamoonService;
import com.example.demo.utils.ConvertDate;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.camunda.bpm.client.ExternalTaskClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import jakarta.annotation.PostConstruct;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
@Component
public class DeactiveHaoonWorker {
    @Autowired
    private ErrorMessagesProperties errorMessages;
    private final ApiUrlsProperties properties;
    private final DeactiveHamoonService apiService;
    public DeactiveHaoonWorker(ApiUrlsProperties properties, DeactiveHamoonService apiService) {
        this.properties = properties;
        this.apiService = apiService;
    }
    @PostConstruct
    public void subscribe() {
        ExternalTaskClient client = ExternalTaskClient.create()
                .baseUrl(properties.getCamunda())
                .asyncResponseTimeout(20000)
                .build();
        String disableHamoonWorker = "disableHamoonWorkerVer2";
        client.subscribe(disableHamoonWorker) // تاپیک جدید در BPMN
                .lockDuration(30000)
                .handler((externalTask, externalTaskService) -> {
                    String customerNumber = externalTask.getVariable("customerNumber");
                    String idCode = externalTask.getVariable("Identifier");
                    String shahabId = externalTask.getVariable("shahabId");
                    ConvertDate convertDate=new ConvertDate();
                    String requestDate = convertDate.currentDateShamsi();
                    OffsetDateTime tehranTime = OffsetDateTime.now(ZoneOffset.ofHoursMinutes(3, 30));
                    String formatted = tehranTime.format(DateTimeFormatter.ofPattern("HHmmss"));
                    String requestDateTime = requestDate+formatted;
                    try {
                        Map<String, Object> variables = apiService.callUserApi(shahabId, idCode, requestDateTime, customerNumber);
                        int statusCode = (int) variables.get("statusCode");
                        if (statusCode == 200 || statusCode == 201) {
                            String json =variables.get("Output").toString();
                            ObjectMapper mapper = new ObjectMapper();
                            JsonNode root = mapper.readTree(json);
                            String responseString = root.path("Response").asText();
                            JsonNode responseJson = mapper.readTree(responseString);
                            String errorCode = "";
                            try {
                                errorCode = responseJson.path("errorCode").asText();
                            }catch (Exception e){
                                e.printStackTrace();
                            }
                            if(errorCode.equals("CSR102")){
                                String msg=responseJson.path("errorMessage").asText();
                                Map<String, Object> resultError = new HashMap<>();
                                resultError.put("ErrorMsg", errorMessages.get("CUSTOMER_IS_DE_ACTIVE"));
                                externalTaskService.handleBpmnError(externalTask, "Error_Return", msg, resultError);

                            }else {
                                Map<String, Object> result = new HashMap<>();
                                result.put("messageResult", "غیرفعالسازی پروفایل با موفقیت صورت پذیرفت");
                                result.put("outputService", variables.toString());
                                externalTaskService.complete(externalTask, result);
                            }
                        } else {
                            externalTaskService.handleBpmnError(externalTask, "Error_Return", "خطای سرویس", variables);
                        }
                    } catch (Exception e) {
                        externalTaskService.handleFailure(externalTask, "API error", e.getMessage(), 0, 0);
                    }
                })
                .open();
    }
}

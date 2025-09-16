package com.example.demo.service.loan;

import com.example.demo.config.ApiUrlsProperties;
import com.example.demo.error.ErrorMessagesProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.Map;

@Service
public class SaveRequestLoanService {

    @Autowired
    private ErrorMessagesProperties errorMessages;

    private final HttpClient httpClient;
    private final ApiUrlsProperties urls;
    private final ObjectMapper objectMapper;

    public SaveRequestLoanService(HttpClient httpClient, ApiUrlsProperties urls) {
        this.httpClient = httpClient;
        this.urls = urls;
        this.objectMapper = new ObjectMapper();
    }

    public Map<String, Object> callUserApi(String nationalCode, String mobile, String fullName, String applicationKey, String customerNumber,
                                           String loanplanid,String selectedDeposit, String amount,String repaymentperiodid,String paymentmethodid,String scenarioid) throws Exception {
        return callApi(urls.getSaverequestLoan(), nationalCode, mobile, fullName, applicationKey, customerNumber, loanplanid, amount,
                repaymentperiodid, paymentmethodid, scenarioid,selectedDeposit);
    }

    private Map<String, Object> callApi(String url, String nationalCode, String mobile, String fullName, String applicationKey, String customerNumber,
                                        String loanplanid, String amount,String repaymentperiodid,String paymentmethodid,String scenarioid,String selectedDeposit) throws Exception {


        Map<String, Object> map = new HashMap<>();
        map.put("RequestTypeId", "11");
        map.put("NationalCode", nationalCode);
        map.put("Mobile", mobile);
        map.put("FullName", fullName);
        map.put("InstanceId", "faraz-camunda");
        map.put("DefinitionId", "faraz-camunda");
        map.put("ApplicationKey", applicationKey);
        map.put("CustomerNumber", customerNumber);
        map.put("NationalCardSerial", "");
        map.put("VideoIsValid", true);
        map.put("PlanId", loanplanid);
        map.put("Amount", amount);
        map.put("RepaymentPeriodId", repaymentperiodid);
        map.put("PaymentMethodId", paymentmethodid);
        map.put("ScenarioId", scenarioid);
        map.put("DepositNumber", selectedDeposit);

        String jsonRequest = objectMapper.writeValueAsString(map);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Accept", "application/json")
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonRequest))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        Map<String, Object> result = new HashMap<>();
        int statusCode = response.statusCode();
        result.put("statusCode", statusCode);

        if (statusCode == 200 || statusCode == 201) {
            result.put("Output", response.body());
        } else {
            result.put("ErrorMessage", errorMessages.get("GENERAL_ERROR"));
        }

        return result;
    }
}

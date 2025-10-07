package com.example.demo.service.card;

import com.example.demo.config.ApiUrlsProperties;
import com.example.demo.error.ErrorMessagesProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.Map;
@org.springframework.stereotype.Service
public class DepositToIbanService {
    @Autowired
    private ErrorMessagesProperties errorMessages;

    private final HttpClient httpClient;
    private final ApiUrlsProperties urls;
    private final ObjectMapper objectMapper;

    public DepositToIbanService(HttpClient httpClient, ApiUrlsProperties urls) {
        this.httpClient = httpClient;
        this.urls = urls;
        this.objectMapper = new ObjectMapper();
    }

    public Map<String, Object> callUserApi(String branchCode, String depositType, String customerNumber,String serialNumber, String depositNumber) throws Exception {
        return callApi(urls.getDepositToIban(), branchCode , depositType,customerNumber,serialNumber,depositNumber);
    }

    private Map<String, Object> callApi(String url, String branchCode, String depositType,String customerNumber,String serialNumber,String depositNumber) throws Exception {

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url + "?BranchCode="+branchCode+"&DepositType="+depositType
                +"&CustomerNumber="+customerNumber+"&Serial="+serialNumber))
                .header("Content-Type", "application/json")
                .GET()
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



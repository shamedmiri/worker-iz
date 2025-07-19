package com.example.demo.service.cheque.kartabl;

import com.example.demo.config.ApiUrlsProperties;
import com.example.demo.error.ErrorMessagesProperties;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.*;

@Service
public class ReceiveredChequeKartablService {

    private final ErrorMessagesProperties errorMessages;
    private final HttpClient httpClient;
    private final ApiUrlsProperties urls;
    private final ObjectMapper objectMapper;

    @Autowired
    public ReceiveredChequeKartablService(HttpClient httpClient, ApiUrlsProperties urls, ErrorMessagesProperties errorMessages) {
        this.httpClient = httpClient;
        this.urls = urls;
        this.errorMessages = errorMessages;
        this.objectMapper = new ObjectMapper();
    }

    public Map<String, Object> callUserApi(String shahabId, String identifier, String customerNumber) throws Exception {
        String url = urls.getReceiverChequeKartabl();
        return callApi(url,shahabId, identifier,customerNumber);
    }

    private Map<String, Object> callApi(String url,String shahabId, String identifier, String customerNumber) throws Exception {
        Map<String, Object> requestBody = buildRequestBody(shahabId, identifier,customerNumber);
        String jsonRequest = objectMapper.writeValueAsString(requestBody);

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

    private Map<String, Object> buildRequestBody(String shahabId, String identifier, String customerNumber) throws Exception {


        Map<String, Object> input = new HashMap<>();
        Map<String, Object> requestInfo = new HashMap<>();
        Map<String, Object> innerInput = new HashMap<>();

        innerInput.put("shahabId", shahabId);//1000000521119009
        innerInput.put("idCode", identifier);//0521119006");
        innerInput.put("idType", 1);
        innerInput.put("requestDate", "14031108113000");

        requestInfo.put("EndUrl", "inquiry/cartable");
        requestInfo.put("Type", "Cartable");
        requestInfo.put("SayadId", "");
        requestInfo.put("Input", innerInput);

        input.put("RequestInfo", requestInfo);
        input.put("Channel", 1);
        input.put("BranchCode", 0);
        input.put("UserName", "");
        input.put("AuthStatus", 1);
        input.put("CustomerNumber", customerNumber);

        return input;
    }

    private String getIdentifierType(String code) {
        return switch (code.length()) {
            case 11 -> "2";
            case 10 -> "1";
            default -> "3";
        };
    }
}

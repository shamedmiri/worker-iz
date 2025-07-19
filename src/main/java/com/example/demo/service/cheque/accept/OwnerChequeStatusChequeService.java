package com.example.demo.service.cheque.accept;

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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class OwnerChequeStatusChequeService {

    @Autowired
    private ErrorMessagesProperties errorMessages;

    private final HttpClient httpClient;
    private final ApiUrlsProperties urls;
    private final ObjectMapper objectMapper;

    public OwnerChequeStatusChequeService(HttpClient httpClient, ApiUrlsProperties urls) {
        this.httpClient = httpClient;
        this.urls = urls;
        this.objectMapper = new ObjectMapper();
    }

    public Map<String, Object> callUserApi( String sayadId, String idCode) throws Exception {
        return callApi(urls.getOwnerAcceptChequeStatus(), sayadId, idCode);
    }

    private Map<String, Object> callApi(String url, String sayadId, String idCode) throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();



        Map<String, Object> requestInfo = new HashMap<>();
        requestInfo.put("idType", 1);
        requestInfo.put("sayadId", sayadId);
        requestInfo.put("idCode", idCode);

        Map<String, Object> bodyMap = new HashMap<>();
        bodyMap.put("RequestInfo", requestInfo);
        bodyMap.put("Channel", 1);
        bodyMap.put("UserName", "123");
        bodyMap.put("BranchCode", 101);
        bodyMap.put("CustomerNumber", 133);
        bodyMap.put("AuthStatus", 0);

        String jsonRequest = objectMapper.writeValueAsString(bodyMap);

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

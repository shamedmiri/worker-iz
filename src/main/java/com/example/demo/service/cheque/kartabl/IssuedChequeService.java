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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class IssuedChequeService {

    private final ErrorMessagesProperties errorMessages;
    private final HttpClient httpClient;
    private final ApiUrlsProperties urls;

    @Autowired
    public IssuedChequeService(HttpClient httpClient, ApiUrlsProperties urls, ErrorMessagesProperties errorMessages) {
        this.httpClient = httpClient;
        this.urls = urls;
        this.errorMessages = errorMessages;
    }

    public Map<String, Object> callUserApi(String customerNumber) throws Exception {
        String url = urls.getIssuedChequeKartabl();
        return callApi(url, customerNumber);
    }

    private Map<String, Object> callApi(String url, String customerNumber) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url + customerNumber))
                .header("Content-Type", "application/json")
                .GET()
                .build();
        Map<String, Object> result = new HashMap<>();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        int statusCode = response.statusCode();
        result.put("statusCode", statusCode);
        if (statusCode == 200 || statusCode == 201) {
            String responseBody = response.body();
            result.put("Output", responseBody);
        } else {
            result.put("ErrorMessage", errorMessages.get("GENERAL_ERROR"));
        }
        return result;

    }

}

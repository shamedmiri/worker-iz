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
public class CardToIbanService {
    @Autowired
    private ErrorMessagesProperties errorMessages;

    private final HttpClient httpClient;
    private final ApiUrlsProperties urls;
    private final ObjectMapper objectMapper;

    public CardToIbanService(HttpClient httpClient, ApiUrlsProperties urls) {
        this.httpClient = httpClient;
        this.urls = urls;
        this.objectMapper = new ObjectMapper();
    }

    public Map<String, Object> callUserApi(String card) throws Exception {
        return callApi(urls.getCardToIban(), card );
    }

    private Map<String, Object> callApi(String url, String card) throws Exception {


        Map<String, Object> requestMap = new HashMap<>();
        requestMap.put("card", "");

        String jsonRequest = objectMapper.writeValueAsString(requestMap);
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



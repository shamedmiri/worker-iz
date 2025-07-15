package com.example.demo.service.customer;

import com.example.demo.config.ApiUrlsProperties;
import com.example.demo.error.ErrorMessagesProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.Map;

@Service
public class GetCustomerInfoByNationalCodeService {
    @Autowired
    private ErrorMessagesProperties errorMessages;
    private final HttpClient httpClient;
    private final ApiUrlsProperties urls;
    public GetCustomerInfoByNationalCodeService(HttpClient httpClient, ApiUrlsProperties urls) {
        this.httpClient = httpClient;
        this.urls = urls;
    }
    public Map<String, Object> callUserApi(String nationalCode) throws Exception {
        return callApi(urls.getGetCustomerByNationalCodeFromReport(), nationalCode);
    }
    private Map<String, Object> callApi(String url, String nationalCode) throws Exception {
        Map<String, Object> result = new HashMap<>();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url+nationalCode))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.noBody())
                .build();
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

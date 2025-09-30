package com.example.demo.service.loan;

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
public class SanaInquiryService {
    @Autowired
    private ErrorMessagesProperties errorMessages;
    private final HttpClient httpClient;
    private final ApiUrlsProperties urls;
    public SanaInquiryService(HttpClient httpClient, ApiUrlsProperties urls) {
        this.httpClient = httpClient;
        this.urls = urls;
    }
    public Map<String, Object> callUserApi(String NationalCode, String PersonType) throws Exception {
        return callApi(urls.getSanaInquiry(), NationalCode, PersonType );
    }
    private Map<String, Object> callApi(String url, String NationalCode, String PersonType ) throws Exception {
        Map<String, Object> map = new HashMap<>();
        map.put("NationalCode", "");
        map.put("PersonType", "");

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.noBody())
                .build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        int statusCode = response.statusCode();
        map.put("statusCode", statusCode);
        if (statusCode == 200 || statusCode == 201) {
            String responseBody = response.body();
            map.put("Output", responseBody);
        } else {
            map.put("ErrorMessage", errorMessages.get("GENERAL_ERROR"));
        }
        return map;
    }
}


package com.example.demo.service.cheque;

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
public class ChequeIssuerInquiryService {
    @Autowired
    private ErrorMessagesProperties errorMessages;

    private final HttpClient httpClient;
    private final ApiUrlsProperties urls;

    public ChequeIssuerInquiryService(HttpClient httpClient, ApiUrlsProperties urls) {
        this.httpClient = httpClient;
        this.urls = urls;
    }

    public Map<String, Object> callUserApi(String sayadId, String identifier, String shahabId) throws Exception {
        return callApi(urls.getChequeIssuerInquiry(), sayadId, identifier, shahabId);
    }


    private Map<String, Object> callApi(String url, String sayadId, String identifier, String shahabId) throws Exception {
        Map<String, Object> result = new HashMap<>();
        String jsonRequest = String.format("""
                {
                 "RequestInfo":{
                    "SayadId": "%s",
                    "Identifier": "%s",
                    "IdentifierType": 1,
                    "ShahabId": "%s"
                      },
                	"Channel": 3,
                	"BranchCode": 101,
                	"UserName": "19506",
                	"AuthStatus": 4,
                	"CustomerNumber": 0
                 }
                
                
                """, sayadId, identifier, shahabId);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("accept", "application/json")
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonRequest))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        int statusCode = response.statusCode();
        result.put("statusCode", statusCode);

        if (statusCode == 200 || statusCode == 201) {
            String responseBody = response.body();
            result.put("Output", responseBody);

        } else {
            System.out.println("Something went wrong");
            result.put("ErrorMessage", errorMessages.get("GENERAL_ERROR"));
        }

        return result;
    }
}

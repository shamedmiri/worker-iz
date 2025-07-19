package com.example.demo.service.cheque.hamoon;

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
public class DeactiveHamoonService {
    @Autowired
    private ErrorMessagesProperties errorMessages;

    private final HttpClient httpClient;
    private final ApiUrlsProperties urls;

    public DeactiveHamoonService(HttpClient httpClient, ApiUrlsProperties urls) {
        this.httpClient = httpClient;
        this.urls = urls;
    }

    public Map<String, Object> callUserApi(String shahabId,  String idCode, String requestDateTime, String customerNumber) throws Exception {
        return callApi(urls.getDeactivateHamoon(), shahabId,idCode,requestDateTime, customerNumber );
    }


    private Map<String, Object> callApi(String url,  String shahabId, String idCode, String requestDateTime, String customerNumber) throws Exception {
        Map<String, Object> result = new HashMap<>();
        String jsonRequest = String.format("""
                {
                  "RequestInfo": {
                    "EndUrl": "customer/deactivation",
                    "Type": "Dectivation",
                    "SayadId": "",
                    "Input": {
                      "tokenType": 1,
                      "customer": {
                        "shahabId": "%s",
                        "idCode": "%s",
                        "idType": 1
                      },
                      "legalStamp": 0,
                      "bankCode": "69",
                      "requestDateTime": "%s"
                    }
                  },
                  "Channel": 1,
                  "BranchCode": 0,
                  "UserName": "",
                  "AuthStatus": 1,
                  "CustomerNumber": "%s"
                }
                """, shahabId, idCode, requestDateTime, customerNumber);

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
            result.put("ErrorMessage", errorMessages.get("GENERAL_ERROR"));
        }

        return result;
    }
}

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
public class CaStatusService {

    private final ErrorMessagesProperties errorMessages;
    private final HttpClient httpClient;
    private final ApiUrlsProperties urls;
    private final ObjectMapper objectMapper;

    @Autowired
    public CaStatusService(HttpClient httpClient, ApiUrlsProperties urls, ErrorMessagesProperties errorMessages) {
        this.httpClient = httpClient;
        this.urls = urls;
        this.errorMessages = errorMessages;
        this.objectMapper = new ObjectMapper();
    }

    public Map<String, Object> callUserApi(String trnId) throws Exception {
        String url = urls.getCaStatus();
        return callApi(url,trnId);
    }

    private Map<String, Object> callApi(String url,String trnId) throws Exception {
        Map<String, Object> requestBody = buildRequestBody(trnId);
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

    private Map<String, Object> buildRequestBody(String trnId) throws Exception {


        Map<String, Object> apInfo = new HashMap<>();
        apInfo.put("AP_ID", "http://www.ap-irza-fraz.ir");
        apInfo.put("AP_TransID", "FRAZ2025011816040000");
        apInfo.put("AP_URL", "http://www.ap-irza-fraz.ir");
        apInfo.put("AP_REFERRER", "IRZA");

        Map<String,Object> msspId = new HashMap<>();
        msspId.put("DNSName", "mss.isc.co.ir");

        Map<String,Object> msspInfo = new HashMap<>();
        msspInfo.put("MSSP_ID", msspId);

        Map<String,Object> mainMap = new HashMap<>();
        mainMap.put("MajorVersion", 1);
        mainMap.put("MinorVersion", 1);
        mainMap.put("MSSP_TransID", trnId);
        mainMap.put("AP_Info", apInfo);
        mainMap.put("MSSP_Info", msspInfo);

        return mainMap;
    }

    private String getIdentifierType(String code) {
        return switch (code.length()) {
            case 11 -> "2";
            case 10 -> "1";
            default -> "3";
        };
    }
}

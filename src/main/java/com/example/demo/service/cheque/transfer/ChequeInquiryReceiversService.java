package com.example.demo.service.cheque.transfer;

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
public class ChequeInquiryReceiversService {

    private final ErrorMessagesProperties errorMessages;
    private final HttpClient httpClient;
    private final ApiUrlsProperties urls;
    private final ObjectMapper objectMapper;

    @Autowired
    public ChequeInquiryReceiversService(HttpClient httpClient, ApiUrlsProperties urls, ErrorMessagesProperties errorMessages) {
        this.httpClient = httpClient;
        this.urls = urls;
        this.errorMessages = errorMessages;
        this.objectMapper = new ObjectMapper();
    }

    public Map<String, Object> callUserApi( String sayadId, String receiversDataInput) throws Exception {
        String url = urls.getReceiverInquiryCheque();
        return callApi(url,sayadId,receiversDataInput);
    }

    private Map<String, Object> callApi(String url, String sayadId, String receiversDataInput) throws Exception {
        Map<String, Object> requestBody = buildRequestBody(sayadId, receiversDataInput);
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

    private Map<String, Object> buildRequestBody(String sayadId, String receiversDataInput) throws Exception {
        List<Map<String, Object>> receiversData = objectMapper.readValue(
                receiversDataInput,
                new TypeReference<List<Map<String, Object>>>() {}
        );

        List<Map<String, Object>> inquiryList = new ArrayList<>();
        for (Map<String, Object> receiver : receiversData) {
            String code = receiver.get("Identifier").toString();
            Map<String, Object> receiverMap = new HashMap<>();
            receiverMap.put("Identifier", code);
            receiverMap.put("IdentifierType", getIdentifierType(code));
            inquiryList.add(receiverMap);
        }

        Map<String, Object> requestInfo = new HashMap<>();
        requestInfo.put("SayadId", sayadId);
        requestInfo.put("ReceiversId", inquiryList);

        Map<String, Object> requestMap = new HashMap<>();
        requestMap.put("BranchCode", "0");
        requestMap.put("UserName", "");
        requestMap.put("Channel", "1");
        requestMap.put("AuthStatus", "1");
        requestMap.put("DefinitionId", "");
        requestMap.put("InstanceId", "");
        requestMap.put("CustomerNumber", 11);
        requestMap.put("RequestInfo", requestInfo);

        return requestMap;
    }

    private String getIdentifierType(String code) {
        return switch (code.length()) {
            case 11 -> "2";
            case 10 -> "1";
            default -> "3";
        };
    }
}

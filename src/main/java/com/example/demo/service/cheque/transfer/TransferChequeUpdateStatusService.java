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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class TransferChequeUpdateStatusService {

    @Autowired
    private ErrorMessagesProperties errorMessages;

    private final HttpClient httpClient;
    private final ApiUrlsProperties urls;
    private final ObjectMapper objectMapper;

    public TransferChequeUpdateStatusService(HttpClient httpClient, ApiUrlsProperties urls) {
        this.httpClient = httpClient;
        this.urls = urls;
        this.objectMapper = new ObjectMapper();
    }

    public Map<String, Object> callUserApi( String fullName, String identifier, String shahabId,
                                           String receivers, String signers, String description,
                                           String toIban, String reason, String sayadId) throws Exception {
        return callApi(urls.getTransferCheque(), fullName, identifier, shahabId, receivers, signers,
                description, toIban, reason, sayadId);
    }

    private Map<String, Object> callApi(String url, String fullName, String identifier, String shahabId,
                                        String receivers, String signers, String description,
                                        String toIban, String reason, String sayadId) throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();

        List<Map<String, Object>> receiversList = objectMapper.readValue(
                receivers,
                new TypeReference<List<Map<String, Object>>>() {}
        );
        List<Map<String, Object>> signersList = objectMapper.readValue(
                signers,
                new TypeReference<List<Map<String, Object>>>() {}
        );

        Map<String, Object> holder = Map.of(
                "FullName", fullName,
                "Identifier", identifier,
                "IdentifierType", 1,
                "ShahabId", shahabId
        );
System.setOut(System.out);
        Map<String, Object> requestInfo = new HashMap<>();
        requestInfo.put("Holder", holder);
        requestInfo.put("Receivers", receiversList);
        requestInfo.put("Signers", signersList);
        requestInfo.put("Description", description);
        requestInfo.put("AcceptTransfer", 0);
        requestInfo.put("ToIban", toIban);
        requestInfo.put("Reason", reason);
        requestInfo.put("SayadId", sayadId);
        requestInfo.put("GiveBack", 0);

        Map<String, Object> bodyMap = new HashMap<>();
        bodyMap.put("RequestInfo", requestInfo);
        bodyMap.put("Channel", 1);
        bodyMap.put("UserName", "");
        bodyMap.put("BranchCode", 201);
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

package com.example.demo.service.cheque.accept;

import com.example.demo.config.ApiUrlsProperties;
import com.example.demo.error.ErrorMessagesProperties;
import com.example.demo.utils.ConvertDate;
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
public class AcceptChequeService {

    @Autowired
    private ErrorMessagesProperties errorMessages;

    private final HttpClient httpClient;
    private final ApiUrlsProperties urls;
    private final ObjectMapper objectMapper;

    public AcceptChequeService(HttpClient httpClient, ApiUrlsProperties urls) {
        this.httpClient = httpClient;
        this.urls = urls;
        this.objectMapper = new ObjectMapper();
    }

    public Map<String, Object> callUserApi(String fullName, String identifier, String shahabId, String agentIdentifier, String agentFullName, String agentifierType, String agentShahabId,
                                           String description,
                                           String toIban, String sayadId) throws Exception {
        return callApi(urls.getPichakAccept(), fullName, identifier, shahabId, agentIdentifier, agentFullName, agentifierType, agentShahabId,
                description, toIban, sayadId);
    }

    private Map<String, Object> callApi(String url, String fullName, String identifier, String shahabId, String agentIdentifier, String agentFullName, String agentifierType, String agentShahabId,
                                        String description, String toIban, String sayadId) throws Exception {

        ConvertDate convertDate = new ConvertDate();
        String acceptDate=convertDate.currentDateShamsi()+convertDate.currentTimeShamsi();
        System.out.println(acceptDate);
        Map<String, Object> acceptorMap = new HashMap<>();
        acceptorMap.put("FullName", fullName);
        acceptorMap.put("Identifier", identifier);
        acceptorMap.put("IdentifierType", 1);
        acceptorMap.put("ShahabId", shahabId);

        Map<String, Object> acceptorAgentMap = new HashMap<>();
        acceptorAgentMap.put("Identifier", agentIdentifier);
        acceptorAgentMap.put("FullName", agentFullName);
        acceptorAgentMap.put("IdentifierType", 1);
        acceptorAgentMap.put("ShahabId", agentShahabId);

        Map<String, Object> chequeIssueMap = new HashMap<>();
        chequeIssueMap.put("SayadId", sayadId);
        chequeIssueMap.put("Description", description);
        chequeIssueMap.put("AcceptDate", acceptDate);
        chequeIssueMap.put("Accept", 1);
        chequeIssueMap.put("Acceptor", acceptorMap);
        chequeIssueMap.put("AcceptorAgent", acceptorAgentMap);

        Map<String, Object> requestMap = new HashMap<>();
        requestMap.put("Channel", 1);
        requestMap.put("CustomerNumber", "1");
        requestMap.put("AuthStatus", 3);
        requestMap.put("UserName", "");
        requestMap.put("BranchCode", 0);
        requestMap.put("RequestInfo", chequeIssueMap);

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

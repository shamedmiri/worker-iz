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
    public Map<String, Object> callUserApi(String sayadId, String serialNumber, String userIdentification, String amount, String dueDate, String description,
                                           String issuerIbanNumber, String receiverNationalCode, String receiversNameString
    ) throws Exception {
        return callApi(urls.getSaveChequeTransferStatus(), sayadId, serialNumber, userIdentification, amount, dueDate, description, issuerIbanNumber,
                receiverNationalCode, receiversNameString);
    }
    private Map<String, Object> callApi(String url, String sayadId, String serialNumber, String userIdentification, String amount, String dueDate, String description, String issuerIbanNumber,String receiverNationalCode, String receiversNameString) throws Exception {
        Map<String, Object> requestMap = new HashMap<>();
        requestMap.put("SayadId", sayadId);
        requestMap.put("SerialNumber", serialNumber);
        requestMap.put("UserIdentification", userIdentification);
        requestMap.put("Amount", amount);
        requestMap.put("DueDate", dueDate);
        requestMap.put("Description", description);
        requestMap.put("ChannelId", 1);
        requestMap.put("BranchCode", 101);
        requestMap.put("BankId", "69");
        requestMap.put("IssuerIbanNumber", issuerIbanNumber);
        requestMap.put("State", "4");
        requestMap.put("ReceiverNationalCode", receiverNationalCode);
        requestMap.put("ReceiverName", receiversNameString);
        requestMap.put("DefinitionId", "");
        requestMap.put("InstanceId", "");
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

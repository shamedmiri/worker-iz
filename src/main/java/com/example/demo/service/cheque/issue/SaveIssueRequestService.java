package com.example.demo.service.cheque.issue;

import com.example.demo.config.ApiUrlsProperties;
import com.example.demo.error.ErrorMessagesProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class SaveIssueRequestService {

    @Autowired
    private ErrorMessagesProperties errorMessages;

    private final HttpClient httpClient;
    private final ApiUrlsProperties urls;
    private final ObjectMapper objectMapper;

    public SaveIssueRequestService(HttpClient httpClient, ApiUrlsProperties urls) {
        this.httpClient = httpClient;
        this.urls = urls;
        this.objectMapper = new ObjectMapper();
    }

    public Map<String, Object> callUserApi(String fullName,ArrayList<String> receivers, String identifier, String shahabId, String dueDate,
                                           String sayadId, String serial, String seriesNo, String fromIban
            , String amount, String saveDescription, String reasonCode, String customerNumber
            , String MSSP_TransID) throws Exception {
        return callApi(urls.getSaveIssueRequest(), fullName, receivers, identifier, shahabId, dueDate, sayadId, serial, seriesNo
                , fromIban, amount, saveDescription, reasonCode, customerNumber, MSSP_TransID);
    }

    private Map<String, Object> callApi(String url, String fullName,
                                        ArrayList<String> receivers, String identifier, String shahabId, String dueDate,
                                        String sayadId, String serial, String seriesNo, String fromIban
            , String amount, String saveDescription, String reasonCode, String customerNumber
            , String MSSP_TransID) throws Exception {
        Map<String, Object> requestBody = buildRequestBody(fullName, receivers, identifier, shahabId, dueDate,
                sayadId, serial, seriesNo, fromIban, amount, saveDescription, reasonCode, customerNumber, MSSP_TransID);
        String jsonRequest = objectMapper.writeValueAsString(requestBody);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Accept", "application/json")
                .header("Content-Type", "application/json")
                .PUT(HttpRequest.BodyPublishers.ofString(jsonRequest))
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

    private Map<String, Object> buildRequestBody(String fullName,
                                                 ArrayList<String> receivers, String identifier, String shahabId, String dueDate,
                                                 String sayadId, String serial, String seriesNo, String fromIban
            , String amount, String saveDescription, String reasonCode, String customerNumber
            , String MSSP_TransID) throws Exception {


        Map<String, Object> data = new HashMap<>();
        ArrayList<Object> accountOwners = new ArrayList<>();
        Map<String, Object> accountOwner = new HashMap<>();
        accountOwner.put("FullName", fullName);
        accountOwner.put("Identifier", identifier);
        accountOwner.put("IdentifierType", 1);
        accountOwner.put("ShahabId", shahabId);
        accountOwners.add(accountOwner);
        data.put("AccountOwners", accountOwners);
        JSONArray array = new JSONArray(receivers);
        ArrayList<Object> receiversArr = new ArrayList<>();
        for (var j = 0; j < array.length(); j++) {
            JSONObject item = array.getJSONObject(j);
            Map<String, Object> receiver = new HashMap();
            receiver.put("FullName", item.get("ReceiverName"));
            receiver.put("Identifier", item.get("Identifier"));
            receiver.put("IdentifierType", 1);
            receiver.put("ShahabId", "");
            receiversArr.add(receiver);
        }
        data.put("Receivers", receiversArr);
        ArrayList<Object> signers = new ArrayList<>();
        Map<String, Object> signerMap = new HashMap<>();
        Map<String, Object> signer = new HashMap<>();
        signer.put("FullName", fullName);
        signer.put("Identifier", identifier);
        signer.put("IdentifierType", 1);
        signer.put("ShahabId", shahabId);
        signerMap.put("Signer", signer);
        signerMap.put("SignGrantor", null);
        signerMap.put("LegalStamp", 0);
        signers.add(signerMap);
        data.put("Signers", signers);
        // Other fields
        var DueDate2 = dueDate.toString().replaceAll("/", "");
        data.put("SayadId", sayadId);
        data.put("SerialNumber", serial);
        data.put("SeriesNumber", seriesNo);
        data.put("FromIban", fromIban);
        data.put("Amount", amount);
        data.put("DueDate", DueDate2);
        data.put("Description", saveDescription);
        data.put("ToIban", "");
        data.put("BankCode", "69");
        data.put("BranchCode", "6900000");
        data.put("ChequeType", 1);
        data.put("ChequeMedia", 1);
        data.put("Currency", "1");
        data.put("Reason", reasonCode);
        Map<String, Object> headers = new HashMap<>();
        headers.put("CustomerNumber", customerNumber);
        headers.put("Channel", 1);
        headers.put("BranchCode", "6900000");
        headers.put("UserName", "");
        headers.put("AuthStatus", 1);
        data.put("Headers", headers);
        data.put("TransactionId", MSSP_TransID);
        System.out.println(data);
        return data;
    }


}
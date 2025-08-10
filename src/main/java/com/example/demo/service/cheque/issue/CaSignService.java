package com.example.demo.service.cheque.issue;

import com.example.demo.config.ApiUrlsProperties;
import com.example.demo.error.ErrorMessagesProperties;
import com.example.demo.utils.ConvertDate;
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
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class CaSignService {

    @Autowired
    private ErrorMessagesProperties errorMessages;

    private final HttpClient httpClient;
    private final ApiUrlsProperties urls;
    private final ObjectMapper objectMapper;

    public CaSignService(HttpClient httpClient, ApiUrlsProperties urls) {
        this.httpClient = httpClient;
        this.urls = urls;
        this.objectMapper = new ObjectMapper();
    }

    public Map<String, Object> callUserApi(String nationalCode,
                                           ArrayList<String> receivers, String sayadId, String DueDate2, String amount,
                                           String saveDescription, String shahabIdReciever) throws Exception {
        return callApi(urls.getCaSign(), nationalCode, receivers, sayadId, DueDate2, amount, saveDescription, shahabIdReciever);
    }

    private Map<String, Object> callApi(String url, String nationalCode,
                                        ArrayList<String> receivers, String sayadId, String DueDate2, String amount,
                                        String saveDescription, String shahabIdReciever) throws Exception {
        Map<String, Object> requestBody = buildRequestBody(nationalCode, receivers, sayadId, DueDate2, amount,
                saveDescription, shahabIdReciever);
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

    private Map<String, Object> buildRequestBody(String nationalCode,
                                                 ArrayList<String > receivers, String sayadId, String DueDate2, String amount,
                                                 String saveDescription, String shahabIdReciever) throws Exception {
//        ConvertDate convertDate = new ConvertDate();
//        String requestDate = convertDate.currentDateShamsi();
//        OffsetDateTime tehranTime = OffsetDateTime.now(ZoneOffset.ofHoursMinutes(3, 30));
//        String formatted = tehranTime.format(DateTimeFormatter.ofPattern("HHmmss"));
//        String requestDateTime = requestDate + formatted + "00";
        var date = new Date();
        var formatter = new SimpleDateFormat("YYYYMMddHHmmss");
        var timeSTR = formatter.format(date) + "00";
        Map<String, Object> requestHamoon = new HashMap<>();

        requestHamoon.put("MajorVersion", 1);
        requestHamoon.put("MinorVersion", 1);
        requestHamoon.put("MessagingMode", "asynchClientServer");

        Map<String, Object> AppInfo = new HashMap<>();
        AppInfo.put("AP_ID", "http://www.ap-irza-fraz.ir");
        AppInfo.put("AP_TransID", "FRAZ" + timeSTR);
        AppInfo.put("AP_URL", "http://www.ap-irza-fraz.ir");
        AppInfo.put("AP_REFERRER", "IRZA");
        requestHamoon.put("AP_Info", AppInfo);

        Map<String, Object> dnsName = new HashMap<>();
        dnsName.put("DNSName", "mss.isc.co.ir");
        Map<String, Object> msspId = new HashMap<>();
        msspId.put("MSSP_ID", dnsName);
        requestHamoon.put("MSSP_Info", msspId);

        Map<String, Object> userIdentifier = new HashMap<>();
        userIdentifier.put("owner", nationalCode);
        userIdentifier.put("holder", nationalCode);
        Map<String, Object> mobileUser = new HashMap<>();
        mobileUser.put("UserIdentifier", userIdentifier);
        requestHamoon.put("MobileUser", mobileUser);
        String encoded = java.util.Base64.getEncoder().encodeToString(saveDescription.getBytes("UTF-8"));
        JSONArray array = new JSONArray(receivers);
        String IdentifierRec = "";
        int IdentifierTypeRec = 0;
        for (int i = 0; i < array.length(); i++) {
            JSONObject item = array.getJSONObject(i);
            IdentifierRec = item.getString("Identifier");
        }
        String TBS = sayadId + "|" + DueDate2 + "|" + amount + "|" + encoded + "|" + IdentifierRec + "_" + IdentifierTypeRec + "_" + shahabIdReciever + "||";
        String str1 = new String(TBS);
        String encoded1 = java.util.Base64.getEncoder().encodeToString(str1.getBytes("UTF-8"));
        Map<String, Object> dataToBeSigned = new HashMap<>();
        dataToBeSigned.put("MimeType", "plain/text");
        dataToBeSigned.put("Encoding", "base64");
        dataToBeSigned.put("Value", encoded1);
        requestHamoon.put("DataToBeSigned", dataToBeSigned);


        Map<String, Object> dataToBeDisplayed = new HashMap<>();
        dataToBeDisplayed.put("MimeType", "plain/text");
        dataToBeDisplayed.put("Encoding", "UTF-8");
        String tex1 = "ثبت صدور چک الکترونیکی" + " \n";
        String tex2 = "حساب: " + "deposit" + " \n";
        String tex3 = "شناسه صیادی: " + sayadId + " \n";
        String tex4 = " تاریخ سررسید: " + "dueDate" + " \n";
        String tex5 = " مبلغ : " + amount + " \n";
        String txt = tex1 + tex2 + tex3 + tex4 + tex5;
        dataToBeDisplayed.put("Value", txt);
        requestHamoon.put("DataToBeDisplayed", dataToBeDisplayed);
        Map<String, Object> signatureProfile = new HashMap<>();
        signatureProfile.put("mssURI", "http://mss.isc.co.ir/v1.0/CMS-ATT/NRS-SHA256/DGS");
        requestHamoon.put("SignatureProfile", signatureProfile);
        return requestHamoon;
    }


}
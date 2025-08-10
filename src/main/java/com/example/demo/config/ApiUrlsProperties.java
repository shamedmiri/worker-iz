package com.example.demo.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "api.urls")
public class ApiUrlsProperties {
    private String transfersChain;
    private String deactivateHamoon;
    private String getCustomerInfo;
    private String chequeHolderInquiry;
    private String chequeIssuerInquiry;
    private String getCustomerRelations;
    private String GetCustomerByNationalCodeFromReport;
    private String transferCheque;
    private String receiverInquiryCheque;
    private String issuedChequeKartabl;
    private String receiverChequeKartabl;
    private String pichakAccept;
    private String caStatus;
    private String GetChequeInquiry;
    private String caSign;
    private String saveIssueRequest;

    public String getSaveIssueRequest() {
        return saveIssueRequest;
    }

    public void setSaveIssueRequest(String saveIssueRequest) {
        this.saveIssueRequest = saveIssueRequest;
    }

    public String getCaSign() {
        return caSign;
    }

    public void setCaSign(String caSign) {
        this.caSign = caSign;
    }

    public String getGetChequeInquiry() {
        return GetChequeInquiry;
    }

    public void setGetChequeInquiry(String getChequeInquiry) {
        GetChequeInquiry = getChequeInquiry;
    }


    public String getCaStatus() {
        return caStatus;
    }

    public void setCaStatus(String caStatus) {
        this.caStatus = caStatus;
    }

    public String getPichakAccept() {
        return pichakAccept;
    }

    public void setPichakAccept(String pichakAccept) {
        this.pichakAccept = pichakAccept;
    }


    public String getIssuedChequeKartabl() {
        return issuedChequeKartabl;
    }

    public String getReceiverChequeKartabl() {
        return receiverChequeKartabl;
    }

    public void setReceiverChequeKartabl(String receiverChequeKartabl) {
        this.receiverChequeKartabl = receiverChequeKartabl;
    }

    public void setIssuedChequeKartabl(String issuedChequeKartabl) {
        this.issuedChequeKartabl = issuedChequeKartabl;
    }

    public String getOwnerAcceptChequeStatus() {
        return ownerAcceptChequeStatus;
    }

    public void setOwnerAcceptChequeStatus(String ownerAcceptChequeStatus) {
        this.ownerAcceptChequeStatus = ownerAcceptChequeStatus;
    }

    private String ownerAcceptChequeStatus;

    public String getSaveChequeTransferStatus() {
        return saveChequeTransferStatus;
    }

    public void setSaveChequeTransferStatus(String saveChequeTransferStatus) {
        this.saveChequeTransferStatus = saveChequeTransferStatus;
    }

    private String saveChequeTransferStatus;

    public String getReceiverInquiryCheque() {
        return receiverInquiryCheque;
    }

    public void setReceiverInquiryCheque(String receiverInquiryCheque) {
        this.receiverInquiryCheque = receiverInquiryCheque;
    }

    public String getTransferCheque() {
        return transferCheque;
    }

    public void setTransferCheque(String transferCheque) {
        this.transferCheque = transferCheque;
    }

    public String getGetCustomerByNationalCodeFromReport() {
        return GetCustomerByNationalCodeFromReport;
    }

    public void setGetCustomerByNationalCodeFromReport(String getCustomerByNationalCodeFromReport) {
        GetCustomerByNationalCodeFromReport = getCustomerByNationalCodeFromReport;
    }

    public String getGetCustomerRelations() {
        return getCustomerRelations;
    }

    public void setGetCustomerRelations(String getCustomerRelations) {
        this.getCustomerRelations = getCustomerRelations;
    }

    public String getChequeIssuerInquiry() {
        return chequeIssuerInquiry;
    }

    public void setChequeIssuerInquiry(String chequeIssuerInquiry) {
        this.chequeIssuerInquiry = chequeIssuerInquiry;
    }

    public String getChequeHolderInquiry() {
        return chequeHolderInquiry;
    }

    public void setChequeHolderInquiry(String chequeHolderInquiry) {
        this.chequeHolderInquiry = chequeHolderInquiry;
    }

    public String getTransfersChain() {
        return transfersChain;
    }

    public void setTransfersChain(String transfersChain) {
        this.transfersChain = transfersChain;
    }

    public String getDeactivateHamoon() {
        return deactivateHamoon;
    }

    public void setDeactivateHamoon(String deactivateHamoon) {
        this.deactivateHamoon = deactivateHamoon;
    }

    public String getGetCustomerInfo() {
        return getCustomerInfo;
    }

    public void setGetCustomerInfo(String getCustomerInfo) {
        this.getCustomerInfo = getCustomerInfo;
    }

    private String camunda;

    public String getCamunda() {
        return camunda;
    }

    public void setCamunda(String camunda) {
        this.camunda = camunda;
    }

}

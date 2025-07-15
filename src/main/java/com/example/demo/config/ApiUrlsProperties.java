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

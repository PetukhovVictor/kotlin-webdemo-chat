package com.jetbrains.dto;

public class ErrorDTO {
    private String status = "ERROR";
    private Integer errorCode;
    private String errorName;
    private String errorDescription;

    public Integer getErrorCode() {
        return errorCode;
    }

    public String getErrorDescription() {
        return errorDescription;
    }

    public String getErrorName() {
        return errorName;
    }

    public void setErrorCode(Integer code) {
        this.errorCode = code;
    }

    public void setErrorDescription(String description) {
        this.errorDescription = description;
    }

    public void setErrorName(String name) {
        this.errorName = name;
    }
}

package com.jetbrains.dto;

/**
 * DTO-модель серверной ошибки. Отдается на клиент.
 */
public class ErrorDTO {
    /**
     * Поле статуса отдаваемого JSON-объекта.
     */
    private String status = "ERROR";

    /**
     * Внутренний код ошибки.
     */
    private Integer errorCode;

    /**
     * Внутреннее название ошибки.
     */
    private String errorName;

    /**
     * Внутреннее описание ошибки.
     */
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

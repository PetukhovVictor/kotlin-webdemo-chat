package com.jetbrains.web.errors;

/**
 * Перечень возможных ошибок, передаваемый пользователю.
 * Описание ошибок находится в ресурсах, в файле responseErrors.json.
 */
public enum ResponseErrors {
    NOT_AUTHORIZED(1000, 401),
    USER_NOT_FOUND(1001, 400),

    SEARCH_PHRASE_NOT_SPECIFIED(2001, 400),
    INCORRECT_INTERLOCUTOR_ID(2002, 400),
    INCORRECT_DIALOG_ID(2003, 400),
    DIALOG_DOES_NOT_EXIST(2004, 400),
    MESSAGE_NOT_SPECIFIED(2005, 400),
    DIALOG_NOT_SPECIFIED(2006, 400);

    /**
     * Внутренний error-код ошибки.
     */
    private final Integer errorCode;

    /**
     * HTTP код ошибки.
     */
    private final Integer httpCode;

    ResponseErrors(Integer errorCode, Integer httpCode) {
        this.errorCode = errorCode;
        this.httpCode = httpCode;
    }

    public int getErrorCode() {
        return errorCode;
    }

    public int getHttpCode() {
        return httpCode;
    }
}

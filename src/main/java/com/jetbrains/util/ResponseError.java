package com.jetbrains.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jetbrains.AppConfig;
import com.jetbrains.dto.ErrorDTO;
import com.jetbrains.web.errors.ResponseErrors;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.springframework.core.io.ClassPathResource;

import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.InputStreamReader;

public class ResponseError {
    private static JSONObject errors;

    private ResponseErrors errorCode;

    private HttpServletResponse response;

    static {
        try {
            BufferedReader bufferedReader = new BufferedReader(
                    new InputStreamReader(
                            new ClassPathResource(AppConfig.RESPONSE_ERRORS_FILE).getInputStream()));
            errors = (JSONObject) new JSONParser().parse(bufferedReader);
        } catch (Exception e) {
            errors = null;
        }
    }

    public ResponseError(Enum errorCode) {
        this.errorCode = (ResponseErrors) errorCode;
    }

    public ResponseError(Enum errorCode, HttpServletResponse response) {
        this.errorCode = (ResponseErrors) errorCode;
        this.response = response;
    }

    public String toString() {
        ErrorDTO error = new ErrorDTO();
        Integer errorCode = this.errorCode.getErrorCode();
        Integer httpCode = this.errorCode.getHttpCode();
        error.setErrorCode(errorCode);
        error.setErrorName(this.errorCode.name());
        error.setErrorDescription((String) ((JSONObject)errors.get(Integer.toString(errorCode))).get("description"));
        this.response.setStatus(httpCode);
        String jsonResult;
        try {
            jsonResult = new ObjectMapper().writeValueAsString(error);
        } catch (JsonProcessingException e) {
            jsonResult = null;
        }
        return jsonResult;
    }
}

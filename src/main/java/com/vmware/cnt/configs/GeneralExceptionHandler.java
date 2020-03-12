package com.vmware.cnt.configs;

import feign.FeignException;
import org.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;

import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

@RestControllerAdvice
public class GeneralExceptionHandler {

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Map<String, Object> handleException(Exception e) {
        e.printStackTrace();
        return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, e.getLocalizedMessage());
    }

    @ExceptionHandler({NoHandlerFoundException.class, NoSuchElementException.class})
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Map<String, Object> handleNoHandlerFound(NoHandlerFoundException e) {
        e.printStackTrace();
        return buildErrorResponse(HttpStatus.NOT_FOUND, e.getLocalizedMessage());
    }

    @ExceptionHandler({FeignException.BadRequest.class, FeignException.FeignClientException.class})
    public Map<String, Object> handleFeignStatusException(FeignException e, HttpServletResponse response) {
        e.printStackTrace();
        final int status = e.status();
        response.setStatus(status);
        final HttpStatus httpStatus = HttpStatus.valueOf(status);
        return new JSONObject(e.contentUTF8()).put("status", httpStatus.name()).toMap();
    }

    public static Map<String, Object> buildErrorResponse(HttpStatus status, String localizedMessage) {
        Map<String, Object> response = new HashMap<>();
        response.put("status", status.name());
        response.put("message", localizedMessage);
        return response;
    }
}
package com.altendin.peln.payloads.errors;

public class GenericError {

    private String path;
    private String error;
    private String message;
    private Integer status;

    public GenericError(String path, String error, String message, Integer status) {
        this.path = path;
        this.error = error;
        this.message = message;
        this.status = status;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }
}

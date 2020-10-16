
package com.airtel.xstreamfiber.errors;

import com.squareup.moshi.Json;


public class ErrorResponse {
    @Json(name = "resultDescription")
    private String resultDescription;
    @Json(name = "resultCode")
    private String resultCode;

    public String getMessage() {
        return resultDescription;
    }

    public void setMessage(String message) {
        this.resultDescription = message;
    }

    public String getCode() {
        return resultCode;
    }

    public void setCode(String code) {
        this.resultCode = code;
    }
}

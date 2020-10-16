
package com.airtel.xstreamfiber.errors;


public interface HttpStatus {
    int BAD_REQUEST = 400;
    int AUTHORIZATION = 401;
    int FORBIDDEN = 403;
    int NOT_FOUND = 404;
    int SERVER_ERROR = 500;
    int NO_CONTENT = 204;
    int TNC_NOT_ACCEPTED = 412;
}

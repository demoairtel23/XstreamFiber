
package com.airtel.xstreamfiber.errors;

import com.airtel.xstreamfiber.exception.NoNetworkException;
import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;

import java.io.IOException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

import javax.inject.Inject;

import okhttp3.ResponseBody;
import retrofit2.HttpException;

import static com.airtel.xstreamfiber.errors.HttpStatus.AUTHORIZATION;
import static com.airtel.xstreamfiber.errors.HttpStatus.BAD_REQUEST;
import static com.airtel.xstreamfiber.errors.HttpStatus.FORBIDDEN;
import static com.airtel.xstreamfiber.errors.HttpStatus.NOT_FOUND;
import static com.airtel.xstreamfiber.errors.HttpStatus.SERVER_ERROR;
import static com.airtel.xstreamfiber.errors.HttpStatus.TNC_NOT_ACCEPTED;


public class ErrorHandler {
    private final ErrorProvider mErrorProvider;

    @Inject
    public ErrorHandler(final ErrorProvider errorProvider) {
        this.mErrorProvider = errorProvider;
    }

    public Error getErrorMessage(Throwable throwable) {
        Error error = new Error();
        if (throwable instanceof NoNetworkException) {
            error.setMessage(mErrorProvider.getMessage(ErrorType.NO_INTERNET));
        } else if (throwable instanceof SocketException || throwable instanceof SocketTimeoutException) {
            error.setMessage(mErrorProvider.getMessage(ErrorType.SOCKET_TIMEOUT));
        } else if (throwable instanceof UnknownHostException) {
            error.setMessage(mErrorProvider.getMessage(ErrorType.HTTP_NOT_FOUND));
        } else if (throwable instanceof HttpException) {
            // non-2XX http error
            int errorCodes = ((HttpException) throwable).code();
            switch (errorCodes) {
                case BAD_REQUEST:
                    error.setMessage(mErrorProvider.getMessage(ErrorType.HTTP_BAD_REQUEST));
                    break;
                case AUTHORIZATION:
                    error.setMessage(mErrorProvider.getMessage(ErrorType.HTTP_AUTHORIZATION));
                    break;
                case FORBIDDEN:
                    error.setMessage(mErrorProvider.getMessage(ErrorType.HTTP_FORBIDDEN));
                    break;
                case NOT_FOUND:
                    error.setMessage(mErrorProvider.getMessage(ErrorType.HTTP_NOT_FOUND));
                    break;
                case SERVER_ERROR:
                    error.setMessage(mErrorProvider.getMessage(ErrorType.HTTP_SERVER_ERROR));
                    break;
                case TNC_NOT_ACCEPTED:
                    error.setMessage(mErrorProvider.getMessage(ErrorType.TNC_NOT_ACCEPTED));
                    break;
                default:
                    error.setMessage(mErrorProvider.getMessage(ErrorType.HTTP_SERVER_ERROR));
                    break;
            }
            error.setCode(errorCodes);
        }
        return error;
    }

    public ErrorResponse getErrorResponse(Throwable throwable) {
        ErrorResponse errorResponse = new ErrorResponse();

        Error error = getErrorMessage(throwable);

        errorResponse.setCode(String.valueOf(error.getCode()));
        errorResponse.setMessage(error.getMessage());

        if (error.getCode() == BAD_REQUEST) {
            if (((HttpException) throwable).response() != null) {
                ResponseBody responseBody = ((HttpException) throwable).response().errorBody();
                if (responseBody != null) {
                    final Moshi moshi = new Moshi.Builder().build();
                    final JsonAdapter<ErrorResponse> adapter = moshi.adapter(ErrorResponse.class);
                    try {
                        errorResponse = adapter.fromJson(responseBody.string());
                    } catch (IOException e) {
                        // Do nothing as we handled this case already
                    }
                }
            }
        }
        return errorResponse;
    }

    public String getUnknownError() {
        return mErrorProvider.getMessage(ErrorType.UNKNOWN);
    }

    public String getEmptyListError() {
        return mErrorProvider.getMessage(ErrorType.EMPTY_LIST);
    }

    public boolean isSocketException(Throwable throwable) {
        return (throwable instanceof SocketException || throwable instanceof SocketTimeoutException);
    }
}

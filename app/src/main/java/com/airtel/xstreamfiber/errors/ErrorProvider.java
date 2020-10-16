
package com.airtel.xstreamfiber.errors;

import android.content.Context;

import com.airtel.xstreamfiber.R;

import javax.inject.Inject;


public class ErrorProvider {

    private final Context context;

    @Inject
    public ErrorProvider(final Context context) {
        this.context = context;
    }

    public String getMessage(final ErrorType messageType) {
        switch (messageType) {
            case GENERIC:
                return context.getString(R.string.error_generic);
            case SOCKET_TIMEOUT:
                return context.getString(R.string.error_socket_timeout);
            case SSL_ERROR:
                return context.getString(R.string.error_ssl_error);
            case HTTP_GENERIC:
                return context.getString(R.string.error_http_generic);
            case HTTP_FORBIDDEN:
                return context.getString(R.string.error_http_forbidden);
            case HTTP_BAD_REQUEST:
                return context.getString(R.string.error_http_bad_request);
            case HTTP_AUTHORIZATION:
                return context.getString(R.string.error_http_authorization);
            case HTTP_SERVER_ERROR:
                return context.getString(R.string.error_server_error);
            case HTTP_NOT_FOUND:
                return context.getString(R.string.error_not_found);
            case NO_INTERNET:
                return context.getString(R.string.error_no_network);
            case HTTP_NO_CONTENT:
                return context.getString(R.string.error_no_content);
            case UNKNOWN:
                return context.getString(R.string.error_unknown);
            case USERNAME_TAKEN:
                return context.getString(R.string.error_username_taken);
            case PROFILE_WRONG_CREDS:
                return context.getString(R.string.error_message_profile_wrong_creds);
            case PROFILE_SOMETHING_WRONG:
                return context.getString(R.string.error_message_profile_something_wrong);
            case APPROVE_IT_REJECTED:
                return context.getString(R.string.error_approve_it_rejected);

            case TNC_NOT_ACCEPTED:
                return context.getString(R.string.error_message_tnc_not_accepted);
            case PASSWORD_DO_NOT_MATCH:
                return context.getString(R.string.error_message_password_do_not_match);
            case PROVIDE_VALID_PASSWORD:
                return context.getString(R.string.error_message_provide_valid_password);
            case CARD_INVALID_PIN:
                return context.getString(R.string.error_message_card_validation_failed);
            case CARD_VALIDATION_FAILED:
                return context.getString(R.string.error_message_invalid_pin_card);
            case WRONG_USER_DETAILS:
                return context.getString(R.string.error_message_wrong_user_details);
            case USERNAME_NOT_FOUND_FORGOT_PASSWORD:
                return context.getString(R.string.error_message_username_not_found_forgot_pass);
            case CARD_LOCKED:
                return context.getString(R.string.error_message_card_locked);
            case CARD_INACTIVE:
                return context.getString(R.string.error_message_card_inactive);
            case CARD_PIN_INVALID_PRIMARY_ACCOUNT_NUMBER:
                return context.getString(R.string.error_message_card_pin_invalid_primary_account_number);
            case CARD_TECHNICAL_ERROR:
                return context.getString(R.string.error_message_card_technical_error);
            case ACCOUNT_LINKING_ERROR:
                return context.getString(R.string.error_linking_accoutns);
            case REPEATED_PASSWORD:
                return context.getString(R.string.error_repeated_password);
            case NULL_BASE_URL:
                return context.getString(R.string.error_null_base_url);
            case WRONG_OTP:
                return context.getString(R.string.error_wrong_otp);
            case ERROR:
                return context.getString(R.string.error);

            default:
                return "";
        }
    }
}

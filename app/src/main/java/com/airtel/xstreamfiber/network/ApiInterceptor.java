package com.airtel.xstreamfiber.network;

import androidx.annotation.NonNull;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class ApiInterceptor implements Interceptor {
    @Override
    public Response intercept(@NonNull final Chain chain) throws IOException {
        final Request original = chain.request();

        Request.Builder requestBuilder = original.newBuilder()
                .header("Content-Type", "application/json");

        Request request = requestBuilder.build();
        return chain.proceed(request);
    }
}

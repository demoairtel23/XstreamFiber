package com.airtel.xstreamfiber.network;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class AuthInterceptor implements Interceptor {
    private String authToken;

    public AuthInterceptor(String token) {
        this.authToken = token;
    }

    @NotNull
    @Override
    public Response intercept(Chain chain) throws IOException {
        Request original = chain.request();

        Request.Builder builder = original.newBuilder()
                .header("Content-Type", "application/json")
                .header("key","64df95454c3f4f0896647a2d01cbef15")
                .header("Authorization", authToken)

                ;


        Request request = builder.build();
        return chain.proceed(request);
    }
}

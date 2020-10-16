package com.airtel.xstreamfiber.Apphelper;

import com.google.gson.GsonBuilder;

import java.util.concurrent.TimeUnit;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Retrofit API factory for Building Retrofit based APIs interfaces
 */

public class ApiFactory {

    private static final int REQUEST_TIME_OUT = 60;
    private Retrofit retrofit;

    public static Retrofit createRetrofitInstance(String baseUrl) {
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);
        OkHttpClient.Builder httpClientBuilder = new OkHttpClient.Builder();
        httpClientBuilder.readTimeout(REQUEST_TIME_OUT, TimeUnit.SECONDS);
        httpClientBuilder.writeTimeout(REQUEST_TIME_OUT, TimeUnit.SECONDS);
        httpClientBuilder.connectTimeout(REQUEST_TIME_OUT, TimeUnit.SECONDS);
        httpClientBuilder.addInterceptor(logging);

        OkHttpClient client = httpClientBuilder.build();
        return new Retrofit.Builder()
                .baseUrl(baseUrl)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create(new GsonBuilder()
                        .setLenient()
                        .create()))
                .build();
    }

    public static RequestBody getRequestBodyFromString(String string) {
        return RequestBody.create(MediaType.parse("text/plain"), string);

    }

}


package com.airtel.xstreamfiber.network;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;


public class NetworkClient {

    private static final String BASE_URL = "https://ltesd.telesonic.in:8443/";
    private static NetworkClient connect;
    private NetworkMonitor networkMonitor;

    public static synchronized NetworkClient getInstance(NetworkMonitor networkMonitor) {

        if (connect == null) {
            connect = new NetworkClient();
            connect.networkMonitor=networkMonitor;

        }
        return connect;
    }


    private ApiInterface clientService;

    public ApiInterface createService() {

        if (clientService == null) {

            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            // set your desired log level
            logging.setLevel(HttpLoggingInterceptor.Level.BODY);

            OkHttpClient.Builder httpClient = new OkHttpClient.Builder();
            httpClient.readTimeout(5, TimeUnit.MINUTES);
            httpClient.connectTimeout(5, TimeUnit.MINUTES);

            httpClient.addInterceptor(new NetworkMonitorInterceptor(networkMonitor));

            // add logging as last interceptor
            if (!httpClient.interceptors().contains(logging))
                httpClient.addInterceptor(logging);

            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                    .client(httpClient.build())
                    .build();

            clientService = retrofit.create(ApiInterface.class);
        }
        return clientService;
    }
}

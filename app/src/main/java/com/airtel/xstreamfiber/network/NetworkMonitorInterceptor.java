package com.airtel.xstreamfiber.network;

import com.airtel.xstreamfiber.exception.NoNetworkException;

import java.io.IOException;

import javax.inject.Inject;

import io.reactivex.annotations.NonNull;
import okhttp3.Interceptor;
import okhttp3.Response;

public class NetworkMonitorInterceptor implements Interceptor {
    private NetworkMonitor mLiveNetworkMonitor;

//    class ServiceInterceptor implements Interceptor{
//
//        String authToken;
//
//        public ServiceInterceptor(String authToken) {
//            this.authToken = authToken;
//        }
//
//        @NonNull
//        @Override
//        public okhttp3.Response intercept(@NonNull Interceptor.Chain chain) throws IOException {
//            Request request = chain.request();
//            if (request.header("No-Authentication") == null){
//                SharedPreferences sharedPref = getSharedPreferences(USER, Context.MODE_PRIVATE);
//                        request = request.newBuilder()
//                        .addHeader("Authorization", "JWT " + sharedPref.getString("auth_token", null))
//                        .build();
//            }
//            return chain.proceed(request);
//        }
//    }

    @Inject
    public NetworkMonitorInterceptor(NetworkMonitor liveNetworkMonitor) {
        this.mLiveNetworkMonitor = liveNetworkMonitor;
    }

    @Override
    public Response intercept(@NonNull final Interceptor.Chain chain) throws IOException {
        if (mLiveNetworkMonitor.isConnected()) {
            return chain.proceed(chain.request());
        } else {
            throw new NoNetworkException();
        }
    }
}

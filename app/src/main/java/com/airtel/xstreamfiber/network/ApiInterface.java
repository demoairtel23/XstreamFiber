package com.airtel.xstreamfiber.network;

import com.airtel.xstreamfiber.network.request.ConnectedDeviceRequest;
import com.airtel.xstreamfiber.network.response.ConnectedDeviceResponse;

import java.util.Map;

import io.reactivex.Observable;
import retrofit2.http.Body;
import retrofit2.http.HeaderMap;
import retrofit2.http.POST;

public interface ApiInterface {

//    @Headers({
//            "Content-Type: application/json",
//            "key: 64df95454c3f4f0896647a2d01cbef15",
//            "Authorization: Basic dGxjMG5uM2N0ZjBydDN3ZGw6dGVsZXNvbmljIyMwMDk4"
//    })
    @POST("v1/connected_device")
    Observable<ConnectedDeviceResponse> networMapApi(@HeaderMap Map<String,String> headers, @Body ConnectedDeviceRequest request);

}

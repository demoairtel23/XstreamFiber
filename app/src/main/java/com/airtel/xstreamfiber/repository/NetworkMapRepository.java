package com.airtel.xstreamfiber.repository;

import com.airtel.xstreamfiber.network.NetworkClient;
import com.airtel.xstreamfiber.network.request.ConnectedDeviceRequest;
import com.airtel.xstreamfiber.network.response.ConnectedDeviceResponse;

import java.util.HashMap;
import java.util.Map;

import io.reactivex.Observable;

public class NetworkMapRepository implements INetworkMapRepository {
    private final NetworkClient mNetworkClient;

    public NetworkMapRepository(NetworkClient networkClient) {
        this.mNetworkClient = networkClient;
    }


    @Override
    public Observable<ConnectedDeviceResponse> networkMap(ConnectedDeviceRequest request,String key) {
        /*  "Content-Type: application/json",
            "key: 64df95454c3f4f0896647a2d01cbef15",
            "Authorization: Basic dGxjMG5uM2N0ZjBydDN3ZGw6dGVsZXNvbmljIyMwMDk4"*/
        Map<String,String> map=new HashMap<String,String>();
        map.put("Content-Type","application/json");
        map.put("key",key);
        map.put("Authorization","Basic dGxjMG5uM2N0ZjBydDN3ZGw6dGVsZXNvbmljIyMwMDk4");
        return mNetworkClient.createService().networMapApi(map,request).map(connectedDeviceResponse -> connectedDeviceResponse);
    }
}


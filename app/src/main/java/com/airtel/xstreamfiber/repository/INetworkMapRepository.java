package com.airtel.xstreamfiber.repository;

import com.airtel.xstreamfiber.network.request.ConnectedDeviceRequest;
import com.airtel.xstreamfiber.network.response.ConnectedDeviceResponse;

import io.reactivex.Observable;

public interface INetworkMapRepository {

    Observable<ConnectedDeviceResponse> networkMap(ConnectedDeviceRequest request, String key);
}

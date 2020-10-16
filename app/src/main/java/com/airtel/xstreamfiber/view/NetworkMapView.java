package com.airtel.xstreamfiber.view;

import com.airtel.xstreamfiber.base.BaseView;
import com.airtel.xstreamfiber.network.response.ConnectedDeviceResponse;

public interface NetworkMapView extends BaseView {
     void showProgress(Boolean b);
     void showError(String msg);
     void handleNetworkMapData(ConnectedDeviceResponse connectedDeviceResponse);
}

package com.airtel.xstreamfiber.controller;

import android.content.Context;
import android.content.SharedPreferences;

import com.airtel.xstreamfiber.Util.Constant;
import com.airtel.xstreamfiber.Util.Constants;
import com.airtel.xstreamfiber.Util.MethodsUtil;
import com.airtel.xstreamfiber.errors.ErrorHandler;
import com.airtel.xstreamfiber.network.request.ConnectedDeviceRequest;
import com.airtel.xstreamfiber.usecase.MapUseCase;
import com.airtel.xstreamfiber.view.NetworkMapView;

import javax.inject.Inject;

import static android.content.Context.MODE_PRIVATE;

public class NetworkMapPresenter extends BaseController<NetworkMapView> implements Constants {

    private final MapUseCase networkMapUseCase;
    private final ErrorHandler errorHandler;
    private String userSerial, apiKey;

    @Inject
    public NetworkMapPresenter(MapUseCase networkMapUseCase, ErrorHandler errorHandler) {
        this.networkMapUseCase = networkMapUseCase;
        this.errorHandler = errorHandler;
    }


    public void callNetworkMapApi(Context context) {
        if (view != null) {
            view.showProgress(true);
        }
        SharedPreferences dduSharedPref = context.getSharedPreferences(Constant.LOGIN_PREF, MODE_PRIVATE);
//        userSerial = dduSharedPref.getString("userSerial", "");
        String userSerialEncrypted = dduSharedPref.getString("userSerial", "");
        userSerial = MethodsUtil.decryptSerial(context, userSerialEncrypted);

        // apiKey = dduSharedPref.getString("apiKey", "");



        ConnectedDeviceRequest connectedDeviceRequest = new ConnectedDeviceRequest();
        connectedDeviceRequest.setSerial(userSerial);

        networkMapUseCase.execute(connectedDeviceRequest).compose(bindToLifecycle())
                .doOnSubscribe(disposable -> {
                    if (view != null) {
                        view.showProgress(true);
                    }
                })
                .doOnTerminate(() -> {
                    if (view != null) {
                        view.showProgress(false);
                    }
                })
                .subscribe(connectedDeviceResponse -> {
                    if(view!=null){
                        view.handleNetworkMapData(connectedDeviceResponse);
                    }

                },this::showError);
    }

    private void showError(final Throwable error) {
        if (view != null)
            view.showError(errorHandler.getErrorMessage(error).getMessage());
    }
}

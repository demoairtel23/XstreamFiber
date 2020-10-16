package com.airtel.xstreamfiber.usecase;

import android.app.Application;
import android.content.SharedPreferences;

import com.airtel.xstreamfiber.Util.Constant;
import com.airtel.xstreamfiber.network.request.ConnectedDeviceRequest;
import com.airtel.xstreamfiber.network.response.ConnectedDeviceResponse;
import com.airtel.xstreamfiber.repository.NetworkMapRepository;
import com.airtel.xstreamfiber.usecase.base.UseCase;
import com.airtel.xstreamfiber.usecase.base.UseCaseComposer;

import javax.inject.Inject;

import io.reactivex.Observable;

import static android.content.Context.MODE_PRIVATE;

public class MapUseCase extends UseCase<ConnectedDeviceRequest, ConnectedDeviceResponse> {
    private final NetworkMapRepository networkMapRepository;
    private String apiKey;
    private Application context;

    @Inject
    protected MapUseCase(UseCaseComposer useCaseComposer, NetworkMapRepository networkMapRepository, Application context) {
        super(useCaseComposer);
        this.networkMapRepository = networkMapRepository;
        this.context = context;
    }


    @Override
    protected Observable<ConnectedDeviceResponse> createUseCaseObservable(ConnectedDeviceRequest param) {

        SharedPreferences dduSharedPref = context.getSharedPreferences(Constant.LOGIN_PREF, MODE_PRIVATE);
        apiKey = dduSharedPref.getString("apiKey", "");

        return networkMapRepository.networkMap(param, apiKey);
    }
}

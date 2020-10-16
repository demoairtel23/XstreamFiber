package com.airtel.xstreamfiber.usecase.base;

import java.util.Observable;
import java.util.concurrent.atomic.AtomicInteger;


public class StateObservable extends Observable {
    public interface ObservaleConstants {
        int HANDLE_NONE = 0;
        int HANDLE_NULL_URL = 1;
        int HANDLE_JWT_EXPIRE = 2;
    }

    private final static StateObservable INSTANCE =  new StateObservable();
    public static StateObservable getInstance() {
        return INSTANCE;
    }
    private StateObservable() {
        // Do nothing
    }
    private AtomicInteger mValue = new AtomicInteger(ObservaleConstants.HANDLE_NONE);

    public int getValue() {
        return mValue.get();
    }

    public void setValue(int value) {
        this.mValue.set(value);
        this.setChanged();
        this.notifyObservers(this.mValue.get());
    }
}
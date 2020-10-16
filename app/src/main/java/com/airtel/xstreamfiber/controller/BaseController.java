package com.airtel.xstreamfiber.controller;

import com.airtel.xstreamfiber.base.BaseView;
import com.airtel.xstreamfiber.rxlifecycle.PresenterEvent;
import com.airtel.xstreamfiber.rxlifecycle.RxLifecyclePresenter;
import com.trello.rxlifecycle2.LifecycleProvider;
import com.trello.rxlifecycle2.LifecycleTransformer;
import com.trello.rxlifecycle2.RxLifecycle;

import io.reactivex.Observable;
import io.reactivex.annotations.NonNull;
import io.reactivex.subjects.BehaviorSubject;

public abstract  class BaseController<V extends BaseView>  implements LifecycleProvider<Integer> {

    private final BehaviorSubject<Integer> lifecycleSubject = BehaviorSubject.create();

    protected V view;

    protected void onBind() {

    }

    protected void onUnbind() {

    }

    public final void bind(V viewToBind) {
        view = viewToBind;
        onBind();
        lifecycleSubject.onNext(PresenterEvent.ATTACH);
    }

    public final void unbind() {
        onUnbind();
        lifecycleSubject.onNext(PresenterEvent.DETACH);
        view = null;
    }

    @Override
    @NonNull
    public final Observable<Integer> lifecycle() {
        return lifecycleSubject.hide();
    }

    @Override
    @NonNull
    public final <T> LifecycleTransformer<T> bindUntilEvent(@NonNull final Integer event) {
        return RxLifecycle.bindUntilEvent(lifecycleSubject, event);
    }

    @Override
    @NonNull
    public final <T> LifecycleTransformer<T> bindToLifecycle() {
        return RxLifecyclePresenter.bindPresenter(lifecycleSubject);
    }

}

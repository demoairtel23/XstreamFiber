
package com.airtel.xstreamfiber.usecase.base;


import io.reactivex.ObservableTransformer;

public interface UseCaseComposer {

    <T> ObservableTransformer<T, T> apply();

}

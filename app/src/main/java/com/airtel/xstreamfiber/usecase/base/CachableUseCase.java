
package com.airtel.xstreamfiber.usecase.base;


import java.util.HashMap;
import java.util.Map;

import io.reactivex.Observable;

public abstract class CachableUseCase<P, R> extends UseCase<P, CachableValue<R>> {

    private static final long CACHE_TIMEOUT = 60000;

    protected Map<P, CachedValue<R>> cache = new HashMap<>();

    public CachableUseCase(final UseCaseComposer useCaseComposer) {
        super(useCaseComposer);
    }

    @Override
    protected final Observable<CachableValue<R>> createUseCaseObservable(final P param) {

        Observable<CachableValue<R>> loadObservable =
                createCachableUseCaseObservable(param)
                        .doOnNext(
                                value -> cache(param, value)
                        )
                        .map(this::mapToLiveValue);
        R cachedValue = getCached(param);

        if (cachedValue == null) {
            return loadObservable;
        } else {
            CachableValue<R> cached = new CachableValue<>(cachedValue, true, true);
            Observable<CachableValue<R>> emmitCached = Observable.defer(() -> Observable.just(cached));
            return Observable.concat(emmitCached, loadObservable);
        }
    }

    public void invalidateCache(final P param) {
        cache.remove(param);
    }

    public void invalidateCache() {
        cache.clear();
    }

    protected abstract Observable<R> createCachableUseCaseObservable(P param);

    protected boolean shouldCache(final P param) {
        return true;
    }

    private CachableValue<R> mapToLiveValue(final R value) {
        return new CachableValue<>(value, false, false);
    }

    private void cache(final P param, final R value) {
        if (shouldCache(param)) {
            cache.put(param, new CachedValue<>(value));
        }
    }

    private R getCached(final P param) {
        CachedValue<R> cached = cache.get(param);
        if (cached == null) {
            return null;
        }
        if (System.currentTimeMillis() - cached.time > CACHE_TIMEOUT) {
            cache.remove(param);
            return null;
        }
        cached.time = System.currentTimeMillis();
        return cached.value;
    }

    protected class CachedValue<T> {

        T value;

        long time;

        public CachedValue(T value) {
            this.value = value;
            this.time = System.currentTimeMillis();
        }
    }
}

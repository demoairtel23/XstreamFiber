
package com.airtel.xstreamfiber.usecase.base;


import com.airtel.xstreamfiber.usecase.VoidUseCase;

import io.reactivex.Observable;

public abstract class VoidCachableUseCase<R> extends VoidUseCase<CachableValue<R>> {

    private long mCacheTimeout = 60000;

    protected CachedValue<R> cache = null;

    public VoidCachableUseCase(final UseCaseComposer useCaseComposer) {
        super(useCaseComposer);
    }

    @Override
    protected final Observable<CachableValue<R>> createUseCaseObservable() {
        R cachedValue = getCached();
        if (cachedValue == null) {
            return createCachableUseCaseObservable()
                    .doOnNext(this::cache)
                    .map(this::mapToLiveValue);
        } else {
            CachableValue<R> cached = new CachableValue<>(cachedValue, true, true);
            return Observable.defer(() -> Observable.just(cached));
        }
    }

    public void invalidateCache() {
        cache = null;
    }

    public long getCacheTimeout() {
        return mCacheTimeout;
    }


    protected abstract Observable<R> createCachableUseCaseObservable();

    protected boolean shouldCache() {
        return true;
    }

    private CachableValue<R> mapToLiveValue(final R value) {
        return new CachableValue<>(value, false, false);
    }

    private void cache(final R value) {
        if (shouldCache()) {
            cache = new CachedValue<>(value);
        }
    }

    private R getCached() {
        if (cache == null) {
            return null;
        }
        if (System.currentTimeMillis() - cache.time > getCacheTimeout()) {
            cache = null;
            return null;
        }
        cache.time = System.currentTimeMillis();
        return cache.value;
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

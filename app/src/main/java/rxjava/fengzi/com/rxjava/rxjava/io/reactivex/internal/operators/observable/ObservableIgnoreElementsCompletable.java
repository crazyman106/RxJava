/**
 * Copyright (c) 2016-present, RxJava Contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See
 * the License for the specific language governing permissions and limitations under the License.
 */

package rxjava.fengzi.com.rxjava.rxjava.io.reactivex.internal.operators.observable;

import io.reactivex.*;
import io.reactivex.disposables.Disposable;
import io.reactivex.internal.fuseable.FuseToObservable;
import io.reactivex.internal.operators.observable.ObservableIgnoreElements;
import io.reactivex.plugins.RxJavaPlugins;

public final class ObservableIgnoreElementsCompletable<T> extends Completable implements FuseToObservable<T> {

    final ObservableSource<T> source;

    public ObservableIgnoreElementsCompletable(ObservableSource<T> source) {
        this.source = source;
    }

    @Override
    public void subscribeActual(final CompletableObserver t) {
        source.subscribe(new IgnoreObservable<T>(t));
    }

    @Override
    public Observable<T> fuseToObservable() {
        return RxJavaPlugins.onAssembly(new ObservableIgnoreElements<T>(source));
    }

    static final class IgnoreObservable<T> implements Observer<T>, Disposable {
        final CompletableObserver downstream;

        Disposable upstream;

        IgnoreObservable(CompletableObserver t) {
            this.downstream = t;
        }

        @Override
        public void onSubscribe(Disposable d) {
            this.upstream = d;
            downstream.onSubscribe(this);
        }

        @Override
        public void onNext(T v) {
            // deliberately ignored
        }

        @Override
        public void onError(Throwable e) {
            downstream.onError(e);
        }

        @Override
        public void onComplete() {
            downstream.onComplete();
        }

        @Override
        public void dispose() {
            upstream.dispose();
        }

        @Override
        public boolean isDisposed() {
            return upstream.isDisposed();
        }
    }

}

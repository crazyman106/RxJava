/**
 * Copyright (c) 2016-present, RxJava Contributors.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See
 * the License for the specific language governing permissions and limitations under the License.
 */
package rxjava.fengzi.com.rxjava.rxjava.io.reactivex.disposables;


import rxjava.fengzi.com.rxjava.rxjava.io.reactivex.annotations.NonNull;

/**
 * A Disposable container that handles a {@link Subscription}.
 */
final class SubscriptionDisposable extends ReferenceDisposable<Subscription> {

    private static final long serialVersionUID = -707001650852963139L;

    SubscriptionDisposable(Subscription value) {
        super(value);
    }

    @Override
    protected void onDisposed(@NonNull Subscription value) {
        value.cancel();
    }
}

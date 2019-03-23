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

package rxjava.fengzi.com.rxjava.rxjava.io.reactivex.internal.subscribers;

import io.reactivex.internal.subscribers.InnerQueuedSubscriber;

/**
 * Interface to allow the InnerQueuedSubscriber to call back a parent
 * with signals.
 *
 * @param <T> the value type
 */
public interface InnerQueuedSubscriberSupport<T> {

    void innerNext(io.reactivex.internal.subscribers.InnerQueuedSubscriber<T> inner, T value);

    void innerError(io.reactivex.internal.subscribers.InnerQueuedSubscriber<T> inner, Throwable e);

    void innerComplete(InnerQueuedSubscriber<T> inner);

    void drain();
}

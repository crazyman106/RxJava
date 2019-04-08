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
package io.reactivex;

import io.reactivex.annotations.*;

/**
 * A functional interface that has a {@code subscribe()} method that receives
 * an instance of an {@link ObservableEmitter} instance that allows pushing
 * events in a cancellation-safe manner.
 *
 * 一个含有subscribe函数的接口,该函数中接受一个ObservableEmitter对象,该emitter对象可以(以安全的可取消方式)推送事件
 *
 * @param <T> the value type pushed
 */
public interface ObservableOnSubscribe<T> {

    /**
     * Called for each Observer that subscribes.
     * @param emitter the safe emitter instance, never null
     * @throws Exception on error
     */
    void subscribe(@NonNull ObservableEmitter<T> emitter) throws Exception;
}


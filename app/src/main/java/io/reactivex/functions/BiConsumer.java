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

package io.reactivex.functions;

/**
 * A functional interface (callback) that accepts two values (of possibly different types).
 * <p>
 * 一个接口回调,接收两个值
 * 消费者:接收两个值
 *
 * @param <T1> the first value type
 * @param <T2> the second value type
 *             <p>
 *             consumer:顾客,消费者
 */
public interface BiConsumer<T1, T2> {

    /**
     * Performs an operation on the given values.
     *
     * @param t1 the first value
     * @param t2 the second value
     * @throws Exception on error
     */
    void accept(T1 t1, T2 t2) throws Exception;
}

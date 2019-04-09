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

import io.reactivex.annotations.NonNull;

/**
 * A functional interface that takes a value and returns another value, possibly with a
 * different type and allows throwing a checked exception.
 * <p>
 * <p>
 * 函数接口，它接受一个值并返回另一个值，可能带有不同类型
 * 用于变换对象
 *
 * @param <T> the input value type
 * @param <R> the output value type
 */
public interface Function<T, R> {
    /**
     * Apply some calculation to the input value and return some other value.
     *
     * @param t the input value
     * @return the output value
     * @throws Exception on error
     */
    R apply(@NonNull T t) throws Exception;
}

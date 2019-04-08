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
 * A functional interface (callback) that consumes a primitive long value.
 *
 * 使用原始长值的函数接口(回调)
 */
public interface LongConsumer {
    /**
     * Consume a primitive long input.
     * @param t the primitive long value
     * @throws Exception on error
     */
    void accept(long t) throws Exception;
}

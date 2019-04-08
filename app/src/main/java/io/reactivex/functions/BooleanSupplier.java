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
 * A functional interface (callback) that returns a boolean value.
 *
 * 返回布尔值的函数接口(回调)
 */
public interface BooleanSupplier {
    /**
     * Returns a boolean value.
     * @return a boolean value
     * @throws Exception on error
     */
    boolean getAsBoolean() throws Exception; // NOPMD
}

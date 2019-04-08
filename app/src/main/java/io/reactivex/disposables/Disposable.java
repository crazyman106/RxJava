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
package io.reactivex.disposables;

/**
 * Represents a disposable resource.
 * <p>
 * Disposable:可任意处理的；可自由使用的；用完即可丢弃的
 */
public interface Disposable {
    /**
     * Dispose the resource, the operation should be idempotent.
     * 将事件通道设置为切断（废弃）状态
     * 调用该函数后,导致下游收不到事件.
     */
    void dispose();

    /**
     * Returns true if this resource has been disposed.
     *
     * @return true if this resource has been disposed
     */
    boolean isDisposed();
}

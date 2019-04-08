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

package io.reactivex.internal.schedulers;

/**
 * Marker interface to indicate blocking is not recommended while running
 * on a Scheduler with a thread type implementing it.
 *
 *
 * 在使用实现阻塞的线程类型的调度程序上运行时，不建议使用标记接口来指示阻塞。
 */
public interface NonBlockingThread {

}

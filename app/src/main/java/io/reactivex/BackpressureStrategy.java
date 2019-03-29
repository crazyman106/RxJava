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

/**
 * Represents the options for applying backpressure to a source sequence.
 * 表示对源序列应用反压力的选项
 * <p>
 * 当上下游处于不同的线程中,通过Observable发射,处理,响应数据流时,如果上游发射数据的速度快与下游接受数据的速度,这样对于没有来得及处理的数据就会造成积压,
 * 这些数据既不会丢失,也不会被垃圾回收机制回收,而是存放在一个异步缓存池中,如果缓存池中的数据一直得不到处理,就会越来越多,最后造成内存溢出,这就是响应式编程的背压问题.
 *
 * 如果上下游处于同一个线程中,则不会出现背压问题,因为下游处理完事件后,上游才会发射新的事件.
 *
 *
 */
public enum BackpressureStrategy {
    /**
     * OnNext events are written without any buffering or dropping.
     * Downstream has to deal with any overflow. overflow:溢出
     * <p>Useful when one applies one of the custom-parameter onBackpressureXXX operators.
     * onBackpressureBuffer
     * <p>
     * onNext事件没有任何缓存或丢弃,全部写入,需要下游通过背压操作符（onBackpressureBuffer()/onBackpressureDrop()/onBackpressureLatest()）指定背压策略
     * (不准确:上游不指定背压策略,不会对发射的数据做缓存或丢弃处理,需要下游去处理)
     */
    MISSING,
    /**
     * Signals a MissingBackpressureException in case the downstream can't keep up.
     * signal:信号,标志,发信号,
     */
    ERROR,
    /**
     * Buffers <em>all</em> onNext values until the downstream consumes it.
     */
    BUFFER,
    /**
     * Drops the most recent onNext value if the downstream can't keep up.
     */
    DROP,
    /**
     * Keeps only the latest onNext value, overwriting any previous value if the
     * downstream can't keep up.
     */
    LATEST
}

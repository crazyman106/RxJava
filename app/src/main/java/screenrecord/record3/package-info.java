/**
 * Copyright (c) 2016-present, RxJava Contributors.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * <p>
 * Base reactive classes: {@link io.reactivex.Flowable},  {@link io.reactivex.Observable},
 * {@link io.reactivex.Single},  {@link io.reactivex.Maybe} and
 * {@link io.reactivex.Completable}; base reactive consumers;
 * other common base interfaces.
 *
 * <p>A library that enables subscribing to and composing asynchronous events and
 * callbacks.</p>
 * <p>The Flowable/Subscriber, Observable/Observer, Single/SingleObserver and
 * Completable/CompletableObserver interfaces and associated operators (in
 * the {@code io.reactivex.internal.operators} package) are inspired by the
 * Reactive Rx library in Microsoft .NET but designed and implemented on
 * the more advanced Reactive-Streams ( http://www.reactivestreams.org ) principles.</p>
 * <p>
 * More information can be found at <a
 * href="http://msdn.microsoft.com/en-us/data/gg577609">http://msdn.microsoft.com/en-us/data/gg577609</a>.
 * </p>
 *
 *
 * <p>Compared with the Microsoft implementation:
 * <ul>
 * <li>Observable == IObservable (base type)</li>
 * <li>Observer == IObserver (event consumer)</li>
 * <li>Disposable == IDisposable (resource/cancellation management)</li>
 * <li>Observable == Observable (factory methods)</li>
 * <li>Flowable == IAsyncEnumerable (backpressure)</li>
 * <li>Subscriber == IAsyncEnumerator</li>
 * </ul>
 * The Single and Completable reactive base types have no equivalent in Rx.NET as of 3.x.
 *
 * <p>Services which intend on exposing data asynchronously and wish
 * to allow reactive processing and composition can implement the
 * {@link io.reactivex.Flowable}, {@link io.reactivex.Observable}, {@link io.reactivex.Single},
 * {@link io.reactivex.Maybe} or {@link io.reactivex.Completable} class which then allow
 * consumers to subscribe to them and receive events.</p>
 * <p>Usage examples can be found on the {@link io.reactivex.Flowable}/{@link io.reactivex.Observable} and {@link org.reactivestreams.Subscriber} classes.</p>
 * <p>
 * Base reactive classes: {@link io.reactivex.Flowable},  {@link io.reactivex.Observable},
 * {@link io.reactivex.Single},  {@link io.reactivex.Maybe} and
 * {@link io.reactivex.Completable}; base reactive consumers;
 * other common base interfaces.
 *
 * <p>A library that enables subscribing to and composing asynchronous events and
 * callbacks.</p>
 * <p>The Flowable/Subscriber, Observable/Observer, Single/SingleObserver and
 * Completable/CompletableObserver interfaces and associated operators (in
 * the {@code io.reactivex.internal.operators} package) are inspired by the
 * Reactive Rx library in Microsoft .NET but designed and implemented on
 * the more advanced Reactive-Streams ( http://www.reactivestreams.org ) principles.</p>
 * <p>
 * More information can be found at <a
 * href="http://msdn.microsoft.com/en-us/data/gg577609">http://msdn.microsoft.com/en-us/data/gg577609</a>.
 * </p>
 *
 *
 * <p>Compared with the Microsoft implementation:
 * <ul>
 * <li>Observable == IObservable (base type)</li>
 * <li>Observer == IObserver (event consumer)</li>
 * <li>Disposable == IDisposable (resource/cancellation management)</li>
 * <li>Observable == Observable (factory methods)</li>
 * <li>Flowable == IAsyncEnumerable (backpressure)</li>
 * <li>Subscriber == IAsyncEnumerator</li>
 * </ul>
 * The Single and Completable reactive base types have no equivalent in Rx.NET as of 3.x.
 *
 * <p>Services which intend on exposing data asynchronously and wish
 * to allow reactive processing and composition can implement the
 * {@link io.reactivex.Flowable}, {@link io.reactivex.Observable}, {@link io.reactivex.Single},
 * {@link io.reactivex.Maybe} or {@link io.reactivex.Completable} class which then allow
 * consumers to subscribe to them and receive events.</p>
 * <p>Usage examples can be found on the {@link io.reactivex.Flowable}/{@link io.reactivex.Observable} and {@link org.reactivestreams.Subscriber} classes.</p>
 */
/**
 * Base reactive classes: {@link io.reactivex.Flowable},  {@link io.reactivex.Observable},
 * {@link io.reactivex.Single},  {@link io.reactivex.Maybe} and
 *  {@link io.reactivex.Completable}; base reactive consumers;
 * other common base interfaces.
 *
 * <p>A library that enables subscribing to and composing asynchronous events and
 * callbacks.</p>
 * <p>The Flowable/Subscriber, Observable/Observer, Single/SingleObserver and
 * Completable/CompletableObserver interfaces and associated operators (in
 * the {@code io.reactivex.internal.operators} package) are inspired by the
 * Reactive Rx library in Microsoft .NET but designed and implemented on
 * the more advanced Reactive-Streams ( http://www.reactivestreams.org ) principles.</p>
 * <p>
 * More information can be found at <a
 * href="http://msdn.microsoft.com/en-us/data/gg577609">http://msdn.microsoft.com/en-us/data/gg577609</a>.
 * </p>
 *
 *
 * <p>Compared with the Microsoft implementation:
 * <ul>
 * <li>Observable == IObservable (base type)</li>
 * <li>Observer == IObserver (event consumer)</li>
 * <li>Disposable == IDisposable (resource/cancellation management)</li>
 * <li>Observable == Observable (factory methods)</li>
 * <li>Flowable == IAsyncEnumerable (backpressure)</li>
 * <li>Subscriber == IAsyncEnumerator</li>
 * </ul>
 * The Single and Completable reactive base types have no equivalent in Rx.NET as of 3.x.
 *
 * <p>Services which intend on exposing data asynchronously and wish
 * to allow reactive processing and composition can implement the
 * {@link io.reactivex.Flowable}, {@link io.reactivex.Observable}, {@link io.reactivex.Single},
 * {@link io.reactivex.Maybe} or {@link io.reactivex.Completable} class which then allow
 * consumers to subscribe to them and receive events.</p>
 * <p>Usage examples can be found on the {@link io.reactivex.Flowable}/{@link io.reactivex.Observable} and {@link org.reactivestreams.Subscriber} classes.</p>
 */
package screenrecord.record3;


// MediaCodec与MediaMuxer


/**
 * MediaCodec提供对音视频压缩编码和解码功能，MediaMuxer可以将音视频混合生成多媒体文件，生成MP4文件。
 *
 * 与MediaRecorder类似，都需要先通过MediaProjectionManager获取录屏权限，在回调中进行屏幕数据处理。
 */

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
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Cancellable;

/**
 * Abstraction over an RxJava {@link Observer} that allows associating
 * a resource with it.
 * <p>
 * The {@link #onNext(Object)}, {@link #onError(Throwable)}, {@link #tryOnError(Throwable)}
 * and {@link #onComplete()} methods should be called in a sequential manner(顺序的), just like the
 * {@link Observer}'s methods should be.
 * Use the {@code ObservableEmitter} the {@link #serialize()} method returns instead of the original
 * {@code ObservableEmitter} instance provided by the generator routine if you want to ensure this.
 * The other methods are thread-safe.
 * <p>
 * The emitter allows the registration of a single resource, in the form of a {@link Disposable}
 * or {@link Cancellable} via {@link #setDisposable(Disposable)} or {@link #setCancellable(Cancellable)}
 * respectively. The emitter implementations will dispose/cancel this instance when the
 * downstream cancels the flow or after the event generator logic calls {@link #onError(Throwable)},
 * {@link #onComplete()} or when {@link #tryOnError(Throwable)} succeeds.
 * <p>
 * Only one {@code Disposable} or {@code Cancellable} object can be associated with the emitter at
 * a time. Calling either {@code set} method will dispose/cancel any previous object. If there
 * is a need for handling multiple resources, one can create a {@link io.reactivex.disposables.CompositeDisposable}
 * and associate that with the emitter instead.
 * <p>
 * The {@link Cancellable} is logically equivalent to {@code Disposable} but allows using cleanup logic that can
 * throw a checked exception (such as many {@code close()} methods on Java IO components). Since
 * the release of resources happens after the terminal events have been delivered or the sequence gets
 * cancelled, exceptions throw within {@code Cancellable} are routed to the global error handler via
 * {@link io.reactivex.plugins.RxJavaPlugins#onError(Throwable)}.
 *
 * @param <T> the value type to emit(要发出信号的值类型)
 *            是一个抽象类
 *            是一个信号发射器(它可以安全取消发射事件)
 */
public interface ObservableEmitter<T> extends Emitter<T> {

    /**
     * Sets a Disposable on this emitter; any previous {@link Disposable}
     * or {@link Cancellable} will be disposed/cancelled.
     * 设置此发射器上的可处理的内容(用完既可丢弃的,可任意处理的)
     *
     * @param d the disposable, null is allowed
     */
    void setDisposable(@Nullable Disposable d);

    /**
     * Sets a Cancellable on this emitter; any previous {@link Disposable}
     * or {@link Cancellable} will be disposed/cancelled.
     * <p>
     * 设置此发射器上的可取消项,任何以前的Disposable或Cancellable将被处理/取消
     *
     * @param c the cancellable resource, null is allowed
     */
    void setCancellable(@Nullable Cancellable c);

    /**
     * Returns true if the downstream disposed the sequence or the
     * emitter was terminated via {@link #onError(Throwable)}, {@link #onComplete} or a
     * successful {@link #tryOnError(Throwable)}.
     * 如果下游处理序列或者发射器通过onError,onComplete或者成功调用tryOnError来终止
     *
     * <p>This method is thread-safe.线程安全
     *
     * @return true if the downstream disposed the sequence or the emitter was terminated
     * 如果下游处理序列或发射器终止，则为true
     */
    boolean isDisposed();

    /**
     * Ensures that calls to onNext, onError and onComplete are properly serialized.
     * <p>
     * 确保对onNext、onError和onComplete的调用被正确序列化
     *
     * @return the serialized ObservableEmitter
     * 返回序列化的ObservableEmitter
     */
    @NonNull
    ObservableEmitter<T> serialize();

    /**
     * Attempts to emit the specified {@code Throwable} error if the downstream
     * hasn't cancelled the sequence or is otherwise terminated, returning false
     * if the emission is not allowed to happen due to lifecycle restrictions.
     * <p>
     * 如果下游没有取消序列或以其他方式终止，则尝试发出指定的{@code Throwable}错误;如果由于生命周期限制不允许发出，则返回false
     *
     * <p>
     * Unlike {@link #onError(Throwable)}, the {@code RxJavaPlugins.onError} is not called
     * if the error could not be delivered.
     * <p>History: 2.1.1 - experimental
     *
     * @param t the throwable error to signal if possible
     * @return true if successful, false if the downstream is not able to accept further(如果下游不允许接收事件,则返回错误)
     * events
     * @since 2.2
     */
    boolean tryOnError(@NonNull Throwable t);
}

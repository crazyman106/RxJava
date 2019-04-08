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

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicLong;

/**
 * A ThreadFactory that counts how many threads have been created and given a prefix,
 * sets the created Thread's name to {@code prefix-count}.
 * <p>
 * ThreadFactory计算已经创建了多少线程，并给定一个前缀，它将创建的线程的名称设置为{@code prefix-count}。
 */
public final class RxThreadFactory extends AtomicLong implements ThreadFactory {

    private static final long serialVersionUID = -7789753024099756196L;

    final String prefix;

    final int priority;

    final boolean nonBlocking;

    public RxThreadFactory(String prefix) {
        // 线程默认优先级
        this(prefix, Thread.NORM_PRIORITY, false);
    }

    public RxThreadFactory(String prefix, int priority) {
        this(prefix, priority, false);
    }

    public RxThreadFactory(String prefix, int priority, boolean nonBlocking) {
        this.prefix = prefix;
        this.priority = priority;
        this.nonBlocking = nonBlocking;
    }

    @Override
    public Thread newThread(Runnable r) {
        StringBuilder nameBuilder = new StringBuilder(prefix).append('-').append(incrementAndGet());
        String name = nameBuilder.toString();
        Thread t = nonBlocking ? new RxCustomThread(r, name) : new Thread(r, name);
        t.setPriority(priority);
        t.setDaemon(true);//将此线程标记为守护线程或用户线程
        return t;
    }

    @Override
    public String toString() {
        return "RxThreadFactory[" + prefix + "]";
    }

    static final class RxCustomThread extends Thread implements NonBlockingThread {
        RxCustomThread(Runnable run, String name) {
            super(run, name);
        }
    }
}

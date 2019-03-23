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

package rxjava.fengzi.com.rxjava.rxjava.io.reactivex.annotations;

/**
 * Enumeration for various kinds of backpressure support.
 *
 * @since 2.0 各种背压式支持的枚举类型
 */
public enum BackpressureKind {
    /**
     * The backpressure-related requests pass through this operator without change.
     * 与背压式相关的请求直接通过,无需修改
     */
    PASS_THROUGH,
    /**
     * The operator fully supports backpressure and may coordinate downstream requests
     * with upstream requests through batching, arbitration or by other means.
     * <p>
     * operator完全支持背压式,并且可能通过 batching(批量),仲裁或其他方式协调下游和上游的请求
     */
    FULL,
    /**
     * The operator performs special backpressure management; see the associated javadoc.
     * 操作者执行特殊的背压式管理,
     */
    SPECIAL,
    /**
     * The operator requests Long.MAX_VALUE from upstream but respects the backpressure
     * of the downstream.
     *
     * 操作者从上游请求最大的数据,但是要考虑到下游的背压
     */
    UNBOUNDED_IN,
    /**
     * The operator will emit a MissingBackpressureException if the downstream didn't request
     * enough or in time.
     *
     * 操作者将会发射出一个MissingBackpressureException异常,如果下游没有及时提出足够的要求
     *
     */
    ERROR,
    /**
     * The operator ignores all kinds of backpressure and may overflow the downstream.
     * 操作者忽略各种背压,可能会溢出下游
     */
    NONE
}

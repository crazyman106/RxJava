package com.rxjava.demo.create;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.ObservableSource;

import java.util.concurrent.Callable;

public class ObservableObj<T> {

    /**
     * 使用create创建Observable对象
     * 通过编程调用observer方法从头创建一个可观察对象
     *
     * create函数:
     *  一:Observable创建
     *     public static <T> Observable<T> create(ObservableOnSubscribe<T> source) {
     *         ObjectHelper.requireNonNull(source, "source is null");
     *         return RxJavaPlugins.onAssembly(new ObservableCreate<T>(source));
     *     }
     *
     *     1.我们传入一个ObservableOnSubscribe对象
     *     2.创建一个ObservableCreate对象,并且将ObservableOnSubscribe对象传入到ObservableCreate对象中,使ObservableCreate持有ObservableOnSubscribe对象
     *
     *     public static <T> Observable<T> onAssembly(@NonNull Observable<T> source) {
     *         // 应用hook函数的一些处理，一般用到不到
     *         Function<? super Observable, ? extends Observable> f = onObservableAssembly;
     *         if (f != null) {
     *             return apply(f, source);
     *         }
     *         return source;
     *     }
     *     1.对ObservableCreate进行一些处理,然后返回该对象
     *
     *  二:订阅
     *     public final void subscribe(Observer<? super T> observer) {
     *             1.
     *             observer = RxJavaPlugins.onSubscribe(this, observer);
     *             2.
     *             subscribeActual(observer);
     *     }
     *     1.将观察者和被观察者建立订阅关系
     *     2.调用Observable的subscribeActual函数,(实际上是调用的是实现类ObservableCreate的subscribeActual())
     *
     *     protected void subscribeActual(Observer<? super T> observer) {
     *         //1. 发射器:持有观察者对象
     *         CreateEmitter<T> parent = new CreateEmitter<T>(observer);
     *         //2. 告诉观察者已经成功订阅了被观察者
     *         observer.onSubscribe(parent);
     *         try {
     *             //3. 发送事件流,完成订阅:ObservableOnSubscribe调用它的函数
     *             // 回调函数:subscribe();传递参数CreateEmitter<T>(observer)
     *             // 在CreateEmitter中持有了Observer对象,我们调用CreateEmitter发射事件时,会触发Observer调用对应的函数
     *             source.subscribe(parent);
     *         } catch (Throwable ex) {
     *             Exceptions.throwIfFatal(ex);
     *             parent.onError(ex);
     *         }
     *     }
     *     1.创建一个信号发射器(既发射事件的类),他持有观察者对象
     *     2.观察者绑定发射器
     *     3.接口回调:void subscribe(@NonNull ObservableEmitter<T> emitter) throws Exception;
     *
     *  三:线程切换
     *
     *
     *
     * @param obj
     * @return
     */
    public Observable create(final T obj) {
        return Observable.create(new ObservableOnSubscribe<T>() {
            @Override
            public void subscribe(ObservableEmitter<T> emitter) throws Exception {
                emitter.onNext(obj);
                emitter.onComplete();
            }
        });
    }

    /**
     * defer 就相当于懒加载，只有等observable 与observer建立了订阅关系时，observable才会建立
     *
     * @param obj
     * @return
     */
    public Observable defer(final T obj) {
        return Observable.defer(new Callable<ObservableSource<T>>() {

            @Override
            public ObservableSource<T> call() throws Exception {
                return create(obj);
            }
        });
    }

    /**
     * 执行onCompleted()
     *
     * @return
     */
    public Observable empty() {
        return Observable.empty();
    }

    /**
     * @return
     */
    public Observable never() {
        return Observable.never();
    }

    /**
     * 下游通过循环接收数据
     *
     * @param items
     * @return
     */
    public Observable fromArray(T... items) {
        return Observable.fromArray(items);
    }

    /**
     * @return
     */
    public Observable interval() {
        // TODO
        return null;
    }

}

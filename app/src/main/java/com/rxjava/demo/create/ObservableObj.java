package com.rxjava.demo.create;

import android.schedulers.AndroidSchedulers;
import android.util.Log;
import io.reactivex.*;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Function;
import io.reactivex.observables.GroupedObservable;
import io.reactivex.schedulers.Schedulers;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

public class ObservableObj<T> {
    private final String TAG = this.getClass().getSimpleName();

    /**
     * 使用create创建Observable对象
     * 通过编程调用observer方法从头创建一个可观察对象
     * <p>
     * create函数:
     * 一:Observable创建
     * public static <T> Observable<T> create(ObservableOnSubscribe<T> source) {
     * ObjectHelper.requireNonNull(source, "source is null");
     * return RxJavaPlugins.onAssembly(new ObservableCreate<T>(source));
     * }
     * <p>
     * 1.我们传入一个ObservableOnSubscribe对象
     * 2.创建一个ObservableCreate对象,并且将ObservableOnSubscribe对象传入到ObservableCreate对象中,使ObservableCreate持有ObservableOnSubscribe对象
     * <p>
     * public static <T> Observable<T> onAssembly(@NonNull Observable<T> source) {
     * // 应用hook函数的一些处理，一般用到不到
     * Function<? super Observable, ? extends Observable> f = onObservableAssembly;
     * if (f != null) {
     * return apply(f, source);
     * }
     * return source;
     * }
     * 1.对ObservableCreate进行一些处理,然后返回该对象
     * <p>
     * 二:订阅
     * public final void subscribe(Observer<? super T> observer) {
     * 1.
     * observer = RxJavaPlugins.onSubscribe(this, observer);
     * 2.
     * subscribeActual(observer);
     * }
     * 1.将观察者和被观察者建立订阅关系
     * 2.调用Observable的subscribeActual函数,(实际上是调用的是实现类ObservableCreate的subscribeActual())
     * <p>
     * protected void subscribeActual(Observer<? super T> observer) {
     * //1. 发射器:持有观察者对象
     * CreateEmitter<T> parent = new CreateEmitter<T>(observer);
     * //2. 告诉观察者已经成功订阅了被观察者
     * observer.onSubscribe(parent);
     * try {
     * //3. 发送事件流,完成订阅:ObservableOnSubscribe调用它的函数
     * // 回调函数:subscribe();传递参数CreateEmitter<T>(observer)
     * // 在CreateEmitter中持有了Observer对象,我们调用CreateEmitter发射事件时,会触发Observer调用对应的函数
     * source.subscribe(parent);
     * } catch (Throwable ex) {
     * Exceptions.throwIfFatal(ex);
     * parent.onError(ex);
     * }
     * }
     * 1.创建一个信号发射器(既发射事件的类),他持有观察者对象
     * 2.观察者绑定发射器
     * 3.接口回调:void subscribe(@NonNull ObservableEmitter<T> emitter) throws Exception;
     * <p>
     * 三:线程切换
     */
    public void create() {
        // 创建被观察者
        Observable observable = Observable.create(new ObservableOnSubscribe<String>() {
            @Override
            public void subscribe(ObservableEmitter<String> emitter) throws Exception {
                // 消息发射器
                emitter.onNext("success1");
                emitter.onNext("success2");
                emitter.onNext("success3");
                emitter.onComplete();
                emitter.onNext("success4");
                emitter.onNext("success5");
            }
        });
        // 创建观察者
        Observer observer = new Observer<String>() {
            Disposable disposable;

            @Override
            public void onSubscribe(Disposable d) {
                this.disposable = d;
                Log.e(TAG, "onSubscribe");
            }

            @Override
            public void onNext(String s) {
                Log.e(TAG, "onNext: " + s);
            }

            @Override
            public void onError(Throwable e) {
                Log.e(TAG, "onError");
            }

            @Override
            public void onComplete() {
                Log.e(TAG, "onComplete");
            }
        };
        // 订阅
        observable.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(observer);
    }

    /**
     * 缓冲区操作符:它将发射出的可观察对象转换为发出这些项的缓冲集合的可观察对象。在缓冲区的各种特定于语言的实现中有许多变体，它们在选择将哪些项放入哪个缓冲区方面有所不同
     * <p>
     * 就是将Observable 发射出的内容转换成集合发射出来
     */
    public void buffer() {
        // 创建被观察者
        Observable observable = (Observable) Observable.create(new ObservableOnSubscribe<String>() {
            @Override
            public void subscribe(ObservableEmitter<String> emitter) throws Exception {
                // 消息发射器
                emitter.onNext("success1");
                emitter.onNext("success2");
                emitter.onNext("success3");
                emitter.onNext("success4");
                emitter.onNext("success5");
                emitter.onNext("success6");
            }
        }).buffer(2, 2).delay(2, TimeUnit.SECONDS);

        // 创建观察者
        Observer observer = new Observer<List<String>>() {
            Disposable disposable;

            @Override
            public void onSubscribe(Disposable d) {
                this.disposable = d;
                Log.e(TAG, "onSubscribe");
            }

            @Override
            public void onNext(List<String> data) {
                for (String str : data) {
                    Log.e(TAG, "onNext: " + str);
                    if (str == "success6") {
                        disposable.dispose();
                    }
                }
                Log.e(TAG, "onNext: " + data.size());
            }

            @Override
            public void onError(Throwable e) {
                Log.e(TAG, "onError");
            }

            @Override
            public void onComplete() {
                Log.e(TAG, "onComplete");
            }
        };
        // 订阅
        observable.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(observer);
    }


    public void flatMap() {
        // 创建被观察者
        Observable observable = (Observable) Observable.create(new ObservableOnSubscribe<String>() {
            @Override
            public void subscribe(ObservableEmitter<String> emitter) throws Exception {
                // 消息发射器
                emitter.onNext("success1");
                emitter.onNext("success2");
                emitter.onNext("success3");
            }
        }).flatMap(new Function<String, ObservableSource<String>>() {
            @Override
            public ObservableSource<String> apply(final String s) throws Exception {

                return Observable.create(new ObservableOnSubscribe<String>() {
                    @Override
                    public void subscribe(ObservableEmitter<String> emitter) throws Exception {
                        emitter.onNext(s + ": a");
                        emitter.onNext(s + ": b");
                        emitter.onNext(s + ": c");
                    }
                });
            }
        });

        // 创建观察者
        Observer observer = new Observer<String>() {
            Disposable disposable;

            @Override
            public void onSubscribe(Disposable d) {
                this.disposable = d;
                Log.e(TAG, "onSubscribe");
            }

            @Override
            public void onNext(String data) {
                Log.e(TAG, "onNext: " + data);
            }

            @Override
            public void onError(Throwable e) {
                Log.e(TAG, "onError");
            }

            @Override
            public void onComplete() {
                Log.e(TAG, "onComplete");
            }
        };
        // 订阅
        observable.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(observer);
    }

    public void groupBy() {
        // 创建被观察者
        Observable observable = Observable.create(new ObservableOnSubscribe<String>() {
            @Override
            public void subscribe(ObservableEmitter<String> emitter) throws Exception {
                // 消息发射器
                emitter.onNext("success1");
                emitter.onNext("success1");
                emitter.onNext("success1");
                emitter.onNext("success1");
                emitter.onNext("success");
                emitter.onNext("success");
                emitter.onNext("success");
                emitter.onNext("success");
                emitter.onComplete();
            }
        }).groupBy(new Function<String, String>() {
            @Override
            public String apply(String s) throws Exception {
                if (s.equals("success")) {
                    return "a";
                }
                return "b";
            }
        });
        // 创建观察者
        Observer observer = new Observer<GroupedObservable<String, String>>() {
            Disposable disposable;

            @Override
            public void onSubscribe(Disposable d) {
                this.disposable = d;
                Log.e(TAG, "onSubscribe");
            }

            @Override
            public void onNext(final GroupedObservable<String, String> stringStringGroupedObservable) {
                stringStringGroupedObservable.subscribe(new Observer<String>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(String s) {
                        Log.e(TAG, "onNext: " + stringStringGroupedObservable.getKey() + "--" + s);
                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onComplete() {

                    }
                });
            }

            @Override
            public void onError(Throwable e) {
                Log.e(TAG, "onError");
            }

            @Override
            public void onComplete() {
                Log.e(TAG, "onComplete");
            }
        };
        // 订阅
        observable.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(observer);
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
                return Observable.create(new ObservableOnSubscribe<T>() {
                    @Override
                    public void subscribe(ObservableEmitter<T> emitter) throws Exception {

                    }
                });
            }
        });
    }

    public void single() {
        Single.create(new SingleOnSubscribe<String>() {
            @Override
            public void subscribe(SingleEmitter<String> emitter) throws Exception {
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

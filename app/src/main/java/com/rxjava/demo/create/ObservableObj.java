package com.rxjava.demo.create;

import android.annotation.SuppressLint;
import android.os.SystemClock;
import android.schedulers.AndroidSchedulers;
import android.support.annotation.NonNull;
import android.util.Log;
import com.rxjava.demo.fanxing.Func1;
import io.reactivex.*;
import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.*;
import io.reactivex.observables.GroupedObservable;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.AsyncSubject;
import io.reactivex.subjects.BehaviorSubject;
import io.reactivex.subjects.PublishSubject;
import io.reactivex.subjects.ReplaySubject;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.lang.reflect.Array;
import java.nio.file.attribute.UserDefinedFileAttributeView;
import java.util.*;
import java.util.concurrent.*;

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
        }, new BiFunction<String, String, String>() {
            @Override
            public String apply(String s, String s2) throws Exception {
                // 对上述生成的分类 Observable发射的内容进行处理
                return s + s2;
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

    public void switchMap() {
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
                emitter.onComplete();
            }
        }).switchMap(new Function<String, ObservableSource<String>>() {
            @Override
            public ObservableSource<String> apply(final String s) throws Exception {
                return Observable.create(new ObservableOnSubscribe<String>() {
                    @Override
                    public void subscribe(ObservableEmitter<String> emitter) throws Exception {
                        if (!s.equals("success1")) {
                            emitter.onNext(s + " : a");
                            emitter.onNext(s + " : b");
                            emitter.onComplete();
                        }
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

    public void startWith() {
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
                emitter.onComplete();
            }
        })/*.startWith("start ......").doOnNext(new Consumer<String>() {
            @Override
            public void accept(String s) throws Exception {
                Log.e(TAG, "doOnNext: " + s + "--" + Thread.currentThread().toString());
            }
        }).doOnTerminate(new Action() {
            @Override
            public void run() throws Exception {
                Log.e(TAG, "doOnTerminate: " + Thread.currentThread().toString());
            }
        }).doOnLifecycle(new Consumer<Disposable>() {
            @Override
            public void accept(Disposable disposable) throws Exception {
                Log.e(TAG, "doOnLifecycle: accept" + Thread.currentThread().toString());
            }
        }, new Action() {
            @Override
            public void run() throws Exception {
                Log.e(TAG, "doOnLifecycle: run" + Thread.currentThread().toString());
            }
        })*/.flatMapIterable(new Function<String, Iterable<String>>() {
            @Override
            public Iterable<String> apply(String s) throws Exception {
                return Arrays.asList(String.valueOf("a" + s), String.valueOf("b" + s), String.valueOf("c" + s));
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
                Log.e(TAG, "onNext: " + data + "-" + Thread.currentThread().toString());
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
        observable.subscribeOn(Schedulers.single()).observeOn(AndroidSchedulers.mainThread()).subscribe(observer);
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


    public void map() {
        // 创建被观察者
        Observable observable = Observable.create(new ObservableOnSubscribe<String>() {
            @Override
            public void subscribe(ObservableEmitter<String> emitter) throws Exception {
                // 消息发射器
                emitter.onNext("success1");
                emitter.onNext("success2");
                emitter.onNext("success3");
                emitter.onNext("success4");
                emitter.onComplete();
            }
        }).map(new Function<String, String>() {
            @Override
            public String apply(String o) throws Exception {
                return o + ":a";
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
            public void onNext(String string) {
                Log.e(TAG, "onNext:" + string);
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

    public void scan() {
        // 创建被观察者
        Observable observable = Observable.create(new ObservableOnSubscribe<String>() {
            @Override
            public void subscribe(ObservableEmitter<String> emitter) throws Exception {
                // 消息发射器
                emitter.onNext("success1");
                emitter.onNext("success2");
                emitter.onNext("success3");
                emitter.onNext("success4");
                emitter.onComplete();
            }
        }).scan("haha", new BiFunction<String, String, String>() {
            @Override
            public String apply(String s, String s2) throws Exception {
                return s2 + "--" + s;
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
            public void onNext(String string) {
                Log.e(TAG, "onNext:" + string);
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

    public void scanWith() {
        // 创建被观察者
        Observable observable = Observable.create(new ObservableOnSubscribe<String>() {
            @Override
            public void subscribe(ObservableEmitter<String> emitter) throws Exception {
                // 消息发射器
                emitter.onNext("success1");
                emitter.onNext("success2");
                emitter.onNext("success3");
                emitter.onNext("success4");
                emitter.onComplete();
            }
        }).scanWith(new Callable<String>() {
            @Override
            public String call() throws Exception {
                return "哈哈";
            }
        }, new BiFunction<String, String, String>() {
            @Override
            public String apply(String s, String s2) throws Exception {
                return s2 + "--" + s;
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
            public void onNext(String string) {
                Log.e(TAG, "onNext:" + string);
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

    public void window() {
        // 创建被观察者
        Observable observable = Observable.create(new ObservableOnSubscribe<String>() {
            @Override
            public void subscribe(ObservableEmitter<String> emitter) throws Exception {
                // 消息发射器
                emitter.onNext("success1");
                emitter.onNext("success2");
                emitter.onNext("success3");
                emitter.onNext("success4");
                emitter.onComplete();
            }
        }).window(2, 1);
        // 创建观察者
        Observer observer = new Observer<Observable<String>>() {
            Disposable disposable;

            @Override
            public void onSubscribe(Disposable d) {
                this.disposable = d;
                Log.e(TAG, "onSubscribe");
            }

            @Override
            public void onNext(Observable<String> string) {
                Log.e(TAG, "onNext: " + string);
                string.subscribe(new Observer<String>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(String s) {
                        Log.e(TAG, "onNext_: " + s);
                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onComplete() {
                        Log.e(TAG, "onComplete_");
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

    public void windowWithCount() {
        // 创建被观察者
        Observable observable = Observable.create(new ObservableOnSubscribe<String>() {
            @Override
            public void subscribe(ObservableEmitter<String> emitter) throws Exception {
                // 消息发射器
                emitter.onNext("success1");
                emitter.onNext("success2");
                emitter.onNext("success3");
                emitter.onNext("success4");
                emitter.onComplete();
            }
        }).window(2, 2);
        // 创建观察者
        Observer observer = new Observer<Observable<String>>() {
            Disposable disposable;

            @Override
            public void onSubscribe(Disposable d) {
                this.disposable = d;
                Log.e(TAG, "onSubscribe");
            }

            @Override
            public void onNext(Observable<String> string) {
                Log.e(TAG, "onNext: " + string);
                string.subscribe(new Observer<String>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(String s) {
                        Log.e(TAG, "onNext_: " + s);
                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onComplete() {
                        Log.e(TAG, "onComplete_");
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

    public void debounce() {
        // 创建被观察者
        Observable observable = Observable.create(new ObservableOnSubscribe<String>() {
            @Override
            public void subscribe(ObservableEmitter<String> emitter) throws Exception {
                // 消息发射器
                emitter.onNext("success1");
                emitter.onNext("success2");
                emitter.onNext("success3");
                emitter.onNext("success4");
                emitter.onComplete();
            }
        })/*.debounce(2,TimeUnit.SECONDS)*//*.throttleWithTimeout(2, TimeUnit.SECONDS)*/.debounce(new Function<String, ObservableSource<String>>() {
            @Override
            public ObservableSource<String> apply(final String s) throws Exception {
                return Observable.create(new ObservableOnSubscribe<String>() {
                    @Override
                    public void subscribe(ObservableEmitter<String> emitter) throws Exception {
                        emitter.onNext(s);
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
            public void onNext(String string) {
                Log.e(TAG, "onNext: " + string);
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

    public void distinct() {
        // 创建被观察者
        Observable observable = Observable.create(new ObservableOnSubscribe<String>() {
            @Override
            public void subscribe(ObservableEmitter<String> emitter) throws Exception {
                // 消息发射器
                emitter.onNext("success1");
                emitter.onNext("success2");
                emitter.onNext("success3");
                emitter.onNext("success4");
                emitter.onNext("success4");
                emitter.onComplete();
            }
        }).distinct();
        // 创建观察者
        Observer observer = new Observer<String>() {
            Disposable disposable;

            @Override
            public void onSubscribe(Disposable d) {
                this.disposable = d;
                Log.e(TAG, "onSubscribe");
            }

            @Override
            public void onNext(String string) {
                Log.e(TAG, "onNext: " + string);
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

    public void filter() {
        // 创建被观察者
        Observable observable = Observable.create((ObservableOnSubscribe<String>) emitter -> {
            // 消息发射器
            emitter.onNext("success1");
            emitter.onNext("success2");
            emitter.onNext("success3");
            emitter.onNext("success4");
            emitter.onNext("success4");
            emitter.onComplete();
        }).ofType(String.class)/*filter(s -> {
            if (s.equals("success1")) {
                return true;
            }
            return false;
        })*/;
        // 创建观察者
        Observer observer = new Observer<String>() {
            Disposable disposable;

            @Override
            public void onSubscribe(Disposable d) {
                this.disposable = d;
                Log.e(TAG, "onSubscribe");
            }

            @Override
            public void onNext(String string) {
                Log.e(TAG, "onNext: " + string);
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

    public void elementAt() {
        // 创建被观察者
        Observable.create((ObservableOnSubscribe<String>) emitter -> {
            // 消息发射器
            emitter.onNext("success1");
            emitter.onNext("success2");
            emitter.onNext("success3");
            emitter.onNext("success4");
            emitter.onNext("success4");
            emitter.onComplete();
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                /*.elementAt(3)*/
                .elementAt(2, "success_default")
                .subscribe(new SingleObserver<String>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        Log.e(TAG, "onSubscribe");
                    }

                    @Override
                    public void onSuccess(String s) {
                        Log.e(TAG, "onSuccess:" + s);
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e(TAG, "onError:" + e.getMessage());
                    }
                });
    }

    @SuppressLint("CheckResult")
    public void ofType() {
        // 创建被观察者
        Observable.create((ObservableOnSubscribe) emitter -> {
            // 消息发射器
            emitter.onNext("success1");
            emitter.onNext("success2");
            emitter.onNext("success3");
            emitter.onNext("success4");
            emitter.onNext("success4");
            emitter.onNext(12);
            emitter.onComplete();
        })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .ofType(Integer.class)
                .subscribe(value -> {
                    Log.e(TAG, "value:" + value);
                });

    }

    public void first() {
        // 创建被观察者
        Observable
                .create((ObservableOnSubscribe) emitter -> {
                    // 消息发射器
                    emitter.onNext(12);
                    emitter.onComplete();
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(value -> {
                    Log.e(TAG, "onSuccess:" + value);
                }, throwable -> {
                    Log.e(TAG, "onError:" + throwable.toString());
                });
    }

    public void ignoreElements() {
        // 创建被观察者
        Observable
                .create((ObservableOnSubscribe) emitter -> {
                    // 消息发射器
                    emitter.onNext(12);
                    emitter.onComplete();
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .ignoreElements()
                .subscribe(new CompletableObserver() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        Log.e(TAG, "onSubscribe");
                    }

                    @Override
                    public void onComplete() {
                        Log.e(TAG, "onComplete");
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e(TAG, "onError:" + e.getMessage());
                    }
                });
    }

    public void last() {
        // 创建被观察者
        Observable
                .create((ObservableOnSubscribe) emitter -> {
                    // 消息发射器
                    emitter.onNext(11);
                    emitter.onNext(13);
                    emitter.onNext(15);
                    emitter.onComplete();
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .lastElement()
                .subscribe(new MaybeObserver() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        Log.e(TAG, "onSubscribe");
                    }

                    @Override
                    public void onSuccess(Object o) {
                        Log.e(TAG, "onSuccess:" + o.toString());
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e(TAG, "onError");
                    }

                    @Override
                    public void onComplete() {
                        Log.e(TAG, "onComplete");
                    }
                });
    }

    public void sample() {
        // 创建被观察者
        Observable.interval(1, TimeUnit.SECONDS)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                /*.throttleFirst(2, TimeUnit.SECONDS)*/
                .sample(2, TimeUnit.SECONDS)
                .skipUntil(new ObservableSource<Object>() {
                    @Override
                    public void subscribe(Observer<? super Object> observer) {

                    }
                })
                .sample(new ObservableSource<Object>() {
                    @Override
                    public void subscribe(Observer<? super Object> observer) {
                        observer.onNext("哈哈哈哈哈哈");
                    }
                })
                .subscribe(new Observer() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        Log.e(TAG, "onDis");
                    }

                    @Override
                    public void onNext(Object o) {
                        Log.e(TAG, "onNext:" + o.toString());
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e(TAG, "onError:" + e.toString());
                    }

                    @Override
                    public void onComplete() {
                        Log.e(TAG, "onComplete");
                    }
                });
    }

    public void combinelatest() {
        // 创建被观察者
        Observable observable1 = Observable.create((ObservableOnSubscribe) emitter -> {
            // 消息发射器
            emitter.onNext("success1");
            emitter.onNext("success2");
            emitter.onNext("success3");
            emitter.onNext("success4");
            emitter.onNext("success5");
        });
        Observable observable2 = Observable.create((ObservableOnSubscribe) emitter -> {
            // 消息发射器
            emitter.onNext("a");
            emitter.onNext("b");
            emitter.onNext("c");
            emitter.onNext("d");
            emitter.onNext("e");
            emitter.onNext("f");
        });
        //.combineLatest(observable1, observable2, (BiFunction<String, String, String>) (s, s2) -> s + s2)
        observable1.withLatestFrom(observable2, new BiFunction<String, String, String>() {
            @Override
            public String apply(String o, String o2) throws Exception {
                return o + o2;
            }
        })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        Log.e("ObservableObj", "onSubscribe");
                    }

                    @Override
                    public void onNext(Object o) {
                        Log.e("ObservableObj_next", o.toString());
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e("ObservableObj", "onError");
                    }

                    @Override
                    public void onComplete() {
                        Log.e("ObservableObj", "onComplete");
                    }
                });
    }

    public void join() {
        Observable observable = Observable.create((ObservableOnSubscribe) emitter -> {
            // 消息发射器
            emitter.onNext("a");
            emitter.onNext("b");
            emitter.onNext("c");
            emitter.onNext("d");
            emitter.onNext("e");
            emitter.onNext("f");
        });
        Observable.create((ObservableOnSubscribe) emitter -> {
            // 消息发射器
            emitter.onNext("success1");
            emitter.onNext("success2");
            emitter.onNext("success3");
            emitter.onNext("success4");
            emitter.onNext("success5");
        }).join(observable, new Function() {
            @Override
            public Object apply(Object o) throws Exception {
                return Observable.timer(1000, TimeUnit.MILLISECONDS);
            }
        }, new Function() {
            @Override
            public Object apply(Object o) throws Exception {
                return Observable.timer(500, TimeUnit.MILLISECONDS);
            }
        }, new BiFunction() {
            @Override
            public Object apply(Object o, Object o2) throws Exception {
                return o + "--" + o2;
            }
        }).subscribe(new Observer() {
            @Override
            public void onSubscribe(Disposable d) {
                Log.e("ObservableObj", "onSubscribe");
            }

            @Override
            public void onNext(Object o) {
                Log.e("ObservableObj_next", o.toString());
            }

            @Override
            public void onError(Throwable e) {
                Log.e("ObservableObj", "onError");
            }

            @Override
            public void onComplete() {
                Log.e("ObservableObj", "onComplete");
            }
        });
    }


    public void merge() {
        Observable observable = Observable.create((ObservableOnSubscribe) emitter -> {
            // 消息发射器
            emitter.onNext("a");
            emitter.onNext("b");
            emitter.onError(new Throwable("--------"));
            emitter.onNext("c");
            emitter.onNext("d");
            emitter.onNext("e");
            emitter.onNext("f");
        });
        Observable.create((ObservableOnSubscribe) emitter -> {
            // 消息发射器
            emitter.onNext("success1");
            Thread.sleep(100);
            emitter.onNext("success2");
            Thread.sleep(500);
            emitter.onNext("success3");
            emitter.onNext("success4");
            emitter.onNext("success5");
        }).mergeWith(observable).subscribe(new Observer() {
            @Override
            public void onSubscribe(Disposable d) {
                Log.e("ObservableObj", "onSubscribe");
            }

            @Override
            public void onNext(Object o) {
                Log.e("ObservableObj_next", o.toString());
            }

            @Override
            public void onError(Throwable e) {
                Log.e("ObservableObj", "onError");
            }

            @Override
            public void onComplete() {
                Log.e("ObservableObj", "onComplete");
            }
        });
    }

    public void startWith_() {
        Observable observable = Observable.create((ObservableOnSubscribe) emitter -> {
            // 消息发射器
            emitter.onNext("a");
            emitter.onNext("b");
            emitter.onNext("c");
            emitter.onNext("d");
            emitter.onNext("e");
            emitter.onNext("f");
            emitter.onComplete();
        });
        Observable.create((ObservableOnSubscribe) emitter -> {
            // 消息发射器
            emitter.onNext("success1");
            emitter.onNext("success2");
            emitter.onNext("success3");
            emitter.onNext("success4");
            emitter.onNext("success5");
            emitter.onComplete();
        }).startWith(observable).startWith("start---").subscribe(new Observer() {
            @Override
            public void onSubscribe(Disposable d) {
                Log.e("ObservableObj", "onSubscribe");
            }

            @Override
            public void onNext(Object o) {
                Log.e("ObservableObj_next", o.toString());
            }

            @Override
            public void onError(Throwable e) {
                Log.e("ObservableObj", "onError");
            }

            @Override
            public void onComplete() {
                Log.e("ObservableObj", "onComplete");
            }
        });
    }

    public void switch_() {
        Observable observable = Observable.create((ObservableOnSubscribe) emitter -> {
            // 消息发射器
            emitter.onNext("a");
            emitter.onNext("b");
            emitter.onNext("c");
            emitter.onNext("d");
            emitter.onNext("e");
            emitter.onNext("f");
            emitter.onComplete();
        });
        Observable.create((ObservableOnSubscribe) emitter -> {
            // 消息发射器
            emitter.onNext("success1");
            emitter.onNext("success2");
            emitter.onNext("success3");
            emitter.onNext("success4");
            emitter.onNext("success5");
            emitter.onComplete();
        }).subscribe(new Observer() {
            @Override
            public void onSubscribe(Disposable d) {
                Log.e("ObservableObj", "onSubscribe");
            }

            @Override
            public void onNext(Object o) {
                Log.e("ObservableObj_next", o.toString());
            }

            @Override
            public void onError(Throwable e) {
                Log.e("ObservableObj", "onError");
            }

            @Override
            public void onComplete() {
                Log.e("ObservableObj", "onComplete");
            }
        });
    }

    public void zip() {
        Observable observable = Observable.create((ObservableOnSubscribe) emitter -> {
            // 消息发射器
            emitter.onNext("a");
            emitter.onNext("b");
            emitter.onNext("c");
            emitter.onNext("d");
            emitter.onNext("e");
            emitter.onNext("f");
        }).subscribeOn(Schedulers.io());
        Observable.create((ObservableOnSubscribe) emitter -> {
            // 消息发射器
            emitter.onNext("success1");
            emitter.onNext("success2");
            emitter.onNext("success3");
            Thread.sleep(1000);
            emitter.onNext("success4");
        }).subscribeOn(Schedulers.io()).zipWith(observable, new BiFunction() {
            @Override
            public Object apply(Object o, Object o2) throws Exception {
                return o + "--" + o2;
            }
        }).subscribe(new Observer() {
            @Override
            public void onSubscribe(Disposable d) {
                Log.e("ObservableObj", "onSubscribe");
            }

            @Override
            public void onNext(Object o) {
                Log.e("ObservableObj_next", o.toString());
            }

            @Override
            public void onError(Throwable e) {
                Log.e("ObservableObj", "onError");
            }

            @Override
            public void onComplete() {
                Log.e("ObservableObj", "onComplete");
            }
        });
    }

    private Observable<String> createObserver() {
        return Observable.create(emitter -> {
            for (int i = 1; i <= 6; i++) {
                if (i < 3) {
                    emitter.onNext(i + "");
                } else {
                    emitter.onError(new Throwable("Throw error"));
                }
            }
            emitter.onComplete();
        });
    }

    private Observable<String> createObserver2() {
        return Observable.create(emitter -> {
            for (int i = 1; i <= 6; i++) {
                if (i < 3) {
                    emitter.onNext("onNext:" + i);
                } else {
                    emitter.onError(new Exception("the nubmer is greater than 3"));
                    //下面写法也是可以的
                    /*try {
                        throw new Exception("the nubmer is greater than 3");
                    } catch (Exception e) {
                        subscriber.onError(e);
                    }*/
                }
            }
            emitter.onComplete();
        });
    }

    /**
     * onErrorReturn方法 返回一个镜像原有Observable行为的新Observable
     * 会忽略前者的onError调用，不会将错误传递给观察者，而是发射一个特殊的项并调用观察者的onCompleted方法
     */
    public void onErrorReturn() {
        Observable
                .create((ObservableOnSubscribe<String>) e -> {
                    for (int i = 0; i <= 3; i++) {
                        if (i == 2) {
                            e.onError(new Throwable("出现错误了"));
                        } else {
                            e.onNext(i + "");
                        }
               /* try {
                    Thread.sleep(1000);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }*/
                    }
                    e.onComplete();
                })
                .subscribeOn(Schedulers.newThread())
                .onErrorReturn(throwable -> {
                    Log.e(TAG, "在onErrorReturn处理了: " + throwable.toString());
                    //拦截到错误之后，返回一个结果发射，然后就正常结束了。
                    return "1";
                })
                .subscribe(s -> Log.e(TAG, "收到消息: " + s)
                        , throwable -> Log.e(TAG, "结果错误: " + throwable.toString()));
    }

    public void onErrorResumeNext() {
        Observable
                .create((ObservableOnSubscribe<String>) e -> {
                    for (int i = 0; i <= 3; i++) {
                        if (i == 2) {
                            e.onError(new Throwable("出现错误了"));
                        } else {
                            e.onNext(i + "");
                        }
               /* try {
                    Thread.sleep(1000);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }*/
                    }
                    e.onComplete();
                })
                .subscribeOn(Schedulers.newThread())
                /*.onErrorReturn(throwable -> {
                    Log.e(TAG, "在onErrorReturn处理了: " + throwable.toString());
                    //拦截到错误之后，返回一个结果发射，然后就正常结束了。
                    return "1";
                })*/
                /*.onErrorResumeNext(new Function<Throwable, ObservableSource<? extends String>>() {
                    @Override
                    public ObservableSource<? extends String> apply(Throwable throwable) throws Exception {
                        return Observable.create(emitter -> {
                            emitter.onNext("onErrorResumeNext");
                            emitter.onNext("game over");
                            emitter.onComplete();
                        });
                    }
                })*/
                .onErrorResumeNext(observer -> {
                    observer.onNext("onErrorResumeNext");
                    observer.onNext("game over");
                    observer.onComplete();
                })
                .subscribe(s -> Log.e(TAG, "收到消息: " + s)
                        , throwable -> Log.e(TAG, "结果错误: " + throwable.toString())
                        , () -> Log.e(TAG, "onComplete")
                );
    }

    public void onExceptionResumeNext() {
        Observable
                .create((ObservableOnSubscribe<String>) e -> {
                    for (int i = 0; i <= 3; i++) {
                        if (i == 2) {
                            e.onError(new Exception("出现错误了"));
                        } else {
                            e.onNext(i + "");
                        }
               /* try {
                    Thread.sleep(1000);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }*/
                    }
                    e.onComplete();
                })
                .subscribeOn(Schedulers.newThread())
                .onExceptionResumeNext(observer -> {
                    observer.onNext("onExceptionResumeNext");
                    observer.onNext("game over");
                    observer.onComplete();
                })
                .subscribe(s -> Log.e(TAG, "收到消息: " + s)
                        , throwable -> Log.e(TAG, "结果错误: " + throwable.toString())
                        , () -> Log.e(TAG, "onComplete")
                );
    }

    public void retry() {
        Observable
                .create((ObservableOnSubscribe<String>) e -> {
                    for (int i = 0; i <= 3; i++) {
                        if (i == 2) {
                            e.onError(new Exception("出现错误了"));
                        } else {
                            e.onNext(i + "");
                        }
                        try {
                            Thread.sleep(1000);
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }
                    e.onComplete();
                })
                /*.retry()*/
                /*.retry(2)*/
                /*.retry(new Predicate<Throwable>() {
                    @Override
                    public boolean test(Throwable throwable) throws Exception {
                        //返回假就是不让重新发射数据了，调用观察者的onError就终止了。
                        //返回真就是让被观察者重新发射请求
                        *//*if (throwable.getMessage().equals("出现错误了")) {
                            return true;
                        }*//*
                        return false;
                    }
                })*/
                /*.retry(2, new Predicate<Throwable>() {
                    @Override
                    public boolean test(Throwable throwable) throws Exception {
                        *//*if (throwable.getMessage().equals("出现错误了")) {
                            return true;
                        }*//*
                        return false;
                    }
                })*/
                /*.retry(new BiPredicate<Integer, Throwable>() {
                    @Override
                    public boolean test(Integer integer, Throwable throwable) throws Exception {
                        if (integer ==3){
                            return false;
                        }
                        return true;
                    }
                })*/
                .subscribeOn(Schedulers.newThread())
                .subscribe(s -> Log.e(TAG, "收到消息: " + s)
                        , throwable -> Log.e(TAG, "结果错误: " + throwable.toString())
                        , () -> Log.e(TAG, "onComplete")
                );
    }


    public void retryWhen() {
        Observable
                .create((ObservableOnSubscribe<String>) e -> {
                    for (int i = 0; i <= 3; i++) {
                        if (i == 2) {
                            e.onError(new Exception("出现错误了"));
                        } else {
                            e.onNext(i + "");
                        }
                        try {
                            Thread.sleep(1000);
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }
                    e.onComplete();
                })
                .retryWhen(new Function<Observable<Throwable>, ObservableSource<?>>() {
                    @Override
                    public ObservableSource<?> apply(Observable<Throwable> throwableObservable) throws Exception {
                        return throwableObservable.flatMap(new Function<Throwable, ObservableSource<?>>() {
                            @Override
                            public ObservableSource<?> apply(Throwable throwable) throws Exception {
                                //如果发射的onError就终止
//                                return Observable.error(new Throwable("retryWhen终止啦"));
                                return Observable.timer(100, TimeUnit.MILLISECONDS);
                            }
                        });
                    }
                })
                .subscribe(s -> Log.e(TAG, "收到消息: " + s)
                        , throwable -> Log.e(TAG, "结果错误: " + throwable.toString())
                        , () -> Log.e(TAG, "onComplete")
                );
    }

    @SuppressLint("CheckResult")
    public void retryUnitl() {
        Observable
                .create((ObservableOnSubscribe<String>) e -> {
                    for (int i = 0; i <= 3; i++) {
                        if (i == 2) {
                            e.onError(new Exception("出现错误了"));
                        } else {
                            e.onNext(i + "");
                        }
                        try {
                            Thread.sleep(1000);
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }
                    e.onComplete();
                })
                .retryUntil(() -> {
                    return false;
                })
                .subscribe(s -> Log.e(TAG, "收到消息: " + s)
                        , throwable -> Log.e(TAG, "结果错误: " + throwable.toString())
                        , () -> Log.e(TAG, "onComplete")
                );
    }

    @SuppressLint("CheckResult")
    public void delay() {
        Date datae = new Date();
        Observable
                /*.create((ObservableOnSubscribe<String>) e -> {
                    e.onError(new Throwable("game over"));
                    for (int i = 0; i <= 3; i++) {
                        e.onNext(i + "-" + Thread.currentThread().toString());
                    }
                    e.onComplete();
                })*/
                .range(0, 3)
                /*.delay(5, TimeUnit.SECONDS)*/
                /*.delay(1, TimeUnit.SECONDS, Schedulers.io())*/
                /*.delay(5,TimeUnit.SECONDS,true)*/
                .delaySubscription(5000, TimeUnit.MILLISECONDS)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext(new Consumer<Integer>() {
                    @Override
                    public void accept(Integer integer) throws Exception {
                        Log.e(TAG, "收到消息Accept: " + datae.getTime() + "--" + integer + "--" + Thread.currentThread().toString());
                    }
                })
                .subscribe(s -> Log.e(TAG, "收到消息: " + datae.getTime() + "--" + s + "--" + Thread.currentThread().toString())
                        , throwable -> Log.e(TAG, "结果错误: " + throwable.toString())
                        , () -> Log.e(TAG, "onComplete" + datae.getTime() + "--")
                );
    }

    @SuppressLint("CheckResult")
    public void doXXX() {
        Observable
                .create((ObservableOnSubscribe<String>) e -> {
                    for (int i = 0; i <= 3; i++) {
                        e.onNext(i + "-" + Thread.currentThread().toString());
                    }
                    e.onComplete();
                })
                .doOnEach(new Observer<String>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        Log.e(TAG, "Disposable");
                    }

                    @Override
                    public void onNext(String s) {
                        Log.e(TAG, "收到消息_: " + s);
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e(TAG, "结果错误_: " + e.toString());
                    }

                    @Override
                    public void onComplete() {
                        Log.e(TAG, "onComplete");
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(s -> Log.e(TAG, "收到消息: " + s)
                        , throwable -> Log.e(TAG, "结果错误: " + throwable.toString())
                        , () -> Log.e(TAG, "onComplete")
                );
    }

    @SuppressLint("CheckResult")
    public void materialize() {
        Observable
                .create((ObservableOnSubscribe<String>) e -> {
                    e.onNext("1");
                    e.onNext("2");
                    e.onNext("3");
                    e.onComplete();
                })
                .materialize()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(stringNotification -> {
                            if (stringNotification.isOnNext()) {
                                Log.e(TAG, "收到消息: " + stringNotification.getValue());
                            } else if (stringNotification.isOnComplete()) {
                                Log.e(TAG, "onComplete1:" + System.currentTimeMillis());
                            }
                        }
                        , throwable -> Log.e(TAG, "结果错误: " + throwable.toString())
                        , () -> {
                            Log.e(TAG, "onComplete2:" + System.currentTimeMillis());
                        }
                );
    }

    @SuppressLint("CheckResult")
    public void timeInterval() {
        Observable
                .create((ObservableOnSubscribe<String>) e -> {
                    e.onNext("1");
                    e.onNext("2");
                    e.onNext("3");
                    e.onComplete();
                })
                .timeInterval()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(stringNotification ->
                                Log.e(TAG, "收到消息: " + stringNotification)
                        , throwable -> Log.e(TAG, "结果错误: " + throwable.toString())
                        , () -> Log.e(TAG, "onComplete2:" + System.currentTimeMillis())
                );
    }

    @SuppressLint("CheckResult")
    public void timeOut() {
        Observable
                .create((ObservableOnSubscribe<Integer>) e -> {
                    e.onNext(1);
                    SystemClock.sleep(100);
                    e.onNext(2);
                    SystemClock.sleep(100);
                    e.onNext(3);
                    SystemClock.sleep(100);
                    e.onNext(4);
                    SystemClock.sleep(100);
                    e.onNext(5);
                    SystemClock.sleep(100);
                    e.onNext(6);
//                    e.onComplete();
                })
//                .timeout(2, TimeUnit.SECONDS)
                /*.timeout(2, TimeUnit.SECONDS, Observable.create(emitter -> {
                    emitter.onNext("aa");
                    emitter.onNext("bb");
                    emitter.onComplete();
                }))*/
                // (Function<? super T, ? extends ObservableSource<? extends R>> mapper)
                // (Function<? super T, ? extends ObservableSource<V>> mapper)
                /*.timeout(new Function<Integer, ObservableSource<Integer>>() {
                    @Override
                    public ObservableSource<Integer> apply(Integer integer) throws Exception {
                        return Observable.range(5, 10);
                    }
                })*/
                .timestamp(TimeUnit.MILLISECONDS)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(stringNotification ->
                                Log.e(TAG, "收到消息: " + stringNotification)
                        , throwable -> Log.e(TAG, "结果错误: " + throwable.toString())
                        , () -> Log.e(TAG, "onComplete2:" + System.currentTimeMillis())
                );
    }

    @SuppressLint("CheckResult")
    public void using() {
        Observable.using(() -> Arrays.asList(new Integer[]{1, 2, 3, 4, 5}),
                (Function<List<Integer>, ObservableSource<Integer>>) integers -> Observable.create(emitter -> {
                    for (Integer data : integers) {
                        emitter.onNext(data);
                    }
                }), integers -> Log.e(TAG, "释放资源:" + integers.toString()))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Integer>() {
                    Disposable disposable;

                    @Override
                    public void onSubscribe(Disposable d) {
                        disposable = d;
                    }

                    @Override
                    public void onNext(Integer integer) {
                        Log.e(TAG, "收到消息: " + integer);
                        if (integer == 5) {
                            disposable.dispose();
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e(TAG, "结果错误: " + e.toString());
                    }

                    @Override
                    public void onComplete() {
                        Log.e(TAG, "onComplete");
                    }
                });
    }

    @SuppressLint("CheckResult")
    public void all() {
        Observable
                .create((ObservableOnSubscribe<Integer>) emitter -> {
                    emitter.onNext(2);
                    emitter.onNext(3);
                    emitter.onNext(4);
                    emitter.onComplete();
                })
                .subscribeOn(Schedulers.io())
                /*.all(integer -> {
                    if (integer % 2 == 0) {
                        return true;
                    }
                    return false;
                })*/
                .any(new Predicate<Integer>() {
                    @Override
                    public boolean test(Integer integer) throws Exception {
                        if (integer % 2 == 0) {
                            return true;
                        }
                        return false;
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(aBoolean -> Log.e(TAG, "success:" + aBoolean),
                        throwable -> Log.e(TAG, "failed:" + throwable.getMessage()));
    }

    @SuppressLint("CheckResult")
    public void amb() {
        List iterator = new ArrayList();
        iterator.add(Observable
                .create((ObservableOnSubscribe<Integer>) emitter -> {
                    emitter.onNext(1);
                    emitter.onNext(2);
                    emitter.onNext(3);
                    emitter.onNext(4);
                    emitter.onComplete();
                }));
        iterator.add(Observable
                .create((ObservableOnSubscribe<Integer>) emitter -> {
                    emitter.onNext(5);
                    emitter.onNext(6);
                    emitter.onNext(7);
                    emitter.onNext(8);
                    emitter.onComplete();
                }));
        iterator.add(Observable
                .create((ObservableOnSubscribe<Integer>) emitter -> {
                    emitter.onNext(9);
                    emitter.onNext(10);
                    emitter.onNext(11);
                    emitter.onNext(12);
                    emitter.onComplete();
                }));
        iterator.add(Observable
                .create((ObservableOnSubscribe<Integer>) emitter -> {
                    emitter.onNext(13);
                    emitter.onNext(14);
                    emitter.onNext(15);
                    emitter.onNext(16);
                    emitter.onComplete();
                }));
        Observable.amb(iterator).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new Observer() {
            @Override
            public void onSubscribe(Disposable d) {
                Log.e(TAG, "onSubscribe:" + d.toString());
            }

            @Override
            public void onNext(Object o) {
                Log.e(TAG, "onNext:" + o.toString());
            }

            @Override
            public void onError(Throwable e) {
                Log.e(TAG, "onError:" + e.getMessage().toString());
            }

            @Override
            public void onComplete() {
                Log.e(TAG, "onComplete");
            }
        });
    }

    @SuppressLint("CheckResult")
    public void contains() {
        Observable
                .create((ObservableOnSubscribe<Integer>) emitter -> {
                    emitter.onComplete();
                })
//                .contains(6)
                .isEmpty()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SingleObserver<Boolean>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onSuccess(Boolean aBoolean) {
                        Log.e(TAG, "onSuccess:" + aBoolean);
                    }

                    @Override
                    public void onError(Throwable e) {

                    }
                });
    }

    public void defaultIfEmpty() {
        Observable
                .create((ObservableOnSubscribe<Integer>) emitter -> {
                    emitter.onComplete();
                })
//                .defaultIfEmpty(5)
                .switchIfEmpty(Observable.create(new ObservableOnSubscribe<Integer>() {
                    @Override
                    public void subscribe(ObservableEmitter<Integer> emitter) throws Exception {
                        emitter.onNext(8);
                    }
                })).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Integer>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(Integer aBoolean) {
                        Log.e(TAG, "onSuccess:" + aBoolean);
                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onComplete() {

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
        Observable.timer(1, TimeUnit.SECONDS).subscribe(new Observer<Long>() {
            @Override
            public void onSubscribe(Disposable d) {
                Log.e(TAG, "onSub");
            }

            @Override
            public void onNext(Long aLong) {
                Log.e(TAG, "onNext:" + aLong);
            }

            @Override
            public void onError(Throwable e) {
                Log.e(TAG, "onError:");

            }

            @Override
            public void onComplete() {
                Log.e(TAG, "onComplete:");
            }
        });
        return null;
    }

    public Observable just() {
        Observable.fromCallable(new Callable<String>() {
            @Override
            public String call() throws Exception {
                return "aa";
            }
        });
        return null;
    }

    public Observable compose() {
        Observable.just(1).compose(transformer()).subscribe(new Observer() {
            @Override
            public void onSubscribe(Disposable d) {

            }

            @Override
            public void onNext(Object o) {

            }

            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onComplete() {

            }
        });
        return null;
    }

    public ObservableTransformer transformer() {
        return new ObservableTransformer() {
            @Override
            public ObservableSource apply(Observable upstream) {
                return upstream.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).unsubscribeOn(AndroidSchedulers.mainThread());
            }
        };
    }

    @SuppressLint("CheckResult")
    public void subject() {
        PublishSubject<Integer> subject = PublishSubject.create();
        subject.subscribe(System.out::println);
        Executor executor = Executors.newFixedThreadPool(5);
        Observable.range(1, 5).subscribe(i -> {
            executor.execute(() -> {
                try {
                    Thread.sleep(i * 200);
                    subject.onNext(i);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            });
        });
    }

    private static void testPublishSubject() throws InterruptedException {
        PublishSubject<Integer> subject = PublishSubject.create();
        subject.subscribe(i -> System.out.print("(1: " + i + ") "));

        Executor executor = Executors.newFixedThreadPool(5);
        Disposable disposable = Observable.range(1, 5).subscribe(i -> executor.execute(() -> {
            try {
                Thread.sleep(i * 200);
                subject.onNext(i);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }));

        Thread.sleep(500);
        subject.subscribe(i -> System.out.print("(2: " + i + ") "));

        Observable.timer(2, TimeUnit.SECONDS).subscribe(i -> ((ExecutorService) executor).shutdown());
    }

    private static void testReplaySubject() throws InterruptedException {
        ReplaySubject<Integer> subject = ReplaySubject.create();
        subject.subscribe(i -> System.out.print("(1: " + i + ") "));

        Executor executor = Executors.newFixedThreadPool(5);
        Disposable disposable = Observable.range(1, 5).subscribe(i -> executor.execute(() -> {
            try {
                Thread.sleep(i * 200);
                subject.onNext(i);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }));

        Thread.sleep(500);
        subject.subscribe(i -> System.out.print("(2: " + i + ") "));

        Observable.timer(2, TimeUnit.SECONDS).subscribe(i -> ((ExecutorService) executor).shutdown());
    }


    public void testAsyncSubject() {
        AsyncSubject<Integer> asyncSubject = AsyncSubject.create();
        asyncSubject.subscribe(i -> Log.e(TAG, i + "----"));

        Observable.range(1, 5).subscribe(
                i -> {
                    asyncSubject.onNext(i);
                    if (i == 5) {
                        asyncSubject.onComplete();
                    }
                }
        );
    }

    public void testBehaviorSubject() {
        BehaviorSubject<Integer> asyncSubject = BehaviorSubject.createDefault(8);
        asyncSubject.subscribe(i -> Log.e(TAG, i + "----"));

        Observable.range(1, 5).subscribe(
                i -> {
                    asyncSubject.onNext(i);
                    if (i == 5) {
                        asyncSubject.onComplete();
                    }
                }
        );
    }
}

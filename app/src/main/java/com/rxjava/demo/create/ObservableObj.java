package com.rxjava.demo.create;

import android.annotation.SuppressLint;
import android.schedulers.AndroidSchedulers;
import android.support.annotation.NonNull;
import android.util.Log;
import io.reactivex.*;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.*;
import io.reactivex.observables.GroupedObservable;
import io.reactivex.schedulers.Schedulers;

import java.nio.file.attribute.UserDefinedFileAttributeView;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
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
                .skipUntil(new ObservableSource<Object> () {
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

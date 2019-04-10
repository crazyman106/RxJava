# RxJava2.0

## RxJava的订阅流程

```java
        // 1. 创建被观察者
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
        // 2. 创建观察者
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
        // 3. 订阅
        observable.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(observer);
```
通过上述我们可以知道事件的订阅一般分为三步,创建被观察者,创建观察者,订阅.所有我们对RxJava的订阅流程进行分析

* 创建被观察者
* 创建观察者
* 订阅

### 1.创建被观察者

#### 1.1 Observable#create()

```java
    public static <T> Observable<T> create(ObservableOnSubscribe<T> source) {
        ObjectHelper.requireNonNull(source, "source is null");
        return RxJavaPlugins.onAssembly(new ObservableCreate<T>(source));
    }
```
在create()中,对传递进来的参数ObservableOnSubscribe进行非空判断,然后创建了一个ObservableCreate对象,它持有ObservableOnSubscribe对象的引用


#### 1.2 ObservableCreate
ObservableCreate是Observable的一个子类,类中含有一些操作,我们在看onAssembly():

#### 1.3 RxJavaPlugins#onAssembly()
```java
    public static <T> Observable<T> onAssembly(@NonNull Observable<T> source) {
        // 应用hook函数的一些处理，一般用到不到
        Function<? super Observable, ? extends Observable> f = onObservableAssembly;
        if (f != null) {
            return apply(f, source);
        }
        return source;
    }
```
它将我们传递进去的ObservableCreate返回来.

#### 1.4 总结
从以上分析可知，Observable.create()方法仅仅是先将我们自定义的ObservableOnSubscribe对象重新包装成了一个ObservableCreate对象。


### 2.订阅

#### 2.1 Observable#subscribe()

```java
    public final void subscribe(Observer<? super T> observer) {
            1.
            observer = RxJavaPlugins.onSubscribe(this, observer);
            2.
            subscribeActual(observer);
    }
```
1.将观察者和被观察者建立订阅关系

2.调用Observable的subscribeActual函数,(实际上是调用的是实现类ObservableCreate的subscribeActual())

#### 2.2 RxJavaPlugins#onSubscribe()

```java
    public static <T> Observer<? super T> onSubscribe(@NonNull Observable<T> source, @NonNull Observer<? super T> observer) {
        BiFunction<? super Observable, ? super Observer, ? extends Observer> f = onObservableSubscribe;
        if (f != null) {
            return apply(f, source, observer);
        }
        return observer;
    }
```
将观察者和被观察者关联起来,同时将观察者返回
#### 2.3 Observable#subscribeActual()
```java
    protected void subscribeActual(Observer<? super T> observer) {
        //1. 发射器:持有观察者对象
        CreateEmitter<T> parent = new CreateEmitter<T>(observer);
        //2. 告诉观察者已经成功订阅了被观察者
        observer.onSubscribe(parent);
        try {
            //3. 发送事件流,完成订阅:ObservableOnSubscribe调用它的函数
            // 回调函数:subscribe();传递参数CreateEmitter<T>(observer)
            // 在CreateEmitter中持有了Observer对象,我们调用CreateEmitter发射事件时,会触发Observer调用对应的函数
            source.subscribe(parent);
        } catch (Throwable ex) {
            Exceptions.throwIfFatal(ex);
            parent.onError(ex);
        }
    }
```
1.创建一个信号发射器(既发射事件的类),他持有观察者对象

2.观察者绑定发射器

3.接口回调:void subscribe(@NonNull ObservableEmitter<T> emitter) throws Exception;

#### 2.4.1 CreateEmitter

```java
 static final class CreateEmitter<T> extends AtomicReference<Disposable> implements ObservableEmitter<T>, Disposable {
        private static final long serialVersionUID = -3434801548987643227L;

        final Observer<? super T> observer;

        CreateEmitter(Observer<? super T> observer) {
            this.observer = observer;
        }

        // 发送事件
        @Override
        public void onNext(T t) {
        }

        @Override
        public void onError(Throwable t) {
        }

        @Override
        public boolean tryOnError(Throwable t) {
        }

        @Override
        public void onComplete() {
        }
}
```
从上面可以看出，CreateEmitter通过继承了Java并发包中的原子引用类AtomicReference保证了事件流切断状态Dispose的一致性（这里不理解的话，看到后面讲解Dispose的时候就明白了），并实现了ObservableEmitter接口和Disposable接口，接着我们分析下注释2处的observer.onSubscribe(parent)，这个onSubscribe回调的含义其实就是告诉观察者已经成功订阅了被观察者。再看到注释3处的source.subscribe(parent)这行代码，这里的source其实是ObservableOnSubscribe对象，我们看到ObservableOnSubscribe的subscribe()方法。
#### 2.4.2 ObservableOnSubscribe#subscribe()
```java
Observable observable = Observable.create(new ObservableOnSubscribe<String>() {
    @Override
    public voidsubscribe(ObservableEmitter<String> emitter) throws Exception {
        emitter.onNext("1");
        emitter.onNext("2");
        emitter.onNext("3");
        emitter.onComplete();
    }
});
```
这里面使用到了ObservableEmitter的onNext()方法将事件流发送出去，最后调用了onComplete()方法完成了订阅过程。ObservableEmitter是一个抽象类，实现类就是我们传入的CreateEmitter对象，接下来我们看看CreateEmitter的onNext()方法和onComplete()方法的处理。
#### 2.4.3 CreateEmitter#onNext() && CreateEmitter#onComplete()

```java
static final class CreateEmitter<T>
extends AtomicReference<Disposable>
implements ObservableEmitter<T>, Disposable {
...
@Override
public void onNext(T t) {
    ...
    if (!isDisposed()) {
        //调用观察者的onNext()
        observer.onNext(t);
    }
}

@Override
public void onComplete() {
    if (!isDisposed()) {
        try {
            observer.onComplete();
        } finally {
            dispose();
        }
    }
}
...
}
```
在CreateEmitter的onNext和onComplete方法中首先都要经过一个isDisposed的判断，作用就是看当前的事件流是否被切断（废弃）掉了，默认是不切断的，如果想要切断，可以调用Disposable的dispose()方法将此状态设置为切断（废弃）状态。我们继续看看这个isDisposed内部的处理。

#### 2.4.4 ObservableEmitter#isDisposed()
```java
@Override
public boolean isDisposed() {
    return DisposableHelper.isDisposed(get());
}
```
注意到这里通过get()方法首先从ObservableEmitter的AtomicReference中拿到了保存的Disposable状态。然后交给了DisposableHelper进行判断处理。接下来看看DisposableHelper的处理。

#### 2.4.5 DisposableHelper#isDisposed() && DisposableHelper#set()
```java
public enum DisposableHelper implements Disposable {

    DISPOSED;

    public static boolean isDisposed(Disposable d) {
        // 1
        return d == DISPOSED;
    }

    public static boolean set(AtomicReference<Disposable> field, Disposable d) {
        for (;;) {
            Disposable current = field.get();
            if (current == DISPOSED) {
                if (d != null) {
                    d.dispose();
                }
                return false;
            }
            // 2
            if (field.compareAndSet(current, d)) {
                if (current != null) {
                    current.dispose();
                }
                return true;
            }
        }
    }

    ...

    public static boolean dispose(AtomicReference<Disposable> field) {
        Disposable current = field.get();
        Disposable d = DISPOSED;
        if (current != d) {
            // ...
            current = field.getAndSet(d);
            if (current != d) {
                if (current != null) {
                    current.dispose();
                }
                return true;
            }
        }
        return false;
    }

    ...
}
```
DisposableHelper是一个枚举类，内部只有一个值即DISPOSED, 从上面的分析可知它就是用来标记事件流被切断（废弃）状态的。先看到注释2和注释3处的代码field.compareAndSet(current, d)和field.getAndSet(d)，这里使用了原子引用AtomicReference内部包装的CAS方法处理了标志Disposable的并发读写问题，最后看到注释3处，将我们传入的CreateEmitter这个原子引用类保存的Dispable状态和DisposableHelper内部的DISPOSED进行比较，如果相等，就证明数据流被切断了。为了更进一步理解Disposed的作用，再来看看CreateEmitter中剩余的关键方法。
#### 2.4.6 CreateEmitter
```java
@Override
public void onNext(T t) {
    ...
    // 1
    if (!isDisposed()) {
        observer.onNext(t);
    }
}

@Override
public void onError(Throwable t) {
    if (!tryOnError(t)) {
        // 2
        RxJavaPlugins.onError(t);
    }
}

@Override
public boolean tryOnError(Throwable t) {
    ...
    // 3
    if (!isDisposed()) {
        try {
            observer.onError(t);
        } finally {
            // 4
            dispose();
        }
        return true;
    }
    return false;
}

@Override
public void onComplete() {
    // 5
    if (!isDisposed()) {
        try {
            observer.onComplete();
        } finally {
            // 6
            dispose();
        }
    }
}
```
在注释1、3、5处，onNext()和onError()、onComplete()方法首先都会判断事件流是否被切断的处理，如果事件流此时被切断了，那么onNext()和onComplete()则会退出方法体，不做处理，onError()则会执行到RxJavaPlugins.onError(t)这句代码，内部会直接抛出异常，导致崩溃。如果事件流没有被切断，那么在onError()和onComplete()内部最终会调用到注释4、6处的这句dispose()代码，将事件流进行切断，由此可知，onError()和onComplete()只能调用一个，如果先执行的是onComplete()，再调用onError()的话就会导致异常崩溃。



https://www.jianshu.com/p/34b8b47c268b
https://blog.csdn.net/qidanchederizi/article/details/78170731
http://reactivex.io/RxJava/2.x/javadoc/

http://reactivex.io/documentation/operators.html#tree
https://jsonchao.github.io/2019/01/01/Android%E4%B8%BB%E6%B5%81%E4%B8%89%E6%96%B9%E5%BA%93%E6%BA%90%E7%A0%81%E5%88%86%E6%9E%90%EF%BC%88%E4%BA%94%E3%80%81%E6%B7%B1%E5%85%A5%E7%90%86%E8%A7%A3RxJava%E6%BA%90%E7%A0%81%EF%BC%89/


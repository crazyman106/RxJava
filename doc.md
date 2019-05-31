# RxJava2.0

依赖的四个基本接口(org.reactivestreams:reactive-streams:1.0.2):
* Publisher
* Subscriber
* Subscription
* Processor

主要的是`Publisher`和`Subscriber`
**`Publisher`** **发出**一系列的 **事件**,而 **`Subscriber`** 负责 **处理** 这些 **事件**

**Scheduler类型**
| Scheduler类型 | 	使用方式     | 	含义       | 使用场景     |
|:--------:| -------------:|:--------:| -------------:|
| IoScheduler| Schedulers.io() |io操作线程|读写SD卡文件，查询数据库，访问网络等IO密集型操作|
|  NewThreadScheduler| Schedulers.newThread()  |创建新线程|耗时操作等|
|SingleScheduler  |  Schedulers.single() |	单例线程|只需一个单例线程时|
|ComputationScheduler  |Schedulers.computation()   |CPU计算操作线程|图片压缩取样、xml,json解析等CPU密集型计算|
|  TrampolineScheduler| Schedulers.trampoline()|当前线程|需要在当前线程立即执行任务时|
|HandlerScheduler	|	AndroidSchedulers.mainThread()|Android主线程|更新UI等|



## Flowable与BackPress
背压介绍:
当上下游处在不同的线程中时,通过Observable发射,处理,响应数据流时,如果上游发射的速度快于下游接受处理数据的速度
,这样对于那些没有来得及处理的数据就会造成积压,这些数据既不会丢失,也不会被垃圾回收机制回收,而是存放在一个异步缓存池中
,如果缓存池中的数据一致得不到处理,越积越多就会造成内存溢出,这就是响应式编程中的背压问题.

如果上下游处在同一个线程中，则不会出现背压的问题。因为下游处理完时间后，上游才会发射

### Flowable
**大量数据处理需要用Flowable，而小数据则使用Observable即可**
由于基于Flowable发射的数据流，以及对数据加工处理的各操作符都添加了背压支持，附加了额外的逻辑，其运行效率要比Observable慢得多。
由于只有在上下游运行在不同的线程中，且上游发射数据的速度大于下游接收处理数据的速度时，才会产生背压问题。

所以，如果能够确定：

1. 上下游运行在同一个线程中，
2. 上下游工作在不同的线程中，但是下游处理数据的速度不慢于上游发射数据的速度，
3. 上下游工作在不同的线程中，但是数据流中只有一条数据
则不会产生背压问题，就没有必要使用Flowable，以免影响性能。

**Flowable的使用**
下例使用了`Flowable`来发射事件，大体与`Observable`类似，只是有几点区别：
1. Flowable发射数据时，使用特有的发射器FlowableEmitter，不同于Observable的ObservableEmitter
2. create方法中多了一个BackpressureStrategy类型的参数，该参数负责当BackPress产生的时候，对应的Emitter的处理策略是什么样的
3. onSubscribe中接收的不是Dispose，而是Subscription对象，并且调用了s?.request(10)

```kotlin
 Flowable.create<Int>({ emitter ->
            emitter.onNext(1)
            emitter.onComplete()
        }, BackpressureStrategy.ERROR)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : Subscriber<Int> {
                   override fun onSubscribe(s: Subscription?) { s?.request(10)}

                    override fun onNext(t: Int?) = System.out.println("onNext...$t")

                    override fun onComplete() = System.out.println("onComplete")

                    override fun onError(t: Throwable?) = System.out.println("onError...$t")
                })
```
### BackpressureStrategy背压策略

* MISSING
* ERROR
* BUFFER
* DROP
* LATEST

当上游发送数据的速度快于下游接收数据的速度，且运行在不同的线程中时，Flowable通过自身特有的异步缓存池，来缓存没来得及处理的数据，缓存池的容量上限为128条。

当缓存池的容量超过128条时，就会触发Backpress的应对策略。

BackpressureStrategy的作用便是用来设置Flowable通过异步缓存池缓存数据的策略。在FlowableCreate类中看到，在设置完BackpressureStrategy之后，对应的Strategy会根据映射生成不同Emitter：

* `MISSING ----> MissingEmitter`：
在此策略下，通过Create方法创建的Flowable相当于没有指定背压策略，不会对通过onNext发射的数据做缓存或丢弃处理，需要下游通过背压操作符（onBackpressureBuffer()/onBackpressureDrop()/onBackpressureLatest()）指定背压策略。
* `ERROR ----> ErrorAsyncEmitter`：
在此策略下，如果放入Flowable的异步缓存池中的数据超限了，则会抛出MissingBackpressureException异常
* `DROP ----> DropAsyncEmitter`：
如果Flowable的异步缓存池满了，会丢掉上游发送的数据。由于Emitter都是继承自AutomicLong或者其他的原子数据，所以通过get()得到的就是缓存池数据剩下的数量，如果为0，代表缓存池已经满了。
* `LATEST ----> LatestAsyncEmitter`:
与Drop策略一样，如果缓存池满了，会丢掉将要放入缓存池中的数据，不同的是，不管缓存池的状态如何，LATEST都会将最后一条数据强行放入缓存池中，来保证观察者在接收到完成通知之前，能够接收到Flowable最新发射的一条数据
* `BUFFER ----> BufferAsyncEmitter`：
默认的策略。如果Flowable默认的异步缓存池满了，会通过该Emitter中新增的缓存池暂存数据，它与Observable的异步缓存池一样，可以无限制向里添加数据，不会抛出MissingBackpressureException异常，但会导致OOM
### 背压操作符
Backpress操作符一共有这些:
* onBackpressureBuffer
* onBackpressureDrop
* onBackpressureLatest

主要的作用就是，当Flowable不是通过create创建时，没有传入BackpressStrategy，则可以通过这些操作符来指定BackpressStrategy。例如

```kotlin
Flowable.range(0, 500).onBackpressureDrop()
```

### Flowable的响应式拉取
Flowable在设计的时候，采用了一种新的思路——响应式拉取方式，来设置下游对数据的请求数量，上游可以根据下游的需求量，按需发送数据

如果不显示调用request则默认下游的需求量为零，上游Flowable发射的数据不会交给下游Subscriber处理。而多次调用则会将该数累加：

```kotlin
Flowable.create<Int>({ emitter ->
            repeat(3) {
                Log.e(TAG, "emitter.request:${emitter.requested()}")
                emitter.onNext(it)
            }
            emitter.onComplete()
        }, BackpressureStrategy.ERROR).onBackpressureBuffer()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : Subscriber<Int> {
                    override fun onSubscribe(s: Subscription?) {
                        //  累加到2 
                        s?.request(1)
                        s?.request(1)
                    }

                    override fun onNext(t: Int?) = System.out.println("onNext...$t")

                    override fun onComplete() = System.out.println("onComplete")

                    override fun onError(t: Throwable?) = System.out.println("onError...$t")
                })
```
输出:

```kotlin
onNext...0
onNext...1
```



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

### RxJava的线程切换

```java
Observable.create(new ObservableOnSubscribe<String>() {
    @Override
    public voidsubscribe(ObservableEmitter<String>emitter) throws Exception {
        emitter.onNext("1");
        emitter.onNext("2");
        emitter.onNext("3");
        emitter.onComplete();
    }
}) 
    .subscribeOn(Schedulers.io())
    .observeOn(AndroidSchedulers.mainThread())
    .subscribe(new Observer<String>() {
        @Override
        public void onSubscribe(Disposable d) {
            Log.d(TAG, "onSubscribe");
        }
        @Override
        public void onNext(String s) {
            Log.d(TAG, "onNext : " + s);
        }
        @Override
        public void onError(Throwable e) {
            Log.d(TAG, "onError : " +e.toString());
        }
        @Override
        public void onComplete() {
            Log.d(TAG, "onComplete");
        }
});
```
可以看到，RxJava的线程切换主要分为subscribeOn()和observeOn()方法，首先，来分析下subscribeOn()方法。
#### 1、subscribeOn(Schedulers.io())
在Schedulers.io()方法中，我们需要先传入一个Scheduler调度类，这里是传入了一个调度到io子线程的调度类，我们看看这个Schedulers.io()方法内部是怎么构造这个调度器的
#### 2、Schedulers#io()

```java
static final Scheduler IO;

...

public static Scheduler io() {
    // 1
    return RxJavaPlugins.onIoScheduler(IO);
}

static {
    ...

    // 2
    IO = RxJavaPlugins.initIoScheduler(new IOTask());
}

static final class IOTask implements Callable<Scheduler> {
    @Override
    public Scheduler call() throws Exception {
        // 3
        return IoHolder.DEFAULT;
    }
}

static final class IoHolder {
    // 4
    static final Scheduler DEFAULT = new IoScheduler();
}
```
Schedulers这个类的代码很多，这里我只拿出有关Schedulers。io这个方法涉及的逻辑代码进行讲解。首先，在注释1处，同前面分析的订阅流程的处理一样，只是一个处理hook的逻辑，最终返回的还是传入的这个IO对象。再看到注释2处，在Schedulers的静态代码块中将IO对象进行了初始化，其实质就是新建了一个IOTask的静态内部类，在IOTask的call方法中，也就是注释3处，可以了解到使用了静态内部类的方式把创建的IOScheduler对象给返回出去了。绕了这么大圈子，Schedulers.io方法其实质就是返回了一个IOScheduler对象。

#### 3、Observable#subscribeOn()
```java
  public final Observable<T> subscribeOn(Scheduler scheduler) {
    ...

    return RxJavaPlugins.onAssembly(new ObservableSubscribeOn<T>(this, scheduler));
}
```
在subscribeOn()方法里面，又将ObservableCreate包装成了一个ObservableSubscribeOn对象。我们关注到ObservableSubscribeOn类。
public final class ObservableSubscribeOn<T> extends AbstractObservableWithUpstream<T, T> {
    final Scheduler scheduler;
```java
public final class ObservableSubscribeOn<T> extends AbstractObservableWithUpstream<T, T> {
    final Scheduler scheduler;

    public ObservableSubscribeOn(ObservableSource<T> source, Scheduler scheduler) {
        // 1
        super(source);
        this.scheduler = scheduler;
    }

    @Override
    public void subscribeActual(final Observer<? super T> observer) {
        // 2
        final SubscribeOnObserver<T> parent = new SubscribeOnObserver<T>(observer);

        // 3
        observer.onSubscribe(parent);

        // 4
        parent.setDisposable(scheduler.scheduleDirect(new SubscribeTask(parent)));
    }
...
```
首先，在注释1处，将传进来的source和scheduler保存起来。接着，等到实际订阅的时候，就会执行到这个subscribeActual方法，在注释2处，将我们自定义的Observer包装成了一个SubscribeOnObserver对象。在注释3处，通知观察者订阅了被观察者。在注释4处，内部先创建了一个SubscribeTask对象，来看看它的实现。
#### 5 ObservableSubscribeOn#SubscribeTask
```java
final class SubscribeTask implements Runnable {
    private final SubscribeOnObserver<T> parent;

    SubscribeTask(SubscribeOnObserver<T> parent) {
        this.parent = parent;
    }

    @Override
    public void run() {
        source.subscribe(parent);
    }
}
```
SubscribeTask是ObservableSubscribeOn的内部类，它实质上就是一个任务类，在它的run方法中会执行到source.subscribe(parent)的订阅方法，这个source其实就是我们在ObservableSubscribeOn构造方法中传进来的ObservableCreate对象。接下来看看scheduler.scheduleDirect()内部的处理。
#### 6、Scheduler#scheduleDirect()

```java
public Disposable scheduleDirect(@NonNull Runnable run) {
    return scheduleDirect(run, 0L, TimeUnit.NANOSECONDS);
}

public Disposable scheduleDirect(@NonNull Runnable run, long delay, @NonNull TimeUnit unit) {

    // 1
    final Worker w = createWorker();

    // 2
    final Runnable decoratedRun = RxJavaPlugins.onSchedule(run);

    // 3
    DisposeTask task = new DisposeTask(decoratedRun, w);

    // 4
    w.schedule(task, delay, unit);

    return task;
}
```
这里最后会执行到上面这个scheduleDirect()重载方法。首先，在注释1处，会调用createWorker()方法创建一个工作者对象Worker，它是一个抽象类，这里的实现类就是IoScheduler，下面，我们看看IoScheduler类的createWorker()方法。

#### 6.1、IOScheduler#createWorker()

```java
final AtomicReference<CachedWorkerPool> pool;

...

public IoScheduler(ThreadFactory threadFactory) {
    this.threadFactory = threadFactory;
    this.pool = new AtomicReference<CachedWorkerPool>(NONE);
    start();
}

...

@Override
public Worker createWorker() {
    // 1
    return new EventLoopWorker(pool.get());
}

static final class EventLoopWorker extends Scheduler.Worker {
    ...

    EventLoopWorker(CachedWorkerPool pool) {
        this.pool = pool;
        this.tasks = new CompositeDisposable();
        // 2
        this.threadWorker = pool.get();
    }

}
```
首先，在注释1处调用了pool.get()这个方法，pool是一个CachedWorkerPool类型的原子引用对象，它的作用就是用于缓存工作者对象Worker的。然后，将得到的CachedWorkerPool传入新创建的EventLoopWorker对象中。重点关注一下注释2处，这里讲CachedWorkerPool缓存的threadWorker对象保存起来了。

下面，我们继续分析3.6处代码段的注释2处的代码，这里又是一个关于hook的封装处理，最终还是返回的当前的Runnable对象。在注释3处新建了一个切断任务DisposeTask将decoratedRun和w包装了起来。最后在注释4处调用了工作者的schedule()方法。下面我们来分析下它内部的处理。

#### 6.2、IoScheduler#schedule()
```java
@Override
public Disposable schedule(@NonNull Runnableaction, long delayTime, @NonNull TimeUnit unit){
    ...

    return threadWorker.scheduleActual(action,delayTime, unit, tasks);
}
```
内部调用了threadWorker的scheduleActual()方法，实际上是调用到了父类NewThreadWorker的scheduleActual()方法，我们继续看看NewThreadWorker的scheduleActual()方法中做的事情。

#### 6.2.1、NewThreadWorker#scheduleActual()
```java
public NewThreadWorker(ThreadFactory threadFactory) {
    executor = SchedulerPoolFactory.create(threadFactory);
}


@NonNull
public ScheduledRunnable scheduleActual(final Runnable run, long delayTime, @NonNull TimeUnit unit, @Nullable DisposableContainer parent) {
    Runnable decoratedRun = RxJavaPlugins.onSchedule(run);

    // 1
    ScheduledRunnable sr = new ScheduledRunnable(decoratedRun, parent);


    if (parent != null) {
        if (!parent.add(sr)) {
            return sr;
        }
    }

    Future<?> f;
    try {
        // 2
        if (delayTime <= 0) {
            // 3
            f = executor.submit((Callable<Object>)sr);
        } else {
            // 4
            f = executor.schedule((Callable<Object>)sr, delayTime, unit);
        }
        sr.setFuture(f);
    } catch (RejectedExecutionException ex) {
        if (parent != null) {
            parent.remove(sr);
        }
        RxJavaPlugins.onError(ex);
    }

    return sr;
}
```
在NewThreadWorker的scheduleActual()方法的内部，在注释1处首先会新建一个ScheduledRunnable对象，将Runnable对象和parent包装起来了，这里parent是一个DisposableContainer对象，它实际的实现类是CompositeDisposable类，即一个保存所有事件流是否被切断状态的容器，它内部的实现是使用了RxJava自己定义的一个简单的OpenHashSet类ji进行存储。最后注释2处，判断是否设置了延迟时间，如果设置了，则调用线程池的submit()方法立即进行线程切换，否则，调用schedule()方法进行延时执行线程切换。

#### 7、为什么多次执行subscribeOn()，只有第一次有效？
从上面的分析，我们可以很容易了解到被观察者被订阅时是从最外面的一层（ObservableSubscribeOn）通知到里面的一层（ObservableOnSubscribe），当连续执行了到多次subscribeOn()的时候，其实就是先执行倒数第一次的subscribeOn()方法，直到最后一次执行的subscribeOn()方法肯定会覆盖前面的线程切换
#### 8、observeOn(AndroidSchedulers.mainThread())
```java
public final Observable<T> observeOn(Scheduler scheduler) {
    return observeOn(scheduler, false, bufferSize());
}

public final Observable<T> observeOn(Scheduler scheduler, boolean delayError, int bufferSize) {
    ....

    return RxJavaPlugins.onAssembly(new ObservableObserveOn<T>(this, scheduler, delayError, bufferSize));
}
```
可以看到，observeOn()方法内部最终也是返回了一个ObservableObserveOn对象，我们直接来看看ObservableObserveOn的subscribeActual()方法。

#### 9、ObservableObserveOn#subscribeActual()
```java
@Override
protected void subscribeActual(Observer<? super T> observer) {
    // 1
    if (scheduler instanceof TrampolineScheduler) {
        // 2
        source.subscribe(observer);
    } else {
        // 3
        Scheduler.Worker w = scheduler.createWorker();
        // 4
        source.subscribe(new ObserveOnObserver<T>(observer, w, delayError, bufferSize));
    }
}
```
首先，在注释1处，判断指定的调度器是不是TrampolineScheduler，这是一个不进行线程切换，立即执行当前代码的调度器，。如果是，则会直接调用调用ObservableSubscribeOn的subscribe()方法，如果不是，则会在注释3处创建一个工作者对象。然后在注释4处创建一个新的ObserveOnObserver将SubscribeOnobserver对象包装起来，并传入ObservableSubscribeOn的subscribe()方法进行订阅。接下来看看ObserveOnObserver类的重点方法。

#### 10、ObserveOnObserver
```java
@Override
public void onNext(T t) {
    ...
    if (sourceMode != QueueDisposable.ASYNC) {
        // 1
        queue.offer(t);
    }
    schedule();
}

@Override
public void onError(Throwable t) {
    ...
    schedule();
}

@Override
public void onComplete() {
    ...
    schedule();
}
```
去除非主线逻辑的代码，在ObserveOnObserver的onNext()和onError()、onComplete()方法中最后都会调用到schedule()方法。接着看schedule()方法，其中onNext()还会把消息存放到队列中。

#### 11、ObserveOnObserver#schedule()
```java
void schedule() {
    if (getAndIncrement() == 0) {
        worker.schedule(this);
    }
}
```
这里使用了worker进行调度ObserveOnObserver这个实现了Runnable的任务。worker就是在AndroidSchedulers.mainThread()中创建的，内部其实就是使用Handler进行线程切换的，此处不再赘述了。接着看ObserveOnObserver的run()方法。

#### 12、ObserveOnObserver#run()
```java
@Override
public void run() {
    // 1
    if (outputFused) {
        drainFused();
    } else {
        // 2
        drainNormal();
    }
}
```
在注释1处会先判断outputFused这个标志位，它表示事件流是否被融化掉，默认是false，所以，最后会执行到drainNormal()方法。接着看看drainNormal()方法内部的处理。

#### 13、ObserveOnObserver#drainNormal()
```java
void drainNormal() {
    int missed = 1;

    final SimpleQueue<T> q = queue;

    // 1
    final Observer<? super T> a = downstream;

    ...

    // 2
    v = q.poll();

    ...
    // 3
    a.onNext(v);

    ...
}
```
在注释1处，这里的downstream实际上是从外面传进来的SubscribeOnObserver对象。在注释2处将队列中的消息取出来，接着在注释3处调用了SubscribeOnObserver的onNext方法。最终，会从我们包装类的最外层一直调用到最里面的我们自定义的Observer中的onNext()方法，所以，在observeOn()方法下面的链式代码都会执行到它所指定的线程中。



## Subject
- Subject 可以同时代表 Observer 和 Observable，允许从数据源中多次发送结果给多个观察者。除了 onSubscribe(), onNext(), onError() 和 onComplete() 之外，所有的方法都是线程安全的。此外，你还可以使用 toSerialized() 方法，也就是转换成串行的，将这些方法设置成线程安全的。
    * AsyncSubject:只有当 Subject 调用 onComplete 方法时，才会将 Subject 中的最后一个事件传递给所有的 Observer。(前边的其他事件不会发送)
    * BehaviorSubject:该类有创建时需要一个默认参数，该默认参数会在 Subject 未发送过其他的事件时，向注册的 Observer 发送；新注册的 Observer 不会收到之前发送的事件，这点和 PublishSubject 一致。
    * PublishSubject:不会改变事件的发送顺序；在已经发送了一部分事件之后注册的 Observer 不会收到之前发送的事件。
    * ReplaySubject:无论什么时候注册 Observer 都可以接收到任何时候通过该 Observable 发射的事件。
    * UnicastSubject:只允许一个 Observer 进行监听，在该 Observer 注册之前会将发射的所有的事件放进一个队列中，并在 Observer 注册的时候一起通知给它。



[参考](https://www.jianshu.com/p/34b8b47c268b)
[参考](https://blog.csdn.net/qidanchederizi/article/details/78170731)
[参考](http://reactivex.io/RxJava/2.x/javadoc/)

[参考](http://reactivex.io/documentation/operators.html#tree)
[参考](https://jsonchao.github.io/2019/01/01/Android%E4%B8%BB%E6%B5%81%E4%B8%89%E6%96%B9%E5%BA%93%E6%BA%90%E7%A0%81%E5%88%86%E6%9E%90%EF%BC%88%E4%BA%94%E3%80%81%E6%B7%B1%E5%85%A5%E7%90%86%E8%A7%A3RxJava%E6%BA%90%E7%A0%81%EF%BC%89/
)
[参考](https://mp.weixin.qq.com/s?__biz=MzIwMTAzMTMxMg==&mid=2649492749&idx=1&sn=a4d2e79afd8257b57c6efa57cbff4404&chksm=8eec86f2b99b0fe46f61f324e032af335fbe02c7db1ef4eca60abb4bc99b4d216da7ba32dc88&scene=38#wechat_redirect)

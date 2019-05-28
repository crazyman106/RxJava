package rxbus;

import android.schedulers.AndroidSchedulers;
import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.FlowableTransformer;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.PublishSubject;
import io.reactivex.subjects.Subject;
import org.reactivestreams.Publisher;

import java.util.HashMap;
import java.util.Map;

/**
 * 记录订阅者的信息以便在适当的时机取消订阅
 */
public class RxBus {

    private Map<String, CompositeDisposable> disposableMap = new HashMap<>();
    private static volatile RxBus rxBus;
    // toSerialized();
    private final Subject<Object> subject = PublishSubject.create().toSerialized();


    public static RxBus getInstance() {
        if (rxBus == null) {
            synchronized (RxBus.class) {
                if (rxBus == null) {
                    rxBus = new RxBus();
                }
            }
        }
        return rxBus;
    }


    /**
     * ofType()方法的作用是用来过滤发射的事件的类型，只有指定类型的事件会被发布
     *
     * @param type
     * @param <T>
     * @return
     */
    public <T> Flowable getObservable(Class<T> type) {
        return subject.toFlowable(BackpressureStrategy.BUFFER).ofType(type);
    }


    public void addSubscription(Object o, Disposable disposable) {
        String key = o.getClass().getName();
        if (disposableMap.get(key) != null) {
            disposableMap.get(key).add(disposable);
        } else {
            CompositeDisposable disposables = new CompositeDisposable();
            disposables.add(disposable);
            disposableMap.put(key, disposables);
        }
    }

    public void unSubscribe(Object o) {
        String key = o.getClass().getName();
        if (!disposableMap.containsKey(key)) {
            return;
        }
        if (disposableMap.get(key) != null) {
            disposableMap.get(key).dispose();
        }
        disposableMap.remove(key);
    }

    public void post(Object o) {
        subject.onNext(o);
    }

    public <T> Disposable doSubscribe(Class<T> type, Consumer<T> next, Consumer<Throwable> error) {
        return getObservable(type)
                .compose(upstream -> upstream.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()))
                .subscribe(next, error);
    }
}

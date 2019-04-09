package com.rxjava.demo;

import android.schedulers.AndroidSchedulers;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import com.rxjava.R;
import io.reactivex.*;
import io.reactivex.disposables.Disposable;
import io.reactivex.internal.operators.mixed.CompletableAndThenPublisher;
import io.reactivex.schedulers.Schedulers;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

public class MainActivity extends AppCompatActivity {
    private final String TAG = this.getClass().getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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
        /**
         * 1.创建被观察者
         * 2.创建观察者
         * 3.订阅
         */

    }
}

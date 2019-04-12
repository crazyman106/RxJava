package com.rxjava.demo;

import android.schedulers.AndroidSchedulers;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import com.rxjava.R;
import com.rxjava.demo.create.ObservableObj;
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

        ObservableObj obj = new ObservableObj();
        obj.scan();
        /**
         * 1.创建被观察者
         * 2.创建观察者
         * 3.订阅
         */

    }
}

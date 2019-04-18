package http.schedulers;

import android.schedulers.AndroidSchedulers;
import android.support.annotation.GuardedBy;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import io.reactivex.ObservableTransformer;
import io.reactivex.Scheduler;
import io.reactivex.schedulers.Schedulers;

public class ApiSchedulers implements BaseApiSchedulers {

    @Nullable
    @GuardedBy("ApiSchedulers.class")
    private static ApiSchedulers instance;

    public static synchronized ApiSchedulers getInstance() {
        if (instance == null) {
            instance = new ApiSchedulers();
        }
        return instance;
    }

    @NonNull
    @Override
    public Scheduler computation() {
        return Schedulers.computation();
    }

    @NonNull
    @Override
    public Scheduler io() {
        return Schedulers.io();
    }

    @NonNull
    @Override
    public Scheduler ui() {
        return AndroidSchedulers.mainThread();
    }

    @NonNull
    @Override
    public <T> ObservableTransformer<T, T> applySchedulers() {
        return upstream -> upstream.subscribeOn(io()).observeOn(ui());
    }
}

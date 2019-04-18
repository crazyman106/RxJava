package http.schedulers;

import android.support.annotation.NonNull;
import io.reactivex.ObservableTransformer;
import io.reactivex.Scheduler;

public interface BaseApiSchedulers {

    @NonNull
    Scheduler computation();

    @NonNull
    Scheduler io();

    @NonNull
    Scheduler ui();

    @NonNull
    <T> ObservableTransformer<T, T> applySchedulers();
}
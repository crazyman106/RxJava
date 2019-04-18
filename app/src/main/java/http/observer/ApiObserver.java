package http.observer;

import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;

public abstract class ApiObserver<T> implements Observer<T> {
    protected Disposable disposable;

    @Override
    public void onSubscribe(Disposable d) {
        if (!d.isDisposed()) {
            // TODO 显示progressdialog
            disposable = d;
        }
    }

    @Override
    public void onNext(T t) {
        onSucess(t);
    }

    @Override
    public void onError(Throwable e) {
        // TODO 显示toast,取消progressdialog
    }

    @Override
    public void onComplete() {
        /// TODO 取消progressdialog
    }


    abstract void onSucess(T t);
}

package http.response;

import http.exception.ApiException;
import http.exception.ApiExceptionEngine;
import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.ObservableTransformer;
import io.reactivex.functions.Function;

/**
 * Created by Zaifeng on 2018/2/28.
 * 对返回的数据进行处理，区分异常的情况。
 */

public class ApiResponseTransformer {

    public static <T> ObservableTransformer<ApiResponse<T>, T> handleResult() {
        return upstream -> upstream
                .onErrorResumeNext(new ErrorResumeFunction<>())
                .flatMap(new ResponseFunction<>());
    }


    /**
     * 非服务器产生的异常，比如本地无网络,网络请求超时，Json数据解析错误等等。
     *
     * @param <T>
     */
    private static class ErrorResumeFunction<T> implements Function<Throwable, ObservableSource<? extends ApiResponse<T>>> {

        @Override
        public ObservableSource<? extends ApiResponse<T>> apply(Throwable throwable) throws Exception {
            return Observable.error(ApiExceptionEngine.handleException(throwable));
        }
    }

    /**
     * 服务器有正常格式数据返回:数据返回正确,或者是自定义错误
     *
     * @param <T>
     */
    private static class ResponseFunction<T> implements Function<ApiResponse<T>, ObservableSource<T>> {

        @Override
        public ObservableSource<T> apply(ApiResponse<T> mResponse) throws Exception {
            int code = mResponse.getCode();
            String message = mResponse.getMessage();
            if (code == 200) {
                return Observable.just(mResponse.getData());
            } else {
                return Observable.error(new ApiException(code, message));
            }
        }
    }
}

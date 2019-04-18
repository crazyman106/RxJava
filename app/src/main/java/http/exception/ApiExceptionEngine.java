package http.exception;

import android.net.ParseException;
import com.google.gson.JsonParseException;
import org.json.JSONException;

import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

public class ApiExceptionEngine {
    public static ApiException handleException(Throwable e) {
        ApiException ex;
        if (e instanceof JsonParseException
                || e instanceof JSONException
                || e instanceof ParseException) {
            //解析错误
            ex = new ApiException(ApiExceptionCode.PARSE_ERROR.getCode(), e.getMessage());
        } else if (e instanceof ConnectException) {
            //网络错误
            ex = new ApiException(ApiExceptionCode.NETWORK_ERROR.getCode(), e.getMessage());
        } else if (e instanceof UnknownHostException || e instanceof SocketTimeoutException) {
            //连接错误
            ex = new ApiException(ApiExceptionCode.NETWORK_ERROR.getCode(), e.getMessage());
        } else {
            //未知错误
            ex = new ApiException(ApiExceptionCode.UNKNOWN.getCode(), e.getMessage());
        }
        return ex;
    }
}

package http.api;

import http.response.ApiResponse;
import io.reactivex.Observable;
import retrofit2.http.*;

import java.util.List;

public interface ApiService {

    public static String HOST = "https://xxxx.api.xxx.com/xxxx/";

    @GET("xxxxx")
    Observable<ApiResponse<List<String>>> getUserList(@Field("userId") String userId);

    @FormUrlEncoded
    @POST("xxxxxx")
    Observable<ApiResponse<String>> uploadTxt(@Field("xxx") String xxx, @Field("xxxx") String xxxx);
}

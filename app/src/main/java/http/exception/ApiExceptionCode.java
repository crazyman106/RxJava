package http.exception;

public enum ApiExceptionCode {
    UNKNOWN("未知错误", 1000), PARSE_ERROR("解析错误", 1001), NETWORK_ERROR("网络错误", 1002), HTTP_ERROR("协议错误", 1003);
    private String message;
    private int code;

    private ApiExceptionCode(String message, int code) {
        this.message = message;
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}

package comment;

import java.util.Map;

/**
 * 公共的返回类
 *
 * @author zhangcanlong
 * @since 2019-10-31 修改，添加描述
 **/
public class CommonResponse<T> {

    /**
     * 0表示成功，非1表示失败
     **/
    private Integer code;

    /**
     * 成功或失败的信息，成功返回"succeed"，一般失败返回"failed" ,其他失败根据具体业务场景返回信息
     **/
    private String msg;

    /**
     * 返回的结果数据
     **/
    private T resultData;

    /**
     * 信息相关的数据，一般都是失败信息相关的数据
     **/
    private Map<String, String> msgData;

    /**
     * 构造方法
     **/
    private CommonResponse() {
    }

    /**
     * 包含参数的构造方法
     **/
    private CommonResponse(int code, String msg, T t, Map<String, String> msgData) {
        this.code = code;
        this.msg = msg;
        this.resultData = t;
        this.msgData = msgData;
    }


    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public T getResultData() {
        return resultData;
    }

    public void setResultData(T resultData) {
        this.resultData = resultData;
    }

    public static CommonResponse failedResult() {
        return failedResult("FAILED");
    }

    public static CommonResponse failedResult(String msg) {
        return failedResult(msg, -1);
    }

    /**
     * 泛型的公共的失败响应信息
     *
     * @param msg 信息
     * @return 包含失败信息的公共响应类
     **/
    public static <T> CommonResponse<T> failedResultWithGenerics(String msg) {
        return new CommonResponse<>(-1, msg, null, null);
    }

    /**
     * 泛型的公共的失败响应信息
     *
     * @param msg 错误信息
     * @param code    错误代码
     * @return 包含失败信息的公共响应类
     **/
    public static <T> CommonResponse<T> failedResultWithGenerics(String msg, int code) {
        return new CommonResponse<>(code, msg, null, null);
    }

    public static CommonResponse failedResult(String msg, int code) {
        return failedResult(msg, code, null);
    }

    public static CommonResponse failedResult(String msg, int code, Map<String, String> msgData) {
        return resultTemplate(null, msg, code, msgData);
    }

    public static CommonResponse succeedResult() {
        return succeedResult("SUCCESS");
    }

    public static CommonResponse succeedResult(String msg) {
        return succeedResult(msg, 0);
    }

    public static CommonResponse succeedResult(String msg, int code) {
        return succeedResult(null, msg, code);
    }

    public static CommonResponse succeedResult(Object rst) {
        return succeedResult(rst, "SUCCESS", 0);
    }

    /**
     * 泛型构造的返回成功的数据
     *
     * @param t 传递的数据的泛型
     * @return 含有数据的公共响应信息
     **/
    public static <T> CommonResponse<T> succeedResultWithGenerics(T t) {
        return new CommonResponse<>(1, "SUCCESS", t, null);
    }

    public static <T> CommonResponse<T> succeedResultWithGenerics(T t, String msg) {
        return new CommonResponse<>(1, msg, t, null);
    }

    public static CommonResponse succeedResult(Object rst, String msg) {
        return succeedResult(rst, msg, 0);
    }

    public static CommonResponse succeedResult(Object rst, String msg, int code) {
        return resultTemplate(rst, msg, code, null);
    }

    public static CommonResponse resultTemplate(Object rst, String msg, int code, Map<String, String> msgData) {
        CommonResponse res = new CommonResponse();
        res.setCode(code);
        res.setResultData(rst);
        res.setMsg(msg);
        res.setMsgData(msgData);
        return res;
    }


    public Map<String, String> getMsgData() {
        return msgData;
    }

    public void setMsgData(Map<String, String> msgData) {
        this.msgData = msgData;
    }


    @Override
    public String toString() {
        return "CommonResponse{" +
                "code=" + code +
                ", msg='" + msg + '\'' +
                ", resultData=" + resultData +
                ", msgData=" + msgData +
                '}';
    }
}

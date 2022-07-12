package comment;

public interface IReturnCode {
    /**
     * 获取响应码
     * @return 响应码
     */
    int getCode();

    /**
     * 获取响应描述
     * @return 响应描述
     */
    String getMsg();
}

package comment.exception;


import comment.IReturnCode;

/**
 * 异常基础类
 *
 */
public abstract class BaseException extends RuntimeException {

    private static final long serialVersionUID = -2866380845736322836L;

    protected int code = -1;

    protected String message;

    public BaseException() {
        super();
    }

    public BaseException(Throwable cause) {
        super(cause);
    }

    public BaseException(String message, Throwable cause) {
        super(message, cause);
    }

    public BaseException(String message) {
        super(message);
        this.message = message;
    }

    public BaseException(int code, String message) {
        super(message);
        this.code = code;
        this.message = message;
    }

    public BaseException(int code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
        this.message = message;
    }

    public BaseException(IReturnCode returnCode) {
        super(returnCode.getMsg());
        this.code = returnCode.getCode();
        this.message = returnCode.getMsg();
    }

    public BaseException(IReturnCode returnCode, String message) {
        super(message);
        this.code = returnCode.getCode();
        this.message = message;
    }

    public BaseException(IReturnCode returnCode, String message, Throwable cause) {
        super(message, cause);
        this.code = returnCode.getCode();
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    @Override
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}

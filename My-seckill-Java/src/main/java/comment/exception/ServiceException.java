package comment.exception;


import comment.IReturnCode;

/**
 */
public class ServiceException extends BaseException {

    private static final long serialVersionUID = -11877886830329992L;

    public ServiceException() {
        super();
    }

    public ServiceException(Throwable cause) {
        super(cause);
    }

    public ServiceException(String message, Throwable cause) {
        super(message, cause);
    }

    public ServiceException(String message) {
        super(message);
        this.message = message;
    }

    public ServiceException(int code, String message) {
        super(message);
        this.code = code;
        this.message = message;
    }

    public ServiceException(int code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
        this.message = message;
    }

    public ServiceException(IReturnCode returnCode) {
        super(returnCode.getMsg());
        this.code = returnCode.getCode();
        this.message = returnCode.getMsg();
    }

    public ServiceException(IReturnCode returnCode, String message) {
        super(message);
        this.code = returnCode.getCode();
        this.message = message;
    }

    public ServiceException(IReturnCode returnCode, String message, Throwable cause) {
        super(message, cause);
        this.code = returnCode.getCode();
        this.message = message;
    }
}

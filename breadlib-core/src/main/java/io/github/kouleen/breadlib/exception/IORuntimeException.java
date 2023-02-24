package io.github.kouleen.breadlib.exception;

/**
 * @author zhangqing
 * @since 2023/2/16 14:02
 */
public class IORuntimeException extends RuntimeException{

    private static final long serialVersionUID = 8247610319171014183L;

    public IORuntimeException(Throwable throwable) {
        super(String.format("%s : %s",throwable.getClass().getSimpleName(),throwable.getMessage()),throwable);
    }

    public IORuntimeException(String message) {
        super(message);
    }

    public IORuntimeException(String messageTemplate, Object... params) {
        super(String.format(messageTemplate, params));
    }

    public IORuntimeException(String message, Throwable throwable) {
        super(message, throwable);
    }

    public IORuntimeException(Throwable throwable, String messageTemplate, Object... params) {
        super(String.format(messageTemplate, params), throwable);
    }

    /**
     * 导致这个异常的异常是否是指定类型的异常
     *
     * @param clazz 异常类
     * @return 是否为指定类型异常
     */
    public boolean causeInstanceOf(Class<? extends Throwable> clazz) {
        final Throwable cause = this.getCause();
        return null != clazz && clazz.isInstance(cause);
    }
}

package io.github.kouleen.breadlib.exception;

/**
 * @author zhangqing
 * @since 2023/2/15 17:13
 */
public class SingletonException extends RuntimeException{

    /**
     * Create a new SingletonException with the specified message.
     * @param message the detail message
     */
    public SingletonException(String message){
        super(message);
    }

    /**
     * Create a new SingletonException with the specified message
     * and root cause.
     * @param message the detail message
     * @param throwable the root cause
     */
    public SingletonException(String message,Throwable throwable) {
        super(message, throwable);
    }
}

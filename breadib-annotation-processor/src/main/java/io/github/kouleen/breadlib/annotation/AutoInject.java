package io.github.kouleen.breadlib.annotation;

import java.lang.annotation.*;

/**
 * @author zhangqing
 * @since 2023/2/23 12:24
 */
@Documented
@Target({ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface AutoInject {

}

package io.github.kouleen.breadlib.processor;


import io.github.kouleen.breadlib.annotation.Main;

/**
 * @author zhangqing
 * @since 2023/2/23 12:26
 */
public class InjectManagerProvider {

    public static void loadInjectObject(Class<?> clazz){
        Main annotation = clazz.getAnnotation(Main.class);
    }
}

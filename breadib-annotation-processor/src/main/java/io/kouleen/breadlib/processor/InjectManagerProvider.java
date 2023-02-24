package io.kouleen.breadlib.processor;


import io.kouleen.breadlib.annotation.Main;

/**
 * @author zhangqing
 * @since 2023/2/23 12:26
 */
public class InjectManagerProvider {

    public static void loadInjectObject(Class<?> clazz){
        Main annotation = clazz.getAnnotation(Main.class);
    }
}

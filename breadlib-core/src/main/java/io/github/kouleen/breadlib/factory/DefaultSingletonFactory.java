package io.github.kouleen.breadlib.factory;

import io.github.kouleen.breadlib.exception.SingletonException;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author zhangqing
 * @since 2023/2/16 10:13
 */
public class DefaultSingletonFactory implements SingletonFactory{

    private static class SingletonMap{
        private static final Map<String, Object> singletonMap = new ConcurrentHashMap<>();
    }

    /**
     * Return an instance, which may be shared or independent, of the specified singleton.
     * <p>This method allows a Spring BeanFactory to be used as a replacement for the
     * Singleton or Prototype design pattern. Callers may retain references to
     * returned objects in the case of Singleton singletons.
     * <p>Translates aliases back to the corresponding canonical singleton name.
     * <p>Will ask the parent factory if the singleton cannot be found in this factory instance.
     *
     * @param name the name of the singleton to retrieve
     * @return an instance of the singleton
     * @throws SingletonException if the singleton could not be obtained
     */
    @Override
    public Object getSingleton(String name) throws SingletonException {
        return SingletonMap.singletonMap.get(name);
    }

    public void setSingleton(String name,Object singleton) throws SingletonException {
        SingletonMap.singletonMap.put(name,singleton);
    }

    public String instanceofImplement(Class<?> clazz){
        String className = null;
        for (Object object : SingletonMap.singletonMap.values()) {
            try {
                className = clazz.cast(object).getClass().getName();
            }catch (ClassCastException classCastException){
            }
        }
        return className;
    }
}

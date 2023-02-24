package io.kouleen.breadlib.factory;


import io.kouleen.breadlib.exception.SingletonException;

/**
 * @author zhangqing
 * @since 2023/2/23 12:31
 */
public interface SingletonFactory {

    /**
     * Return an instance, which may be shared or independent, of the specified singleton.
     * <p>This method allows a Spring BeanFactory to be used as a replacement for the
     * Singleton or Prototype design pattern. Callers may retain references to
     * returned objects in the case of Singleton singletons.
     * <p>Translates aliases back to the corresponding canonical singleton name.
     * <p>Will ask the parent factory if the singleton cannot be found in this factory instance.
     * @param name the name of the singleton to retrieve
     * @return an instance of the singleton
     * @throws SingletonException if the singleton could not be obtained
     */
    Object getSingleton(String name) throws SingletonException;

    void setSingleton(String name,Object singleton) throws SingletonException;

    String instanceofImplement(Class<?> clazz);
}

package io.kouleen.breadlib;

import io.kouleen.breadlib.annotation.AutoInject;
import io.kouleen.breadlib.annotation.Component;
import io.kouleen.breadlib.annotation.Main;
import io.kouleen.breadlib.exception.ClassLoaderException;
import io.kouleen.breadlib.factory.DefaultSingletonFactory;
import io.kouleen.breadlib.factory.SingletonFactory;
import io.kouleen.breadlib.utils.AssertUtils;
import io.kouleen.breadlib.utils.CollectionUtils;
import io.kouleen.breadlib.utils.ObjectUtils;
import io.kouleen.breadlib.utils.StringUtils;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * @author zhangqing
 * @since 2023/2/23 12:30
 */
public class BreadLibApplication {

    private final ClassLoader[] classLoader;

    private static final SingletonFactory singletonFactory = new DefaultSingletonFactory();

    public BreadLibApplication(ClassLoader[] classLoader) {
        this.classLoader = classLoader;
    }

    public static BreadLibApplication run(Object object, ClassLoader... classLoader) {
        singletonFactory.setSingleton(object.getClass().getName(), object);
        return new BreadLibApplication(ObjectUtils.isEmpty(classLoader) ? new ClassLoader[]{Thread.currentThread().getContextClassLoader()} : classLoader).runMain(object.getClass());
    }

    public static BreadLibApplication run(Class<?> primarySources, ClassLoader... classLoader) {
        return new BreadLibApplication(classLoader).runMain(primarySources);
    }

    public BreadLibApplication runMain(Class<?> primarySources) {
        BreadLibApplication breadLibApplication = new BreadLibApplication(new ClassLoader[]{primarySources.getClassLoader()});
        AssertUtils.notNull(primarySources, "PrimarySources must not be null");
        Main annotation = primarySources.getAnnotation(Main.class);
        List<String> packageList = new ArrayList<>();
        String[] packages = annotation.packages();
        if (!ObjectUtils.isEmpty(packages)) {
            packageList.addAll(Arrays.asList(packages));
        }
        String value = annotation.value();
        if (StringUtils.hasText(value)) {
            packageList.add(value);
        }
        if (CollectionUtils.isEmpty(packageList)) {
            packageList.add(primarySources.getPackage().getName());
        }

        for (String packageName : packageList) {
            List<Class<?>> aClass = getClass(packageName);
            for (Class<?> clazz : aClass) {
                this.componentInstance(clazz);
            }
            this.autoInjectField(aClass);
        }
        return breadLibApplication;
    }

    public List<Class<?>> getClass(String packageName) {
        // 第一个class类的集合
        List<Class<?>> classes = new ArrayList<>();
        // 是否循环迭代
        boolean recursive = true;
        // 获取包的名字 并进行替换
        String packageDirName = packageName.replace('.', '/');
        // 定义一个枚举的集合 并进行循环来处理这个目录下的things
        Enumeration<URL> dirs;
        try {
            dirs = classLoader[0].getResources(packageDirName);
            if (!dirs.hasMoreElements()) {
                throw new ClassLoaderException("There is ClassLoader problem");
            }
            while (dirs.hasMoreElements()) {
                // 获取下一个元素
                URL url = dirs.nextElement();
                // 得到协议的名称
                String protocol = url.getProtocol();
                // 如果是以文件的形式保存在服务器上
                if ("file".equals(protocol)) {
                    // 获取包的物理路径
                    String filePath = URLDecoder.decode(url.getFile(), "UTF-8");
                    // 以文件的方式扫描整个包下的文件 并添加到集合中
                    findAndAddClassesInPackageByFile(packageName, filePath, recursive, classes);
                }
                if ("jar".equals(protocol)) {
                    JarFile jar;
                    try {
                        jar = ((JarURLConnection) url.openConnection()).getJarFile();
                        // 从此jar包 得到一个枚举类
                        Enumeration<JarEntry> entries = jar.entries();
                        // 同样的进行循环迭代
                        while (entries.hasMoreElements()) {
                            // 获取jar里的一个实体 可以是目录 和一些jar包里的其他文件 如META-INF等文件
                            JarEntry entry = entries.nextElement();
                            String name = entry.getName();
                            // 如果是以/开头的
                            if (name.charAt(0) == '/') {
                                // 获取后面的字符串
                                name = name.substring(1);
                            }
                            // 如果前半部分和定义的包名相同
                            if (name.startsWith(packageDirName)) {
                                int idx = name.lastIndexOf('/');
                                // 如果以"/"结尾 是一个包
                                if (idx != -1) {
                                    // 获取包名 把"/"替换成"."
                                    packageName = name.substring(0, idx).replace('/', '.');
                                }
                                // 如果可以迭代下去 并且是一个包
                                if ((idx != -1) || recursive) {
                                    // 如果是一个.class文件 而且不是目录
                                    if (name.endsWith(".class") && !entry.isDirectory()) {
                                        // 去掉后面的".class" 获取真正的类名
                                        String className = name.substring(packageName.length() + 1, name.length() - 6);
                                        try {
                                            classes.add(Class.forName(packageName + '.' + className));
                                        } catch (ClassNotFoundException classNotFoundException) {
                                            classNotFoundException.printStackTrace();
                                        }
                                    }
                                }
                            }
                        }
                    } catch (IOException ioException) {
                        ioException.printStackTrace();
                    }
                }
            }
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
        return classes;
    }

    private void findAndAddClassesInPackageByFile(String packageName, String packagePath, boolean recursive, List<Class<?>> classes) {
        // 获取此包的目录 建立一个File
        File dir = new File(packagePath);
        // 如果不存在或者 也不是目录就直接返回
        if (!dir.exists() || !dir.isDirectory()) {
            return;
        }
        // 如果存在 就获取包下的所有文件 包括目录
        File[] dirfiles = dir.listFiles(new FileFilter() {
            // 自定义过滤规则 如果可以循环(包含子目录) 或则是以.class结尾的文件(编译好的java类文件)
            @Override
            public boolean accept(File file) {
                return (recursive && file.isDirectory()) || (file.getName().endsWith(".class"));
            }
        });
        // 循环所有文件
        for (File file : dirfiles) {
            // 如果是目录 则继续扫描
            if (file.isDirectory()) {
                findAndAddClassesInPackageByFile(packageName + "." + file.getName(), file.getAbsolutePath(), recursive, classes);
            } else {
                // 如果是java类文件 去掉后面的.class 只留下类名
                String className = file.getName().substring(0, file.getName().length() - 6);
                try {
                    // 添加到集合中去
                    Class<?> clazz = Class.forName(packageName + '.' + className);
                    classes.add(clazz);
                } catch (ClassNotFoundException classNotFoundException) {
                    classNotFoundException.printStackTrace();
                }
            }
        }
    }

    public Object componentInstance(Class<?> clazz) {
        Object object = null;
        try {
            Component component = clazz.getAnnotation(Component.class);
            Main componentMain = clazz.getAnnotation(Main.class);
            if (!ObjectUtils.isEmpty(component) || !ObjectUtils.isEmpty(componentMain)) {
                String className = clazz.getName();
                Object singleton = singletonFactory.getSingleton(className);
                if (!ObjectUtils.isEmpty(singleton)) {
                    return singleton;
                }
                Constructor<?> constructor = clazz.getConstructor();
                object = constructor.newInstance();
                System.out.println(className);
                singletonFactory.setSingleton(className, object);
            }
        } catch (Exception exception) {
            exception.printStackTrace();
        }
        return object;
    }

    public void autoInjectField(List<Class<?>> classList) {
        for (Class<?> clazz : classList) {
            try {
                Component component = clazz.getAnnotation(Component.class);
                Main componentMain = clazz.getAnnotation(Main.class);
                if (!ObjectUtils.isEmpty(component) || !ObjectUtils.isEmpty(componentMain)) {
                    Object object = singletonFactory.getSingleton(clazz.getName());
                    Field[] declaredFields = clazz.getDeclaredFields();
                    for (Field declaredField : declaredFields) {
                        AutoInject annotation = declaredField.getAnnotation(AutoInject.class);
                        if (!ObjectUtils.isEmpty(annotation)) {
                            declaredField.setAccessible(true);
                            Class<?> type = declaredField.getType();
                            String className = singletonFactory.instanceofImplement(type);
                            Object instance = singletonFactory.getSingleton(className);
                            declaredField.set(object, instance);
                        }
                    }
                }
            } catch (Exception exception) {
                exception.printStackTrace();
            }
        }
    }
}

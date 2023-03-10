package io.github.kouleen.breadlib;

import io.github.kouleen.breadlib.exception.ClassLoaderException;
import io.github.kouleen.breadlib.factory.DefaultSingletonFactory;
import io.github.kouleen.breadlib.factory.SingletonFactory;
import io.github.kouleen.breadlib.utils.AssertUtils;
import io.github.kouleen.breadlib.utils.CollectionUtils;
import io.github.kouleen.breadlib.utils.ObjectUtils;
import io.github.kouleen.breadlib.utils.StringUtils;
import io.github.kouleen.breadlib.annotation.AutoInject;
import io.github.kouleen.breadlib.annotation.Component;
import io.github.kouleen.breadlib.annotation.Main;

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
        // ?????????class????????????
        List<Class<?>> classes = new ArrayList<>();
        // ??????????????????
        boolean recursive = true;
        // ?????????????????? ???????????????
        String packageDirName = packageName.replace('.', '/');
        // ??????????????????????????? ??????????????????????????????????????????things
        Enumeration<URL> dirs;
        try {
            dirs = classLoader[0].getResources(packageDirName);
            if (!dirs.hasMoreElements()) {
                throw new ClassLoaderException("There is ClassLoader problem");
            }
            while (dirs.hasMoreElements()) {
                // ?????????????????????
                URL url = dirs.nextElement();
                // ?????????????????????
                String protocol = url.getProtocol();
                // ????????????????????????????????????????????????
                if ("file".equals(protocol)) {
                    // ????????????????????????
                    String filePath = URLDecoder.decode(url.getFile(), "UTF-8");
                    // ????????????????????????????????????????????? ?????????????????????
                    findAndAddClassesInPackageByFile(packageName, filePath, recursive, classes);
                }
                if ("jar".equals(protocol)) {
                    JarFile jar;
                    try {
                        jar = ((JarURLConnection) url.openConnection()).getJarFile();
                        // ??????jar??? ?????????????????????
                        Enumeration<JarEntry> entries = jar.entries();
                        // ???????????????????????????
                        while (entries.hasMoreElements()) {
                            // ??????jar?????????????????? ??????????????? ?????????jar????????????????????? ???META-INF?????????
                            JarEntry entry = entries.nextElement();
                            String name = entry.getName();
                            // ????????????/?????????
                            if (name.charAt(0) == '/') {
                                // ????????????????????????
                                name = name.substring(1);
                            }
                            // ??????????????????????????????????????????
                            if (name.startsWith(packageDirName)) {
                                int idx = name.lastIndexOf('/');
                                // ?????????"/"?????? ????????????
                                if (idx != -1) {
                                    // ???????????? ???"/"?????????"."
                                    packageName = name.substring(0, idx).replace('/', '.');
                                }
                                // ???????????????????????? ??????????????????
                                if ((idx != -1) || recursive) {
                                    // ???????????????.class?????? ??????????????????
                                    if (name.endsWith(".class") && !entry.isDirectory()) {
                                        // ???????????????".class" ?????????????????????
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
        // ????????????????????? ????????????File
        File dir = new File(packagePath);
        // ????????????????????? ??????????????????????????????
        if (!dir.exists() || !dir.isDirectory()) {
            return;
        }
        // ???????????? ?????????????????????????????? ????????????
        File[] dirfiles = dir.listFiles(new FileFilter() {
            // ????????????????????? ??????????????????(???????????????) ????????????.class???????????????(????????????java?????????)
            @Override
            public boolean accept(File file) {
                return (recursive && file.isDirectory()) || (file.getName().endsWith(".class"));
            }
        });
        // ??????????????????
        for (File file : dirfiles) {
            // ??????????????? ???????????????
            if (file.isDirectory()) {
                findAndAddClassesInPackageByFile(packageName + "." + file.getName(), file.getAbsolutePath(), recursive, classes);
            } else {
                // ?????????java????????? ???????????????.class ???????????????
                String className = file.getName().substring(0, file.getName().length() - 6);
                try {
                    // ?????????????????????
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

package io.github.kouleen.breadlib.processor;

import io.github.kouleen.breadlib.annotation.Main;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import java.util.Set;

/**
 * @author zhangqing
 * @since 2023/2/23 12:26
 */
@SupportedAnnotationTypes("com.kouleen.breadlib.annotation.PluginMain")
@SupportedSourceVersion(SourceVersion.RELEASE_8)
public class ApplicationProcessor extends AbstractProcessor {

    private boolean isProcess = false;

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        Set<? extends Element> elements = roundEnv.getElementsAnnotatedWith(Main.class);
        for (Element element : elements) {
            if (isProcess) {
                throw new IllegalStateException("There can only be one PluginMain");
            }
            isProcess = true;
            System.out.println("##############################");
            System.out.println("#          BreadLib          #");
            System.out.println("##############################");
            Elements elementUtils = processingEnv.getElementUtils();
            PackageElement packageElement = elementUtils.getPackageOf(element);
            // 获取PluginMain注解所在的包名
            String packageName = packageElement.getQualifiedName().toString();

        }
        return true;
    }
}

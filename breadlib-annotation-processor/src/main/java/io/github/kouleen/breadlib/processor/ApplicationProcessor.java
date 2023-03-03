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
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.Set;

/**
 * @author zhangqing
 * @since 2023/2/23 12:26
 */
@SupportedAnnotationTypes("io.github.kouleen.breadlib.annotation.Main")
@SupportedSourceVersion(SourceVersion.RELEASE_8)
public class ApplicationProcessor extends AbstractProcessor {

    private boolean isProcess = false;

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        Set<? extends Element> elements = roundEnv.getElementsAnnotatedWith(Main.class);
        for (Element element : elements) {
            if (isProcess) {
                throw new IllegalStateException("There can only be one plugin Main");
            }
            isProcess = true;
            File file = new File(new File("/").getAbsolutePath() + "/io.github.kouleen.breadlib.txt");
            try {
                if(!file.exists()){
                    file.createNewFile();
                }
            }catch (Exception exception){
                exception.printStackTrace();
            }
            Elements elementUtils = processingEnv.getElementUtils();
            PackageElement packageElement = elementUtils.getPackageOf(element);
            // 获取PluginMain注解所在的包名
            String packageName = packageElement.getQualifiedName().toString();
            try(BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(file))) {
                System.out.println("##############################");
                bufferedWriter.write("##############################\n");
                System.out.println("#          BreadLib          #");
                bufferedWriter.write("#          BreadLib          #\n");
                System.out.println("##############################");
                bufferedWriter.write("##############################\n");
                bufferedWriter.write("\n");
                bufferedWriter.write("\n");
                bufferedWriter.write("version: 1.0.0\n");
                bufferedWriter.write("author : kouleen\n");
                bufferedWriter.write("author : kouleen\n");
            }catch (Exception exception){
                exception.printStackTrace();
            }
        }
        return true;
    }
}

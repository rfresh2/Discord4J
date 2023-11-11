package discord4j.core;

import org.graalvm.nativeimage.hosted.Feature;
import org.graalvm.nativeimage.hosted.RuntimeReflection;
import org.immutables.value.Generated;
import org.reflections.Reflections;

public class DiscordJsonFeature implements Feature {

    public void beforeAnalysis(BeforeAnalysisAccess access) {
        try {
            Reflections reflections = new Reflections("discord4j.");
            reflections.getTypesAnnotatedWith(Generated.class).forEach(this::registerClass);
        } catch (final Exception e) {
            System.out.println("Failed to register discord4j.discordjson");
        }
    }

    private void registerClass(Class<?> clazz) {
        try {
            RuntimeReflection.register(clazz);
            RuntimeReflection.registerAllDeclaredFields(clazz);
            RuntimeReflection.registerAllFields(clazz);
            RuntimeReflection.registerAllDeclaredMethods(clazz);
            RuntimeReflection.registerAllMethods(clazz);
            RuntimeReflection.registerAllDeclaredConstructors(clazz);
            RuntimeReflection.registerAllConstructors(clazz);
        } catch (final Throwable e) {
            System.out.println("Failed to register " + clazz.getName());
        }
    }
}

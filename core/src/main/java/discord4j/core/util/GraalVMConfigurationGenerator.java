package discord4j.core.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import discord4j.gateway.json.jackson.PayloadDeserializer;
import org.immutables.value.Generated;
import org.immutables.value.Value;
import org.reflections.Reflections;
import org.reflections.scanners.Scanners;
import org.reflections.util.ConfigurationBuilder;

import java.io.FileWriter;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

public class GraalVMConfigurationGenerator {

    public static void main(String[] args) throws IOException {
        Path outputPath = Paths.get(args[0]);
        final List<Map<String, Object>> jsonArrayOutput = new ArrayList<>();

        Set<Class<?>> discordJsonClasses = scanClasses("discord4j.discordjson");
        processClasses(discordJsonClasses, jsonArrayOutput);

        final String[] otherPackages = new String[]{"discord4j.gateway", "discord4j.rest", "discord4j.voice", "discord4j.core"};
        final Reflections otherPackageReflections = buildReflections(otherPackages);
        final Set<Class<? extends StdDeserializer>> deserializers = otherPackageReflections.getSubTypesOf(StdDeserializer.class);
        final Reflections otherPackageReflections2 = buildReflections2(otherPackages); // why is this needed lmao reflections is broken on jdk17
        final Set<Class<?>> otherDeserializeClasses = otherPackageReflections2.getTypesAnnotatedWith(JsonDeserialize.class);
        final Set<Class<?>> voiceClasses = scanClasses("discord4j.voice.json");
        final Set<Class<?>> restClasses = scanClasses("discord4j.rest.json.response");
        final Set<Class<?>> joinedClasses = new LinkedHashSet<>(deserializers);
        joinedClasses.addAll(otherDeserializeClasses);
        joinedClasses.addAll(voiceClasses);
        joinedClasses.addAll(restClasses);
        processClasses(joinedClasses, jsonArrayOutput);


        Files.deleteIfExists(outputPath);
        // write the configuration to file
        try (FileWriter writer = new FileWriter(outputPath.toFile())) {
            new ObjectMapper().writerWithDefaultPrettyPrinter().writeValue(writer, jsonArrayOutput);
        }
    }

    private static Set<Class<?>> scanClasses(final String packagePrefix) {
        return scanAllClasses(buildReflections(packagePrefix), packagePrefix);
    }

    private static Reflections buildReflections(String... packages) {
        return new Reflections(
            new ConfigurationBuilder()
                .setScanners(Scanners.SubTypes.filterResultsBy(c -> true))
                .forPackages(packages));
    }

    private static Reflections buildReflections2(String... packages) {
        return new Reflections(
            new ConfigurationBuilder()
                .forPackages(packages));
    }

    private static Set<Class<?>> scanAllClasses(final Reflections reflections, final String packagePrefix) {
        return reflections.getAllTypes().stream()
            .filter(c -> c.startsWith(packagePrefix))
            .map(c -> {
                try {
                    return Class.forName(c);
                } catch (ClassNotFoundException e) {
                    throw new UncheckedIOException(new IOException(e));
                }
            }).collect(Collectors.toSet());
    }

    private static void processClasses(Set<Class<?>> classes, List<Map<String, Object>> jsonArrayOutput) {
        for (Class<?> class_ : classes) {
            if (class_.getName().endsWith("$Builder")) {
                continue;
            }
            Map<String, Object> configurationMap = new LinkedHashMap<>();
            configurationMap.put("name", class_.getName());
            if (class_.isInterface()) {
                // Generate different configuration for interface classes
                configurationMap.put("queryAllDeclaredMethods", true);
            } else {
                configurationMap.put("allDeclaredFields", true);
                configurationMap.put("allPublicFields", true);
                configurationMap.put("allDeclaredMethods", true);
                configurationMap.put("allPublicMethods", true);
                configurationMap.put("allDeclaredConstructors", true);
                configurationMap.put("allPublicConstructors", true);
            }
            jsonArrayOutput.add(configurationMap);
        }
    }
}

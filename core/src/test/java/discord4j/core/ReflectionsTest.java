package discord4j.core;

import discord4j.discordjson.Id;
import discord4j.discordjson.json.gateway.OpcodeConverter;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.reflections.Reflections;
import org.reflections.scanners.Scanners;
import org.reflections.util.ConfigurationBuilder;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Set;
import java.util.stream.Collectors;

public class ReflectionsTest {

//    @Test
    public void classScanTest() {
        final Reflections scanner2 = new Reflections(
            new ConfigurationBuilder()
                .setScanners(Scanners.SubTypes.filterResultsBy(c -> true))
                .forPackages("discord4j.discordjson")
        );

        Set<Class<?>> classes2 = getAllClasses(scanner2); // 1246

        final Reflections scanner1 = new Reflections(
            new ConfigurationBuilder()
                .forPackages("discord4j.discordjson")
        );
        Set<Class<?>> classes1 = getAllClasses(scanner1); // 784



        Assertions.assertEquals(classes1.size(), classes2.size());
        Assertions.assertEquals(classes1, classes2);
    }

//    @Test
    public void classScanTest2() {
        final Reflections scanner2 = new Reflections(
            new ConfigurationBuilder()
                .setScanners(Scanners.SubTypes.filterResultsBy(c -> true))
                .forPackages("discord4j.discordjson")
        );

        Set<Class<?>> classes2 = getAllClasses(scanner2); // 1246

        Assertions.assertEquals(1246, classes2.size());
        Assertions.assertTrue(classes2.contains(OpcodeConverter.class));
        Assertions.assertTrue(classes2.contains(Id.class));
    }

    private Set<Class<?>> getAllClasses(final Reflections scanner) {
        return scanner.getAllTypes().stream()
            .filter(c -> c.startsWith("discord4j.discordjson"))
            .map(c -> {
                try {
                    return Class.forName(c);
                } catch (ClassNotFoundException e) {
                    throw new UncheckedIOException(new IOException(e));
                }
            }).collect(Collectors.toSet());
    }
}

package test.postinstall;

import io.vavr.collection.HashMap;
import io.vavr.collection.Map;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

final class Utils {
    static Map<String, String> readPropertiesFile(final Path fileLocation) throws IOException {
        final var properties = new Properties();
        try (var is = Files.newInputStream(fileLocation)) {
            properties.load(is);
        }
        return HashMap.ofAll(properties)
                .mapKeys(String.class::cast)
                .mapValues(String.class::cast);
    }
}

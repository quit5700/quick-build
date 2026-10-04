package cn.quit5700.buildingwand;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LocalizationResourcesTest {
    private static final String[] LANGUAGES = {
            "en_us", "zh_cn", "zh_tw", "ja_jp", "ko_kr", "de_de", "fr_fr", "es_es"
    };
    private static final String[] NAMESPACES = {"building_wand", "light", "pathfinding_beacon"};
    private static final Pattern JSON_KEY = Pattern.compile("\\\"([^\\\"]+)\\\"\\s*:");
    private static final Pattern TRANSLATION_KEY = Pattern.compile("translatable\\(\\\"([^\\\"]+)\\\"");
    private static final Pattern HAN_CHARACTER = Pattern.compile("[\\u4e00-\\u9fff]");

    @Test
    void everyLanguageHasTheSameKeysAndJavaHasNoHardcodedChinese() throws IOException {
        Path resources = Path.of("src", "main", "resources", "assets");
        Set<String> allKeys = new HashSet<>();
        for (String namespace : NAMESPACES) {
            Path langDir = resources.resolve(namespace).resolve("lang");
            Set<String> expected = keys(langDir.resolve("zh_cn.json"));
            allKeys.addAll(expected);
            for (String language : LANGUAGES) {
                Path file = langDir.resolve(language + ".json");
                assertTrue(Files.isRegularFile(file), "Missing language file: " + file);
                assertEquals(expected, keys(file), "Translation keys differ in " + file);
            }
        }

        Path sourceRoot = Path.of("src");
        try (var files = Files.walk(sourceRoot)) {
            for (Path file : files.filter(path -> path.toString().endsWith(".java")).toList()) {
                String source = Files.readString(file, StandardCharsets.UTF_8);
                assertFalse(HAN_CHARACTER.matcher(source).find(), "Hardcoded Chinese text in " + file);
                Matcher matcher = TRANSLATION_KEY.matcher(source);
                while (matcher.find()) {
                    String key = matcher.group(1);
                    if (key.startsWith("text.quick_build.")
                            || key.startsWith("message.quick_build.")
                            || key.startsWith("display.quick_build.")
                            || key.startsWith("screen.quick_build.")) {
                        assertTrue(allKeys.contains(key), "Missing translation key " + key + " used by " + file);
                    }
                }
            }
        }
    }

    private static Set<String> keys(Path file) throws IOException {
        String json = Files.readString(file, StandardCharsets.UTF_8);
        Set<String> keys = new HashSet<>();
        Matcher matcher = JSON_KEY.matcher(json);
        while (matcher.find()) keys.add(matcher.group(1));
        return keys;
    }
}

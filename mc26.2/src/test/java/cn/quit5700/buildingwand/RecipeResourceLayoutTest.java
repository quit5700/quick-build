package cn.quit5700.buildingwand;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;

class RecipeResourceLayoutTest {
    private static final Path DATA_ROOT = Path.of("src", "main", "resources", "data");

    @Test
    void minecraft26RecipesUseSingularRecipeDirectory() throws IOException {
        List<Path> legacyRecipeFiles;
        try (var paths = Files.walk(DATA_ROOT)) {
            legacyRecipeFiles = paths
                    .filter(Files::isRegularFile)
                    .filter(path -> path.getFileName().toString().endsWith(".json"))
                    .filter(path -> path.getParent().getFileName().toString().equals("recipes"))
                    .toList();
        }

        assertTrue(legacyRecipeFiles.isEmpty(),
                "Minecraft 26.2 recipes must use data/<namespace>/recipe: " + legacyRecipeFiles);
    }
}

package cn.quit5700.buildingwand;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import java.util.List;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

class RecipeResourceLayoutTest {
    private static final Path DATA_ROOT = Path.of("src", "main", "resources", "data");
    private static final Set<String> VALID_CATEGORIES = Set.of("building", "equipment", "misc", "redstone");

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

    @Test
    void migratedRecipesUseMinecraft26IngredientAndResultSchema() throws IOException {
        for (String namespace : List.of("building_wand", "pathfinding_beacon")) {
            Path recipeRoot = DATA_ROOT.resolve(namespace).resolve("recipe");
            try (var paths = Files.list(recipeRoot)) {
                for (Path recipePath : paths.filter(path -> path.toString().endsWith(".json")).toList()) {
                    JsonObject recipe = JsonParser.parseString(Files.readString(recipePath)).getAsJsonObject();
                    JsonObject result = recipe.getAsJsonObject("result");
                    assertTrue(result.has("id"), "Minecraft 26.2 recipe result must use id: " + recipePath);
                    assertFalse(result.has("item"), "Minecraft 26.2 recipe result must not use item: " + recipePath);
                    assertTrue(VALID_CATEGORIES.contains(recipe.get("category").getAsString()),
                            "Unsupported recipe category: " + recipePath);

                    if (recipe.has("key")) {
                        for (JsonElement ingredient : recipe.getAsJsonObject("key").asMap().values()) {
                            assertTrue(ingredient.isJsonPrimitive(),
                                    "Minecraft 26.2 shaped ingredients must be strings: " + recipePath);
                        }
                    }
                    if (recipe.has("ingredients")) {
                        for (JsonElement ingredient : recipe.getAsJsonArray("ingredients")) {
                            assertTrue(ingredient.isJsonPrimitive(),
                                    "Minecraft 26.2 shapeless ingredients must be strings: " + recipePath);
                        }
                    }
                }
            }
        }
    }
}

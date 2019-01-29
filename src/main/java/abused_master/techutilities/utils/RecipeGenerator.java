package abused_master.techutilities.utils;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.item.Item;
import net.minecraft.item.ItemProvider;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Ingredient;
import net.minecraft.tag.ItemTags;
import net.minecraft.tag.Tag;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class RecipeGenerator {

    private static final List<Output> recipes = new ArrayList<>();

    public static void createShaped(ItemStack output, String group, String[] shape, Map<Character, Ingredient> ingredients) {
        JsonObject recipe = prepRecipe("crafting_shaped", group);

        JsonArray pattern = new JsonArray();
        for (String line : shape)
            pattern.add(line);
        recipe.add("pattern", pattern);

        JsonObject keys = new JsonObject();
        ingredients.forEach((k, v) -> keys.add(String.valueOf(k), v.toJson()));
        recipe.add("key", keys);

        Identifier registryName = appendResult(recipe, output);
        recipes.add(new Output(registryName.getPath(), recipe));
    }

    public static  void createShaped(ItemStack output, String group, ShapedParser shape) {
        Pair<String[], Map<Character, Ingredient>> parsed = shape.parse();
        createShaped(output, group, parsed.getLeft(), parsed.getRight());
    }

    public static void createShapeless(ItemStack output, String group, List<Ingredient> ingredients) {
        JsonObject recipe = prepRecipe("crafting_shapeless", group);

        JsonArray ingredientArray = new JsonArray();
        ingredients.forEach(i -> ingredientArray.add(i.toJson()));
        recipe.add("ingredients", ingredientArray);

        Identifier registryName = appendResult(recipe, output);
        recipes.add(new Output(registryName.getPath(), recipe));
    }

    public static void createShapeless(ItemStack output, String group, ShapelessParser shape) {
        createShapeless(output, group, shape.parse());
    }

    private static void createSmelting(String type, ItemStack output, String group, Ingredient input, float experience, int cookingTime) {
        JsonObject recipe = prepRecipe(type, group);
        recipe.add("ingredient", input.toJson());
        Identifier registryName = Registry.ITEM.getId(output.getItem());
        recipe.addProperty("result", registryName.toString());
        recipe.addProperty("experience", experience);
        recipe.addProperty("cookingtime", cookingTime);
        recipes.add(new Output(registryName.getPath(), recipe));
    }

    public static void createFurnace(ItemStack output, String group, Ingredient input, float experience, int cookingTime) {
        createSmelting("smelting", output, group, input, experience, cookingTime);
    }

    public static void createBlasting(ItemStack output, String group, Ingredient input, float experience, int cookingTime) {
        createSmelting("blasting", output, group, input, experience, cookingTime);
    }

    public static void createSmoking(ItemStack output, String group, Ingredient input, float experience, int cookingTime) {
        createSmelting("smoking", output, group, input, experience, cookingTime);
    }

    private static Identifier appendResult(JsonObject recipe, ItemStack output) {
        JsonObject result = new JsonObject();
        Identifier registryName = Registry.ITEM.getId(output.getItem());
        result.addProperty("item", registryName.toString());
        if (output.getAmount() > 1)
            result.addProperty("count", output.getAmount());
        recipe.add("result", result);
        return registryName;
    }

    private static JsonObject prepRecipe(String type, String group) {
        JsonObject recipe = new JsonObject();
        recipe.addProperty("type", type);

        if (group != null)
            recipe.addProperty("group", group);

        return recipe;
    }

    public static List<Output> getRecipes() {
        return recipes;
    }

    public static class Output {
        public final String name;
        public final JsonObject recipe;

        public Output(String name, JsonObject recipe) {
            this.name = name;
            this.recipe = recipe;
        }
    }

    public static class ShapedParser {
        private final Object[] shape;

        public ShapedParser(Object... shape) {
            this.shape = shape;
        }

        public Pair<String[], Map<Character, Ingredient>> parse() {
            String[] pattern = new String[0];
            Map<Character, Ingredient> ingredients = Maps.newHashMap();
            boolean lookingForShape = true;
            for (int i = 0; i < shape.length; i++) {
                Object object = shape[i];
                if (object instanceof String && lookingForShape) {
                    pattern = ArrayUtils.add(pattern, (String) object);
                } else if (object instanceof Character) {
                    Character character = (Character) object;
                    lookingForShape = false;
                    if (shape.length < i + 1)
                        throw new IllegalArgumentException("Character defined with no following valid ingredient");
                    Object next = shape[i + 1];
                    if (next instanceof Ingredient)
                        ingredients.put(character, (Ingredient) next);
                    else if (next instanceof ItemProvider)
                        ingredients.put(character, Ingredient.ofItems((ItemProvider) next));
                    else if (next instanceof ItemStack)
                        ingredients.put(character, Ingredient.ofStacks((ItemStack) next));
                    else if (next instanceof Tag && ItemTags.getContainer().get(((Tag) object).getId()) != null)
                        ingredients.put(character, Ingredient.fromTag(ItemTags.getContainer().get(((Tag) next).getId())));
                    else if (next instanceof String)
                        ingredients.put(character, Ingredient.fromTag(ItemTags.getContainer().get(new Identifier((String) next))));
                    else if (next instanceof Identifier)
                        ingredients.put(character, Ingredient.fromTag(ItemTags.getContainer().get((Identifier) next)));
                    else
                        throw new IllegalArgumentException("Character defined with no following valid ingredient: " + next);

                    i++;
                }
            }

            return Pair.of(pattern, ingredients);
        }
    }

    public static class ShapelessParser {
        private final Object[] shape;

        public ShapelessParser(Object... shape) {
            this.shape = shape;
        }

        public List<Ingredient> parse() {
            List<Ingredient> ingredients = Lists.newArrayList();
            for (Object object : shape) {
                if (object instanceof Ingredient)
                    ingredients.add((Ingredient) object);
                else if (object instanceof ItemProvider)
                    ingredients.add(Ingredient.ofItems((ItemProvider) object));
                else if (object instanceof ItemStack)
                    ingredients.add(Ingredient.ofStacks((ItemStack) object));
                else if (object instanceof Tag && ItemTags.getContainer().get(((Tag) object).getId()) != null)
                    ingredients.add(Ingredient.fromTag((Tag<Item>) object));
                else if (object instanceof String)
                    ingredients.add(Ingredient.fromTag(ItemTags.getContainer().get(new Identifier((String) object))));
                else if (object instanceof Identifier)
                    ingredients.add(Ingredient.fromTag(ItemTags.getContainer().get((Identifier) object)));
            }

            return ingredients;
        }
    }
}
package fr.farrael.craftrecipe.internal;

import fr.farrael.craftrecipe.Configuration;
import fr.farrael.craftrecipe.CraftRecipe;
import fr.farrael.craftrecipe.internal.recipes.BaseRecipe;
import fr.farrael.craftrecipe.internal.recipes.CraftingRecipe;
import fr.farrael.craftrecipe.internal.recipes.SmeltingRecipe;
import fr.farrael.craftrecipe.utils.ChatUtil;
import fr.farrael.rootlib.api.configuration.internal.ConfigData;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;

public class RecipeManager {

    public static final Random random = new Random();

    /*****************************************************************/
    /*                       Recipes types                           */
    /*****************************************************************/

    public enum RecipeType {
        CRAFTING(craftingRecipes),
        SMELTING(smeltingRecipes),
        BREWING(null),
        REPAIR(null),
        UNKNOWN(null);

        private Map<Integer, ? extends BaseRecipe> map;
        RecipeType( Map<Integer, ? extends BaseRecipe> map) {
            this.map   = map;
        }

        public boolean isValid() {
            return this.map != null;
        }

        public BaseRecipe getRecipe(int slot) {
            if(this.isValid())
                return this.map.get(slot);

            return null;
        }

        public Map<Integer, ? extends BaseRecipe> getRecipes() {
            return this.map;
        }

        @Override
        public String toString() {
            return ChatUtil.firstUppercase(this.name());
        }
    }

    /*****************************************************************/
    /*                   Create/Remove Recipes                       */
    /*****************************************************************/

    // List of recipe ordered by id
    private static final HashMap<Integer, CraftingRecipe> craftingRecipes = new HashMap<>();
    private static final HashMap<Integer, SmeltingRecipe> smeltingRecipes = new HashMap<>();

    /**
     * Register new recipe that extends {@link BaseRecipe}.
     *
     * @param recipe - Recipe to register
     * @param slot   - GUI Slot id
     */
    public static void register(BaseRecipe recipe, int slot) {
        BaseRecipe old = null;

        if(recipe instanceof CraftingRecipe) {
            old = craftingRecipes.put(slot, (CraftingRecipe) recipe);
        } else if(recipe instanceof SmeltingRecipe) {
            old = smeltingRecipes.put(slot, (SmeltingRecipe) recipe);
        }

        if(old != null)
            old.unregister();

        updateAndSave(recipe);
    }

    /**
     * Remove a recipe that extends {@link BaseRecipe}.
     *
     * @param recipe - Recipe to unregister
     */
    public static void remove(BaseRecipe recipe) {
        Iterator iter = getRecipeIterator(recipe);

        if(iter == null)
            return;

        // Remove from file
        delete(recipe);

        // Remove from custom recipes
        Map.Entry  pair;
        BaseRecipe current;
        while(iter.hasNext()) {
            pair = (Map.Entry) iter.next();

            current = (BaseRecipe) pair.getValue();
            if(current.equals(recipe)) {
                iter.remove();
            }
        }
    }

    /*****************************************************************/
    /*                         Search recipe                         */
    /*****************************************************************/

    /**
     * Retrieve the slot Id of the recipe.
     *
     * @param recipe - Recipe to retrieve slot
     *
     * @return Integer (-1 if null)
     */
    public static int getSlot(BaseRecipe recipe) {
        Iterator iter = getRecipeIterator(recipe);
        int index = -1;

        if(iter == null)
            return index;

        Map.Entry  pair;
        BaseRecipe current;
        while(iter.hasNext()) {
            pair = (Map.Entry) iter.next();

            current = (BaseRecipe) pair.getValue();
            if(current.equals(recipe)) {
                index = (int) pair.getKey();
                break;
            }
        }

        return index;
    }

    /**
     * Return the type of the recipe.
     *
     * @param recipe - Recipe to retrieve type
     *
     * @return RecipeType
     */
    public static RecipeType getRecipeType(BaseRecipe recipe) {
        if(recipe instanceof CraftingRecipe)
            return RecipeType.CRAFTING;
        else if(recipe instanceof SmeltingRecipe)
            return RecipeType.SMELTING;

        return RecipeType.UNKNOWN;
    }

    /**
     * Return an iterator based on the recipe type
     *
     * @param recipe - Recipe type to iterate
     *
     * @return Iterator
     */
    private static Iterator getRecipeIterator(BaseRecipe recipe) {
        if(recipe instanceof CraftingRecipe)
            return craftingRecipes.entrySet().iterator();
        else if(recipe instanceof SmeltingRecipe)
            return smeltingRecipes.entrySet().iterator();

        return null;
    }

    /*****************************************************************/
    /*                      Save/Remove Recipes                      */
    /*****************************************************************/

    /**
     * Save a recipe on the configuration file
     *
     * @param recipe - RecipeManager to save
     *
     * @return File saving success
     */
    public static void updateAndSave(BaseRecipe recipe) {
        // Retrieve the configuration file
        ConfigData config = CraftRecipe.getInstance().configuration.getFileManager().getFile(Configuration.FILE_CRAFT);

        // Set the type of the craft
        config.setData(recipe.getType() + "." + getSlot(recipe), recipe);
    }

    /**
     * Remove a recipe from the configuration file
     *
     * @param recipe - RecipeManager to remove
     *
     * @return File saving success
     */
    private static void delete(BaseRecipe recipe) {
        if(recipe.isRegistered())
            recipe.unregister();

        // Retrieve the configuration file
        ConfigData config = CraftRecipe.getInstance().configuration.getFileManager().getFile(Configuration.FILE_CRAFT);

        // Remove the entry from the configuration
        config.setData(recipe.getType() + "." + getSlot(recipe), null);
    }
}

package fr.farrael.craftrecipe.internal.recipes;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

public class BaseRecipe implements ConfigurationSerializable {
    // Is recipe currently register
    protected boolean register = false;

    // Hash of the recipe (used to compare)
    protected int hash;

    // Result of the recipe
    protected ItemStack result;

    // Permission require for the recipe
    protected String permission;

    // Bukkit recipe
    private Recipe recipe;

    /*****************************************************************/
    /*                          Constructor                          */
    /*****************************************************************/

    /**
     * Create empty recipe
     */
    protected BaseRecipe(ItemStack result, String permission) {
        this.result     = result;
        this.permission = permission;
    }

    /**
     * Create recipe from Configuration
     * @param args - Base of the recipe
     */
    protected BaseRecipe(Map<String, Object> args) {
        this.result   = (ItemStack) args.get("result");
        this.register = (boolean) args.get("enable");

        if(args.containsKey("permission"))
            permission = (String) args.get("permission");
    }

    /*****************************************************************/
    /*                        Object function                        */
    /*****************************************************************/

    /**
     * Return if this recipe is registered
     * @return Boolean
     */
    public boolean isRegistered() {
        return this.register;
    }

    /**
     * Register a permission for the craft
     */
    public void setPermission(String perm) {
        this.permission = perm;
    }

    /**
     * Return if this recipe has a permission
     * @return Boolean
     */
    public boolean hasPermission() {
        return this.permission != null;
    }

    /**
     * Return if the player can user this recipe
     * @param player - Player that want to use the recipe
     * @return True if can use it.
     */
    public boolean canCraft(Player player) {
        return this.permission == null || player.hasPermission("Craftrecipe.recipe." + this.permission);
    }

    /**
     * Return le nom de la permission requise
     * @return String
     */
    public String getPermission() {
        return this.permission != null ? ChatColor.GREEN + this.permission : ChatColor.RED + "Aucune";
    }

    /**
     * Return the result of the recipe
     * @return ItemStack
     */
    public ItemStack getResult() {
        return this.result.clone();
    }

    /*****************************************************************/
    /*                       Bukkit Conversion                       */
    /*****************************************************************/

    /**
     * Return the bukkit version of this recipe
     * @return RecipeManager
     */
    public Recipe getBukkitRecipe() {
        if(this.recipe == null)
            this.recipe = this.toBukkitRecipe();

        return this.recipe;
    }

    /**
     * Create the bukkit version of this recipe
     * @return RecipeManager
     */
    protected Recipe toBukkitRecipe() {
        return null;
    }

    /*****************************************************************/
    /*                           Comparator                          */
    /*****************************************************************/

    /**
     * Return if recipes are similar
     * @param recipe - Recipe to compare
     * @return True if similar
     */
    public boolean isSimilar(Recipe recipe) {
        return recipe.getResult().equals(this.result);
    }

    @Override
    public int hashCode() {
        return this.hash;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj == this)
            return true;

        if(!(obj instanceof BaseRecipe))
            return false;

        return obj.hashCode() == this.hashCode();
    }

    /*****************************************************************/
    /*                           Registers                           */
    /*****************************************************************/

    public void register() {
        Bukkit.getServer().addRecipe(this.getBukkitRecipe());
        this.register = true;
    }

    public void unregister() {
        Iterator<Recipe> iter = Bukkit.getServer().recipeIterator();
        org.bukkit.inventory.Recipe recipe;
        while(iter.hasNext()) {
            recipe = iter.next();
            if(this.isSimilar(recipe)) {
                iter.remove();
                break;
            }
        }

        this.register = false;
    }

    /*****************************************************************/
    /*                         Serialization                         */
    /*****************************************************************/

    /**
     * Used to set the saving path
     * @return Type of recipe
     */
    public String getType() {
        return "base";
    }

    /**
     * Saving the recipe
     * @return Map to save
     */
    public Map<String, Object> serialize() {
        LinkedHashMap<String, Object> map = new LinkedHashMap<>();

        map.put("enable", this.register);
        map.put("result", this.result);

        if(this.permission != null)
            map.put("permission", this.permission);

        return map;
    }
}

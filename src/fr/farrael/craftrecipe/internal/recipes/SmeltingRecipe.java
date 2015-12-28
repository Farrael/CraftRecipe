package fr.farrael.craftrecipe.internal.recipes;

import fr.farrael.craftrecipe.internal.RecipeManager;
import org.bukkit.inventory.FurnaceRecipe;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;

import java.util.Map;

public class SmeltingRecipe extends BaseRecipe {
    // Require item of the recipe
    ItemStack craft;

    // Minimum and Maximum seconds to cook.
    private int minSmelting = 10;
    private int maxSmelting = 10;

    /*****************************************************************/
    /*                          Constructor                          */
    /*****************************************************************/

    public SmeltingRecipe(ItemStack result, ItemStack craft, String permission) {
        super(result, permission);

        this.craft = craft;

        // Generate hash
        this.generateHash();
    }

    public SmeltingRecipe(Map<String, Object> args) {
        super(args);

        this.craft       = (ItemStack) args.get("component");
        this.minSmelting = (int) args.get("minSmeltingTime");

        if(args.containsKey("maxCookingTime"))
            this.maxSmelting = (int) args.get("maxSmeltingTime");
        else
            this.maxSmelting = this.minSmelting;

        if(this.register)
            this.register();

        // Generate hash
        this.generateHash();
    }

    /*****************************************************************/
    /*                        Object function                        */
    /*****************************************************************/

    /**
     * Set the minimum number of seconds for smelting
     * @param time - Number of seconds
     */
    public void setMinTime(int time) {
        this.minSmelting = time;
    }

    /**
     * Set the maximum number of seconds for smelting
     * @param time - Number of seconds
     */
    public void setMaxTime(int time) {
        this.maxSmelting = time;
    }

    /**
     * Retrieve the minimum number of seconds for smelting
     * @return Integer
     */
    public int getMinTime() {
        return this.minSmelting;
    }

    /**
     * Retrieve the maximum number of seconds for smelting
     * @return Integer
     */
    public int getMaxTime() {
        return this.maxSmelting;
    }

    /**
     * Retrieve the component of the recipe
     * @return ItemStack
     */
    public ItemStack getIngredient() {
        return this.craft;
    }

    /**
     * Compute the numbers of ticks for smelting
     * @return Integer
     */
    public int getBurnTicks() {
        float time;

        if(this.maxSmelting > this.minSmelting)
            time = this.minSmelting + (this.maxSmelting - this.minSmelting) * RecipeManager.random.nextFloat();
        else
            time = this.minSmelting;

        return (int) Math.round(20.0 * time);
    }

    /*****************************************************************/
    /*                            HashCode                           */
    /*****************************************************************/

    /**
     * Generate the HashCode of the recipe
     */
    private void generateHash() {
        StringBuilder str = new StringBuilder();
        str.append("MinTime:").append(this.minSmelting);
        str.append("MaxTime:").append(this.maxSmelting);
        str.append("Component:").append(this.craft);
        str.append("Result:").append(this.result.hashCode());

        this.hash = str.toString().hashCode();
    }

    /*****************************************************************/
    /*                           Comparator                          */
    /*****************************************************************/

    @Override
    public boolean isSimilar(Recipe recipe){
        if(!(recipe instanceof FurnaceRecipe) || !super.isSimilar(recipe))
            return false;

        FurnaceRecipe current = (FurnaceRecipe) this.getBukkitRecipe();
        return current.getInput().equals(((FurnaceRecipe) recipe).getInput());
    }

    /*****************************************************************/
    /*                       Bukkit Conversion                       */
    /*****************************************************************/

    @Override
    protected Recipe toBukkitRecipe() {
        return new FurnaceRecipe(this.result, this.craft.getData());
    }

    /*****************************************************************/
    /*                         Serialization                         */
    /*****************************************************************/

    @Override
    public String getType() {
        return "smelt";
    }

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> map = super.serialize();

        // Save component
        map.put("component", this.craft);

        // Save Smelting time
        map.put("minSmeltingTime", this.minSmelting);
        map.put("maxSmeltingTime", this.maxSmelting);

        return map;
    }
}

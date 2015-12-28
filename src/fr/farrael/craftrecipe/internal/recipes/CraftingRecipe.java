package fr.farrael.craftrecipe.internal.recipes;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

public class CraftingRecipe extends BaseRecipe {
    // Store crafting characters
    private static final Character[] CHARACTERS = {'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I'};

    // Components used for the craft
    private ArrayList<ItemStack> craft;

    // Material used for the craft
    private LinkedList<ItemStack> material;

    /*****************************************************************/
    /*                          Constructor                          */
    /*****************************************************************/

    public CraftingRecipe(ItemStack result, ArrayList<ItemStack> craft, String permission) {
        super(result, permission);
        this.craft = craft;

        // Generate hash
        this.generateHash();
    }

    public CraftingRecipe(Map<String, Object> args) {
        super(args);

        // Loading components
        this.craft = (ArrayList<ItemStack>) args.get("components");

        // Register
        if(this.register)
            this.register();

        // Generate hash here
        this.generateHash();
    }

    /*****************************************************************/
    /*                        Object function                        */
    /*****************************************************************/

    /**
     * Return an component of the recipe
     * @param index - Index of the component (Slot ID, between 0 and 8)
     * @return ItemStack
     */
    public ItemStack getItem(int index) {
        return index < this.craft.size() ? this.craft.get(index) : null;
    }

    /**
     * Return all component of the recipe ordered by slot ID (between 0 and 8)
     * @return LinkedList<ItemStack>>
     */
    public ArrayList<ItemStack> getIngredients() {
        return this.craft;
    }

    /*****************************************************************/
    /*                            HashCode                           */
    /*****************************************************************/

    /**
     * Generate the HashCode of the recipe
     */
    private void generateHash() {
        StringBuilder str = new StringBuilder();
        for(int i = 0, size = craft.size(); i < size; i++) {
            str.append("Slot ").append(i).append(":");
            if(craft.get(i) != null)
                str.append(craft.get(i).hashCode());
            else
                str.append("none");
            str.append(";");
        }
        str.append("Result:").append(this.result.hashCode());

        this.hash = str.toString().hashCode();
    }

    /*****************************************************************/
    /*                           Comparator                          */
    /*****************************************************************/

    @Override
    public boolean isSimilar(Recipe recipe){
        if(!(recipe instanceof ShapedRecipe) || !super.isSimilar(recipe))
            return false;

        ShapedRecipe current;

        // Create material list (used to compare)
        if(this.material == null) {
            current = (ShapedRecipe) this.getBukkitRecipe();
            this.material = new LinkedList<>();
            for (String s : current.getShape()) {
                for (Character ch : s.toCharArray())
                    this.material.add(current.getIngredientMap().get(ch));
            }
        }

        current = (ShapedRecipe) recipe;

        // Check equality
        int i = 0;
        ItemStack item;
        for(String s : current.getShape()) {
            for(Character c : s.toCharArray()) {
                item = current.getIngredientMap().get(c);
                if (item == null || this.material.get(i) == null) {
                    if(item != this.material.get(i))
                        return false;
                } else if(!item.equals(this.material.get(i)))
                    return false;

                i++;
            }
        }

        return true;
    }

    /*****************************************************************/
    /*                       Bukkit Conversion                       */
    /*****************************************************************/

    @Override
    protected Recipe toBukkitRecipe() {
        // Define local variables
        int i; int j; Character c;

        // Initialise the shape with the right size
        String[] rows = new String[this.craft.size() / 3];

        // Create recipe
        i = j = 0;
        HashMap<ItemStack, Character> database    = new HashMap<>();
        HashMap<Character, ItemStack> ingredients = new HashMap<>();
        for(ItemStack item : this.craft) {
            if(!database.containsKey(item))
                database.put(item, CHARACTERS[j++]);

            c = database.get(item);
            if(rows[i / 3] == null)
                rows[i / 3] = "";

            rows[i++ / 3] += c.toString();

            if(item != null)
                ingredients.put(c, item);
        }

        // Create new ShapedRecipe
        ShapedRecipe recipe = new ShapedRecipe(this.result);
        recipe.shape(rows);

        ItemStack item;
        for(Character ch : ingredients.keySet()) {
            item = ingredients.get(ch);
            if(item.getData() != null)
                recipe.setIngredient(ch, item.getData());
            else
                recipe.setIngredient(ch, item.getType());
        }

        return recipe;
    }

    /*****************************************************************/
    /*                         Serialization                         */
    /*****************************************************************/

    @Override
    public String getType() {
        return "craft";
    }

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> map = super.serialize();

        // Save components
        map.put("components", this.craft);

        return map;
    }
}

package fr.farrael.craftrecipe.internal.gui;

import fr.farrael.craftrecipe.internal.RecipeManager;
import fr.farrael.craftrecipe.internal.recipes.BaseRecipe;
import fr.farrael.craftrecipe.internal.recipes.CraftingRecipe;
import fr.farrael.craftrecipe.internal.recipes.SmeltingRecipe;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class EditGUI extends BaseGUI {

    /*****************************************************************/
    /*                         INVENTORY                             */
    /*****************************************************************/

    // Icons names
    private static final String BORDER_NAME   = ChatColor.RED + "\u2716";
    private static final String SIGN_NAME     = ChatColor.RED + "Informations";
    private static final String VALIDATE_NAME = ChatColor.GREEN + "\u2714 Valider";
    private static final String BACK_NAME     = ChatColor.BLUE + "\u2B05 Retour";
    private static final String FUEL_NAME     = ChatColor.RED + "Fuel";

    // Icons items
    private static ItemStack BORDER_ITEM;
    private static ItemStack SIGN_ITEM;
    private static ItemStack VALIDATE_ITEM;
    private static ItemStack BACK_ITEM;
    private static ItemStack FUEL_ITEM;

    // Is icon initiate
    protected static boolean init = false;

    // Slot
    private static final HashMap<RecipeManager.RecipeType, LinkedList<Integer>> SLOT_CRAFTING;
    private static final int SLOT_RESULT = 23;

    static {
        SLOT_CRAFTING = new HashMap<>();
        SLOT_CRAFTING.put(RecipeManager.RecipeType.CRAFTING, new LinkedList<>(Arrays.asList(10,11,12,19,20,21,28,29,30)));
        SLOT_CRAFTING.put(RecipeManager.RecipeType.SMELTING, new LinkedList<>(Collections.singletonList(11)));
    }

    /*****************************************************************/
    /*                           HOLDER                              */
    /*****************************************************************/

    private int                      slot;
    private RecipeManager.RecipeType type;

    public EditGUI(int slot, RecipeManager.RecipeType type) {
        this.slot = slot;
        this.type = type;

        // Inventory size and title
        this.size  = 45;
        this.title = ChatColor.BLUE + "Editing " + this.type + " (Slot " + this.slot + ")";
    }

    /*****************************************************************/
    /*                       Opening the GUI                         */
    /*****************************************************************/

    @Override
    public void open(Player player) {
        if (!init)
            this.init();

        // Create inventory with holder
        Inventory inventory = this.getInventory();

        // Retrieve crafting slots based on recipe type
        LinkedList<Integer> slots = SLOT_CRAFTING.get(this.type);
        if(slots == null)
            return;

        // Default inventory design
        int position = 0;
        for(int i = 0; i < 5; i++) {
            for (int j = 0; j < 9; j++) {
                if(position != SLOT_RESULT && !slots.contains(position))
                    inventory.setItem(position, BORDER_ITEM);

                position++;
            }
        }

        // Button
        inventory.setItem(17, BACK_ITEM);
        inventory.setItem(26, SIGN_ITEM);
        inventory.setItem(35, VALIDATE_ITEM);

        // Format based on recipe type
        switch(this.type) {
            case CRAFTING:
                setCraftInventory(slots);
                break;
            case SMELTING:
                setSmeltInventory(slots);
                break;
            default:
                break;
        }

        // Open player inventory
        player.openInventory(inventory);
    }

    /*****************************************************************/
    /*                       Format the GUI                          */
    /*****************************************************************/

    private void setCraftInventory(LinkedList<Integer> slots) {
        CraftingRecipe recipe    = (CraftingRecipe) this.type.getRecipe(this.slot);
        Inventory      inventory = this.getInventory();
        if(recipe != null) {
            for (int i = 0, size = recipe.getIngredients().size(); i < size; i++)
                inventory.setItem(slots.get(i), recipe.getIngredients().get(i));

            inventory.setItem(SLOT_RESULT, recipe.getResult());
        }
    }

    private void setSmeltInventory(LinkedList<Integer> slots) {
        SmeltingRecipe recipe    = (SmeltingRecipe) this.type.getRecipe(this.slot);
        Inventory      inventory = this.getInventory();
        if(recipe != null) {
            inventory.setItem(slots.get(0), recipe.getIngredient());
            inventory.setItem(SLOT_RESULT, recipe.getResult());
        }

        inventory.setItem(29, FUEL_ITEM);
    }

    /*****************************************************************/
    /*                       Click on the GUI                        */
    /*****************************************************************/

    @Override
    public void onClickEvent(InventoryClickEvent event) {
        Player    player     = (Player) event.getWhoClicked();
        ItemStack clicked    = event.getCurrentItem();
        Inventory inventory  = this.getInventory();

        // Retrieve crafting slots based on recipe type
        LinkedList<Integer> slots = SLOT_CRAFTING.get(this.type);
        if(slots == null)
            return;

        // Prevent double click
        if(event.getClick() == ClickType.DOUBLE_CLICK ||
                (event.getSlot() != EditGUI.SLOT_RESULT && !slots.contains(event.getSlot())))
            event.setCancelled(true);

        if(clicked.equals(EditGUI.BACK_ITEM)) {
            // Retrieve item on result slot
            if (inventory.getItem(EditGUI.SLOT_RESULT) != null)
                player.getInventory().addItem(inventory.getItem(EditGUI.SLOT_RESULT));

            // Retrieve item on crafting slot
            for (int i : slots)
                if (inventory.getItem(i) != null)
                    player.getInventory().addItem(inventory.getItem(i));

            new RecipeGUI(RecipeGUI.getSlotPage(this.slot), this.type).open(player);
        } else if(clicked.equals(EditGUI.VALIDATE_ITEM)) {
            boolean valid = false;

            // Validate recipe
            if(inventory.getItem(EditGUI.SLOT_RESULT) != null && inventory.getItem(EditGUI.SLOT_RESULT).getType() != Material.AIR) {
                ArrayList<ItemStack> craft = new ArrayList<>();
                for(int i : slots) {
                    craft.add(inventory.getItem(i));

                    if(!valid && inventory.getItem(i) != null && inventory.getItem(i).getType() != Material.AIR)
                        valid = true;
                }

                if(valid) {
                    // Create the recipe
                    BaseRecipe recipe = null;
                    switch(this.type) {
                        case CRAFTING:
                            recipe = onSubmitCrafting(inventory.getItem(EditGUI.SLOT_RESULT), craft);
                            break;
                        case SMELTING:
                            recipe = onSubmitSmelting(inventory.getItem(EditGUI.SLOT_RESULT), craft);
                            break;
                        default:
                            break;
                    }

                    if(recipe != null) {
                        // Create and register the recipe
                        recipe.register();

                        // Register the recipe
                        RecipeManager.register(recipe, this.slot);
                    }

                    // Re-open the recipe gui
                    new RecipeGUI(RecipeGUI.getSlotPage(this.slot), this.type).open(player);
                }
            }
        }
    }

    /*****************************************************************/
    /*                       Submit the GUI                          */
    /*****************************************************************/

    private BaseRecipe onSubmitCrafting(ItemStack result, ArrayList<ItemStack> craft) {
        return new CraftingRecipe(result, craft, null);
    }

    private BaseRecipe onSubmitSmelting(ItemStack result, ArrayList<ItemStack> craft) {
        return new SmeltingRecipe(result, craft.get(0), null);
    }

    /*****************************************************************/
    /*                         Initiate                              */
    /*****************************************************************/

    protected void init() {
        ItemMeta meta;

        // Validate icon
        VALIDATE_ITEM = new ItemStack(Material.STAINED_GLASS_PANE, 1, (byte) 5);
        meta          = VALIDATE_ITEM.getItemMeta();
        meta.setDisplayName(VALIDATE_NAME);
        VALIDATE_ITEM.setItemMeta(meta);

        // Back icon
        BACK_ITEM = new ItemStack(Material.STAINED_GLASS_PANE, 1, (byte) 11);
        meta      = BACK_ITEM.getItemMeta();
        meta.setDisplayName(BACK_NAME);
        BACK_ITEM.setItemMeta(meta);

        // Border icon
        BORDER_ITEM = new ItemStack(Material.STAINED_GLASS_PANE, 1, (byte) 14);
        meta        = BORDER_ITEM.getItemMeta();
        meta.setDisplayName(BORDER_NAME);
        BORDER_ITEM.setItemMeta(meta);

        // Fuel icon
        FUEL_ITEM = new ItemStack(Material.COAL, 1);
        meta      = FUEL_ITEM.getItemMeta();
        meta.setDisplayName(FUEL_NAME);
        FUEL_ITEM.setItemMeta(meta);

        // Sign icon
        SIGN_ITEM = new ItemStack(Material.SIGN, 1);
        meta      = SIGN_ITEM.getItemMeta();
        meta.setDisplayName(SIGN_NAME);
        ArrayList<String> lore = new ArrayList<>();
        lore.add(ChatColor.GREEN + "Gauche : " + ChatColor.BLUE + "Paterne");
        lore.add(ChatColor.GREEN + "Droite : " + ChatColor.BLUE + "Resultat");
        meta.setLore(lore);
        SIGN_ITEM.setItemMeta(meta);

        init = true;
    }
}

package fr.farrael.craftrecipe.internal.gui;

import fr.farrael.craftrecipe.internal.RecipeManager;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class SelectGUI extends BaseGUI {

    /*****************************************************************/
    /*                         INVENTORY                             */
    /*****************************************************************/

    // Icons names
    private static final String ICON_CRAFT_NAME = ChatColor.GREEN + "Crafting";
    private static final String ICON_SMELT_NAME = ChatColor.GREEN + "Smelting";
    private static final String ICON_BREW_NAME  = ChatColor.GREEN + "Brewing" + ChatColor.RED + "(Soon)";
    private static final String ICON_REPAIR_NAME= ChatColor.GREEN + "Repair" + ChatColor.RED + "(Soon)";

    // Icons items
    private static ItemStack ITEM_CRAFT;
    private static ItemStack ITEM_SMELT;
    private static ItemStack ITEM_BREW;
    private static ItemStack ITEM_REPAIR;

    // Is icon initiate
    protected static boolean init = false;

    /*****************************************************************/
    /*                          HOLDER                               */
    /*****************************************************************/

    public SelectGUI() {
        // Inventory size and title
        this.size  = 9;
        this.title = ChatColor.BLUE  + "Recipes type";
    }

    /*****************************************************************/
    /*                       Opening the GUI                         */
    /*****************************************************************/

    @Override
    public void open(Player player) {
        if(!init)
            this.init();

        // Create inventory with holder
        Inventory inventory = this.getInventory();

        // Button
        inventory.setItem(1, ITEM_CRAFT);
        inventory.setItem(3, ITEM_SMELT);
        inventory.setItem(5, ITEM_REPAIR);
        inventory.setItem(7, ITEM_BREW);

        // Open player inventory
        player.openInventory(inventory);
    }

    /*****************************************************************/
    /*                     Click on the GUI                          */
    /*****************************************************************/

    @Override
    public void onClickEvent(InventoryClickEvent event) {
        Player    player     = (Player) event.getWhoClicked();
        ItemStack clicked    = event.getCurrentItem();

        event.setCancelled(true);
        if(clicked.equals(ITEM_CRAFT)) {
            new RecipeGUI(1, RecipeManager.RecipeType.CRAFTING).open(player);
        } else if(clicked.equals(ITEM_SMELT)) {
            new RecipeGUI(1, RecipeManager.RecipeType.SMELTING).open(player);
        }
    }

    /*****************************************************************/
    /*                      Private Methods                          */
    /*****************************************************************/

    protected void init() {
        ItemMeta meta;

        // Back icon
        ITEM_CRAFT = new ItemStack(Material.WORKBENCH, 1);
        meta       = ITEM_CRAFT.getItemMeta();
        meta.setDisplayName(ICON_CRAFT_NAME);
        ITEM_CRAFT.setItemMeta(meta);

        // Delete icon
        ITEM_SMELT = new ItemStack(Material.FURNACE, 1);
        meta       = ITEM_SMELT.getItemMeta();
        meta.setDisplayName(ICON_SMELT_NAME);
        ITEM_SMELT.setItemMeta(meta);

        // Permission Icon
        ITEM_BREW = new ItemStack(Material.BREWING_STAND_ITEM, 1);
        meta      = ITEM_BREW.getItemMeta();
        meta.setDisplayName(ICON_BREW_NAME);
        ITEM_BREW.setItemMeta(meta);

        // Update icon
        ITEM_REPAIR = new ItemStack(Material.ANVIL, 1);
        meta         = ITEM_REPAIR.getItemMeta();
        meta.setDisplayName(ICON_REPAIR_NAME);
        ITEM_REPAIR.setItemMeta(meta);

        init = true;
    }
}

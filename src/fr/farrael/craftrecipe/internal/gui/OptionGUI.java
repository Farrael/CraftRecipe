package fr.farrael.craftrecipe.internal.gui;

import fr.farrael.craftrecipe.internal.RecipeManager;
import fr.farrael.craftrecipe.internal.recipes.BaseRecipe;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.HashMap;
import java.util.UUID;

public class OptionGUI extends BaseGUI {

    /*****************************************************************/
    /*                         INVENTORY                             */
    /*****************************************************************/

    // Icons names
    private static final String BACK_NAME       = ChatColor.BLUE + "\u2B05 Retour";
    private static final String DELETE_NAME     = ChatColor.RED + "\u2716 Supprimer";
    private static final String UPDATE_NAME     = ChatColor.GREEN + "Modifier";
    private static final String PERMISSION_NAME = ChatColor.DARK_PURPLE + "Permission : ";
    private static final String ENABLE_NAME     = ChatColor.GREEN + "Status : Enable";
    private static final String DISABLE_NAME    = ChatColor.GOLD + "Status : Disable";

    // Icons items
    private static ItemStack BACK_ITEM;
    private static ItemStack DELETE_ITEM;
    private static ItemStack UPDATE_ITEM;
    private static ItemStack ENABLE_ITEM;
    private static ItemStack DISABLE_ITEM;
    private static ItemStack PERMISSION_ITEM;

    // Is icon initiate
    protected static boolean init = false;

    // Permissions requests
    public static HashMap<UUID, Integer>                  PERMISSION_REQUEST_SLOT = new HashMap<>();
    public static HashMap<UUID, RecipeManager.RecipeType> PERMISSION_REQUEST_TYPE = new HashMap<>();

    /*****************************************************************/
    /*                          HOLDER                               */
    /*****************************************************************/

    private int                      slot;
    private RecipeManager.RecipeType type;

    public OptionGUI(int slot, RecipeManager.RecipeType type) {
        this.slot = slot;
        this.type = type;

        // Inventory size and title
        this.size  = 18;
        this.title = ChatColor.BLUE + "Editing slot " + this.slot;
    }

    /*****************************************************************/
    /*                       Opening the GUI                         */

    /*****************************************************************/

    @Override
    public void open(Player player) {
        if (!init)
            this.init();

        // Retrieve recipe
        BaseRecipe recipe = this.type.getRecipe(this.slot);

        // Unknown recipe
        if(recipe == null)
            return;

        // Create inventory with holder
        Inventory inventory = this.getInventory();

        // Icon
        inventory.setItem(4, recipe.getResult());

        // Button
        inventory.setItem(9,  UPDATE_ITEM);
        inventory.setItem(11, DELETE_ITEM);
        inventory.setItem(13, getPermission(recipe.getPermission()));
        inventory.setItem(17, BACK_ITEM);

        // Status
        if(recipe.isRegistered())
            inventory.setItem(15, ENABLE_ITEM);
        else
            inventory.setItem(15, DISABLE_ITEM);

        // Open player inventory
        player.openInventory(inventory);
    }

    /*****************************************************************/
    /*                          Static                               */
    /*****************************************************************/

    private ItemStack getPermission(String perm) {
        ItemStack clone = PERMISSION_ITEM.clone();
        ItemMeta  meta  = clone.getItemMeta();
        meta.setDisplayName(PERMISSION_NAME + perm);
        clone.setItemMeta(meta);

        return clone;
    }

    /*****************************************************************/
    /*                     Click on the GUI                          */
    /*****************************************************************/

    @Override
    public void onClickEvent(InventoryClickEvent event) {
        Player    player     = (Player) event.getWhoClicked();
        ItemStack clicked    = event.getCurrentItem();
        Inventory inventory  = this.getInventory();

        event.setCancelled(true);

        if(clicked == null || clicked.getType() == Material.AIR)
            return;

        if(clicked.equals(OptionGUI.BACK_ITEM))
            new RecipeGUI(RecipeGUI.getSlotPage(this.slot), this.type).open(player);
        else if(clicked.equals(OptionGUI.DISABLE_ITEM) || clicked.equals(OptionGUI.ENABLE_ITEM)) {
            BaseRecipe recipe = this.type.getRecipe(this.slot);
            if(recipe == null)
                return;

            if(clicked.equals(OptionGUI.ENABLE_ITEM)) {
                recipe.unregister();
                inventory.setItem(event.getSlot(), OptionGUI.DISABLE_ITEM);
            } else {
                recipe.register();
                inventory.setItem(event.getSlot(), OptionGUI.ENABLE_ITEM);
            }

            RecipeManager.updateAndSave(recipe);
        } else if(clicked.equals(OptionGUI.DELETE_ITEM)) {
            BaseRecipe recipe = this.type.getRecipe(this.slot);
            if(recipe == null)
                return;

            RecipeManager.remove(recipe);
            new RecipeGUI(RecipeGUI.getSlotPage(this.slot), this.type).open(player);
        } else if(clicked.equals(OptionGUI.UPDATE_ITEM)) {
            new EditGUI(this.slot, this.type).open(player);
        } else if(clicked.getData().equals(OptionGUI.PERMISSION_ITEM.getData())) {
            PERMISSION_REQUEST_SLOT.put(player.getUniqueId(), this.slot);
            PERMISSION_REQUEST_TYPE.put(player.getUniqueId(), this.type);
            player.closeInventory();
            player.sendMessage(net.md_5.bungee.api.ChatColor.GOLD + "Entrez la permission requise à la réalisation du craft.");
            player.sendMessage(net.md_5.bungee.api.ChatColor.GOLD + "Entrez '" + net.md_5.bungee.api.ChatColor.RED + "Aucune" + net.md_5.bungee.api.ChatColor.GOLD + "' pour effacer la permission.");
        }
    }

    /*****************************************************************/
    /*                         Initiate                              */
    /*****************************************************************/

    protected void init() {
        ItemMeta meta;

        // Back icon
        BACK_ITEM = new ItemStack(Material.STAINED_GLASS_PANE, 1, (byte) 11);
        meta      = BACK_ITEM.getItemMeta();
        meta.setDisplayName(BACK_NAME);
        BACK_ITEM.setItemMeta(meta);

        // Delete icon
        DELETE_ITEM = new ItemStack(Material.STAINED_GLASS_PANE, 1, (byte) 14);
        meta        = DELETE_ITEM.getItemMeta();
        meta.setDisplayName(DELETE_NAME);
        DELETE_ITEM.setItemMeta(meta);

        // Permission Icon
        PERMISSION_ITEM = new ItemStack(Material.STAINED_GLASS_PANE, 1, (byte) 2);
        meta            = PERMISSION_ITEM.getItemMeta();
        meta.setDisplayName(PERMISSION_NAME);
        PERMISSION_ITEM.setItemMeta(meta);

        // Update icon
        UPDATE_ITEM = new ItemStack(Material.STAINED_GLASS_PANE, 1, (byte) 4);
        meta        = UPDATE_ITEM.getItemMeta();
        meta.setDisplayName(UPDATE_NAME);
        UPDATE_ITEM.setItemMeta(meta);

        // Enable icon
        ENABLE_ITEM = new ItemStack(Material.STAINED_GLASS_PANE, 1, (byte) 5);
        meta        = ENABLE_ITEM.getItemMeta();
        meta.setDisplayName(ENABLE_NAME);
        ENABLE_ITEM.setItemMeta(meta);

        // Disable icon
        DISABLE_ITEM = new ItemStack(Material.STAINED_GLASS_PANE, 1, (byte) 1);
        meta         = DISABLE_ITEM.getItemMeta();
        meta.setDisplayName(DISABLE_NAME);
        DISABLE_ITEM.setItemMeta(meta);

        init = true;
    }
}

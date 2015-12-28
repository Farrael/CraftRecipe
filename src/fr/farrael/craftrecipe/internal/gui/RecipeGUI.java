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

import java.util.ArrayList;

public class RecipeGUI extends BaseGUI {

    /*****************************************************************/
    /*                         INVENTORY                             */
    /*****************************************************************/

    // Icons names
    private static final String BORDER_NAME   = ChatColor.RED + "\u2716";
    private static final String NEXT_NAME     = ChatColor.GOLD + "Page suivante \u2192";
    private static final String PREVIOUS_NAME = ChatColor.GOLD + "\u2190 Page precedente";
    private static final String SIGN_NAME     = ChatColor.RED + "Informations";

    // Icons items
    private static ItemStack BORDER_ITEM;
    private static ItemStack NEXT_ITEM;
    private static ItemStack PREVIOUS_ITEM;
    private static ItemStack SIGN_ITEM;

    // Is icon initiate
    protected static boolean init = false;

    /*****************************************************************/
    /*                          HOLDER                               */
    /*****************************************************************/

    private int                      page;
    private RecipeManager.RecipeType type;

    public RecipeGUI(int page, RecipeManager.RecipeType type) {
        // Page 0 do not exist
        if (page == 0)
            page++;

        this.page = page;
        this.type = type;

        // Inventory title
        this.title = ChatColor.BLUE + this.type.toString() + " Recipes (Page " + this.page + ")";
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

        // Default inventory design
        for(int i = 0; i < 6; i++) {
            if(i == 0 || i == 5) {
                for (int j = 0; j < 9; j++)
                    inventory.setItem(i * 9 + j, BORDER_ITEM);
            } else {
                inventory.setItem(i * 9, BORDER_ITEM);
                inventory.setItem(i * 9 + 8, BORDER_ITEM);
            }
        }

        // Top sign
        inventory.setItem(4, SIGN_ITEM);

        // Previous page
        if(this.page > 1)
            inventory.setItem(47, PREVIOUS_ITEM);

        // Next page
        inventory.setItem(51, NEXT_ITEM);

        // Fill inventory with recipe
        BaseRecipe recipe;
        ItemStack item;
        ItemMeta meta;
        for(int i = 28 * (this.page - 1); i < 28 * this.page; i++) {
            recipe = this.type.getRecipe(i);
            if(recipe != null) {
                item = recipe.getResult();
                meta = item.getItemMeta();
                ArrayList<String> lore = new ArrayList<>();
                lore.add(ChatColor.GOLD + "Status      : " + (recipe.isRegistered() ? ChatColor.GREEN + "Enable" : ChatColor.RED + "Disable"));
                lore.add(ChatColor.GOLD + "Permission : " + recipe.getPermission());
                meta.setLore(lore);
                item.setItemMeta(meta);

                inventory.setItem(RecipeGUI.getRealSlot(i), item);
            }
        }

        // Open player inventory
        player.openInventory(inventory);
    }

    /*****************************************************************/
    /*                          Static                               */
    /*****************************************************************/

    public static int getMenuSlot(int rawSlot, int page) {
        int line = rawSlot / 9;
        rawSlot = (rawSlot - 8) - 2 * line;
        return rawSlot + 28 * (page - 1);
    }

    public static int getRealSlot(int rawSlot) {
        rawSlot = rawSlot % 28;
        int line = rawSlot / 7;
        rawSlot = (rawSlot + 10) + 2 * line;

        return rawSlot;
    }

    public static int getSlotPage(int rawSlot) {
        return (rawSlot / 28) + 1;
    }

    /*****************************************************************/
    /*                     Click on the GUI                          */
    /*****************************************************************/

    @Override
    public void onClickEvent(InventoryClickEvent event) {
        Player    player     = (Player) event.getWhoClicked();
        ItemStack clicked    = event.getCurrentItem();

        event.setCancelled(true);

        // New craft
        if(clicked == null || clicked.getType() == Material.AIR) {
            new EditGUI(RecipeGUI.getMenuSlot(event.getSlot(), this.page), this.type).open(player);
        } else {
            if(clicked.equals(RecipeGUI.NEXT_ITEM))
                new RecipeGUI(this.page + 1, this.type).open(player);
            else if(clicked.equals(RecipeGUI.PREVIOUS_ITEM))
                new RecipeGUI(this.page - 1, this.type).open(player);
            else if(!clicked.equals(RecipeGUI.BORDER_ITEM))
                new OptionGUI(RecipeGUI.getMenuSlot(event.getSlot(), page), this.type).open(player);
        }
    }

    /*****************************************************************/
    /*                         Initiate                              */
    /*****************************************************************/

    protected void init() {
        ItemMeta meta;

        // Border icon
        BORDER_ITEM = new ItemStack(Material.STAINED_GLASS_PANE, 1, (byte) 14);
        meta        = BORDER_ITEM.getItemMeta();
        meta.setDisplayName(BORDER_NAME);
        BORDER_ITEM.setItemMeta(meta);

        // Next icon
        NEXT_ITEM = new ItemStack(Material.DIODE);
        meta      = NEXT_ITEM.getItemMeta();
        meta.setDisplayName(NEXT_NAME);
        NEXT_ITEM.setItemMeta(meta);

        // Previous Icon
        PREVIOUS_ITEM = new ItemStack(Material.DIODE);
        meta          = PREVIOUS_ITEM.getItemMeta();
        meta.setDisplayName(PREVIOUS_NAME);
        PREVIOUS_ITEM.setItemMeta(meta);

        // Sign icon
        SIGN_ITEM = new ItemStack(Material.SIGN, 1);
        meta      = SIGN_ITEM.getItemMeta();
        meta.setDisplayName(SIGN_NAME);
        ArrayList<String> lore = new ArrayList<>();
        lore.add(ChatColor.GREEN + "Slot vide : " + ChatColor.BLUE + "Nouveau craft");
        lore.add(ChatColor.GREEN + "Slot plein : " + ChatColor.BLUE + "Editer le craft");
        meta.setLore(lore);
        SIGN_ITEM.setItemMeta(meta);

        init = true;
    }
}

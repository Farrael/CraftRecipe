package fr.farrael.craftrecipe.internal.gui;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

public abstract class BaseGUI implements InventoryHolder {

    /*****************************************************************/
    /*                           HOLDER                              */
    /*****************************************************************/

    // Inventory
    private Inventory  inv;

    // Default title and size
    protected int    size  = 54;
    protected String title = "BaseGUI";

    public Inventory getInventory() {
        if(this.inv == null)
            this.inv = Bukkit.createInventory(this, this.size, this.title);

        return this.inv;
    }

    /*****************************************************************/
    /*                       Opening the GUI                         */
    /*****************************************************************/

    /**
     * Open the GUI to the player
     * @param player - Player to open GUI
     */
    public abstract void open(Player player);

    /*****************************************************************/
    /*                     Click on the GUI                          */
    /*****************************************************************/

    /**
     * Called when user click on the inventory
     * @param event - Click event
     */
    public abstract void onClickEvent(InventoryClickEvent event);
}

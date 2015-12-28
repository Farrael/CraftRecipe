package fr.farrael.craftrecipe.listeners;

import fr.farrael.craftrecipe.Configuration;
import fr.farrael.craftrecipe.internal.RecipeManager;
import fr.farrael.craftrecipe.internal.gui.BaseGUI;
import fr.farrael.craftrecipe.internal.gui.RecipeGUI;
import fr.farrael.craftrecipe.internal.recipes.BaseRecipe;
import fr.farrael.craftrecipe.internal.recipes.CraftingRecipe;
import fr.farrael.craftrecipe.internal.recipes.SmeltingRecipe;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.*;
import org.bukkit.inventory.CraftingInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;

public class InventoryListener implements Listener {

    /*****************************************************************/
    /*                         Listeners                             */
    /*****************************************************************/

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        // Event previously cancelled
        if(event.isCancelled())
            return;

        // Viewer is not a player
        if(!(event.getView().getPlayer() instanceof Player))
            return;

        if(event.getClickedInventory() == null)
            return;

        if(event.getClickedInventory().getHolder() != null && event.getClickedInventory().getHolder() instanceof BaseGUI)
            ((BaseGUI) event.getClickedInventory().getHolder()).onClickEvent(event);
        else if(event.getInventory().getType().equals(InventoryType.FURNACE) && event.getSlot() != 2) {
            if(event.getSlot() == 1 && event.getCursor() == null)
                return;

            ItemStack component = event.getInventory().getItem(0);
            if(event.getClickedInventory().getType().equals(InventoryType.PLAYER)) {
                if(event.isShiftClick()) {
                    if (component == null || component.getType().equals(Material.AIR))
                        component = event.getCurrentItem();
                } else
                    return;
            } else if(event.getSlot() == 0)
                component = event.getCursor();

            event.setCancelled(this.onSmeltRecipe(event.getInventory(), (Player) event.getView().getPlayer(), component));
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onInventoryDrag(InventoryDragEvent event) {
        // Event previously cancelled
        if(event.isCancelled())
            return;

        // Viewer is not a player
        if(!(event.getWhoClicked() instanceof Player))
            return;

        Inventory inventory = event.getInventory();
        if(inventory == null)
            return;

        if(inventory.getHolder() != null && inventory.getHolder() instanceof RecipeGUI) {
            for(int index : event.getNewItems().keySet()) {
                if(index < 54) {
                    event.setCancelled(true);
                    break;
                }
            }
        } else if(inventory.getType().equals(InventoryType.FURNACE)) {
            ItemStack component = null;
            if(event.getNewItems().containsKey(0) || event.getNewItems().containsKey(1)) {
                if(event.getNewItems().containsKey(0))
                    component = event.getNewItems().get(0);
                else
                    component = event.getInventory().getItem(0);
            }

            event.setCancelled(this.onSmeltRecipe(event.getInventory(), (Player) event.getWhoClicked(), component));
        }

    }

    @EventHandler(priority = EventPriority.LOW)
    public void onInventoryMove(InventoryMoveItemEvent event) {
        if(event.getDestination().getType().equals(InventoryType.FURNACE)) {
            event.setCancelled(this.onSmeltRecipe(event.getDestination(), null, event.getItem()));
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onPrepareCraft(PrepareItemCraftEvent event) {
        // Event previously cancelled
        if(event.getInventory().getResult() == null)
            return;

        // Viewer is not a player
        if(!(event.getView().getPlayer() instanceof Player))
            return;

        // Repair
        if(event.isRepair())
            this.onRepairRecipe(event);

        // Crafting
        if(event.getRecipe() instanceof ShapedRecipe)
            this.onCraftRecipe(event);
    }

    /*****************************************************************/
    /*                       Check recipes                           */
    /*****************************************************************/

    private void onCraftRecipe(PrepareItemCraftEvent event) {
        CraftingInventory inventory = event.getInventory();
        Player player = (Player) event.getView().getPlayer();

        // Is crafting enable
        if(!RecipeManager.RecipeType.CRAFTING.isValid())
            return;

        boolean delete;
        CraftingRecipe recipe;
        for(BaseRecipe base : RecipeManager.RecipeType.CRAFTING.getRecipes().values()) {
            if(base.isSimilar(event.getRecipe())) {

                // Plugin disabled
                if(!Configuration.ENABLE) {
                    inventory.setResult(null);
                    break;
                }

                // Converte recipe
                recipe = (CraftingRecipe) base;

                // Check recipe components
                ItemStack itemRecipe;
                ItemStack itemInventory;

                delete = false;
                for(int i = 1; i < inventory.getSize(); i++) {
                    itemRecipe     = recipe.getItem(i - 1);
                    itemInventory  = inventory.getItem(i);
                    if(itemRecipe == null || itemInventory == null) {
                        if(itemRecipe != itemInventory)
                            delete = true;
                    } else if(!itemInventory.equals(itemRecipe))
                        delete = true;

                    if(delete)
                        break;
                }

                if(!delete && recipe.canCraft(player)) {
                    event.getInventory().setResult(recipe.getResult());
                    break;
                } else {
                    event.getInventory().setResult(null);
                }
            }
        }
    }

    private boolean onSmeltRecipe(Inventory inventory, Player player, ItemStack  item) {
        // Is crafting enable
        if(!RecipeManager.RecipeType.SMELTING.isValid())
            return false;

        if(item != null) {
            SmeltingRecipe recipe;
            for (BaseRecipe base : RecipeManager.RecipeType.SMELTING.getRecipes().values()) {
                recipe = (SmeltingRecipe) base;
                if(recipe.getIngredient().isSimilar(item)) {
                    if(player == null)
                        return recipe.hasPermission();
                    else if(!recipe.canCraft(player)) {
                        player.sendMessage(ChatColor.RED + "Vous ne pouvez utiliser cette recette.");
                        return true;
                    }
                }
            }
        }

        return false;
    }

    private void onRepairRecipe(PrepareItemCraftEvent event) {
        // @Todo
    }
}

package fr.farrael.craftrecipe.listeners;

import fr.farrael.craftrecipe.Configuration;
import fr.farrael.craftrecipe.CraftRecipe;
import fr.farrael.craftrecipe.internal.RecipeManager;
import fr.farrael.craftrecipe.internal.gui.OptionGUI;
import fr.farrael.craftrecipe.internal.recipes.BaseRecipe;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class PlayerListener implements Listener {
    CraftRecipe plugin = CraftRecipe.getInstance();

    @EventHandler(priority = EventPriority.LOW)
    public void onPlayerSpeak(AsyncPlayerChatEvent event) {
        if(!Configuration.ENABLE)
            return;

        Player player = event.getPlayer();
        if(!OptionGUI.PERMISSION_REQUEST_SLOT.containsKey(player.getUniqueId()))
            return;

        // Cancel event
        event.setCancelled(true);

        int                      slot = OptionGUI.PERMISSION_REQUEST_SLOT.remove(player.getUniqueId());
        RecipeManager.RecipeType type = OptionGUI.PERMISSION_REQUEST_TYPE.remove(player.getUniqueId());
        String message = event.getMessage();

        String perm = null;
        if(!message.equalsIgnoreCase("aucune"))
            perm = message;

        BaseRecipe recipe = type.getRecipe(slot);
        if(recipe != null) {
            recipe.setPermission(perm);
            RecipeManager.updateAndSave(recipe);
        }

        plugin.sendPluginMessage(player, "Nouvelle permission : " + (perm != null ? ChatColor.GREEN + "CraftRecipe.recipe." + perm : ChatColor.RED + "Aucune."), false);
        new OptionGUI(slot, type).open(player);
    }
}

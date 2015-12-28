package fr.farrael.craftrecipe.commands;

import fr.farrael.craftrecipe.Configuration;
import fr.farrael.craftrecipe.CraftRecipe;
import fr.farrael.craftrecipe.external.Attributes;
import fr.farrael.craftrecipe.external.EnchantGlow;
import fr.farrael.craftrecipe.internal.gui.SelectGUI;
import fr.farrael.craftrecipe.internal.tasks.StingTask;
import fr.farrael.craftrecipe.utils.ChatUtil;
import fr.farrael.rootlib.api.command.annotation.Command;
import fr.farrael.rootlib.api.command.annotation.CommandHandler;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@CommandHandler(alias = "craftrecipe")
public class GlobalCommands {

    // Need to move from here !!!
    private static final HashMap<String, Attributes.AttributeType> attributes;
    static {
        attributes = new HashMap<>();
        attributes.put("Health"   , Attributes.AttributeType.GENERIC_MAX_HEALTH);
        attributes.put("Speed"    , Attributes.AttributeType.GENERIC_MOVEMENT_SPEED);
        attributes.put("Strength" , Attributes.AttributeType.GENERIC_ATTACK_DAMAGE);
        attributes.put("Knockback", Attributes.AttributeType.GENERIC_KNOCKBACK_RESISTANCE);
    }

    private CraftRecipe plugin;

    public GlobalCommands(CraftRecipe plugin) {
        this.plugin = plugin;
    }

    /*****************************************************************/
    /*                      Basic Commands                           */
    /*****************************************************************/

    @Command(permission = "CraftRecipe.info")
    public boolean info(CommandSender sender) {

        sender.sendMessage(ChatColor.GOLD + "\n#--------[" + ChatColor.BLUE + this.plugin.getName() + ChatColor.GOLD + "]--------#");
        sender.sendMessage(ChatColor.GOLD + "# " + ChatColor.AQUA + "Description : " + ChatColor.GREEN + this.plugin.getDescription().getDescription());
        sender.sendMessage(ChatColor.GOLD + "# " + ChatColor.AQUA + "Version : " + ChatColor.GREEN + this.plugin.getDescription().getVersion());
        sender.sendMessage(ChatColor.GOLD + "#");
        sender.sendMessage(ChatColor.GOLD + "# " + ChatColor.AQUA + "Contributor : " + ChatColor.GREEN + "Farrael");
        sender.sendMessage(ChatColor.GOLD + "#--------------------------#\n");

        return true;
    }

    @Command(alias = "help", permission = "CraftRecipe.help")
    public boolean help(CommandSender sender) {
        sender.sendMessage(ChatColor.GOLD + "\n#----- " + ChatColor.GREEN + "Aide [1/1]" + ChatColor.GOLD + " -----#");
        sender.sendMessage(ChatUtil.getUsage("/cr edit : Ouvre le menu de crafting."));
        sender.sendMessage(ChatUtil.getUsage("/cr reload : Reload la configuration."));
        sender.sendMessage(ChatUtil.getUsage("/cr [on/off] : Active ou désactive le plugin."));
        sender.sendMessage(ChatUtil.getUsage("/cr item [name/lore/flag/atb] <data> : Editer l'objet en main."));

        return true;
    }

    /*****************************************************************/
    /*                      Status / Reload                          */
    /*****************************************************************/

    @Command(alias = "on", permission = "CraftRecipe.status")
    public boolean start(CommandSender sender) {
        return this.setStatus(sender, true);
    }

    @Command(alias = "off", permission = "CraftRecipe.status")
    public boolean stop(CommandSender sender) {
        return this.setStatus(sender, false);
    }

    @Command(alias = "reload", permission = "CraftRecipe.reload")
    public boolean reload(CommandSender sender) {
        StingTask.stop();

        this.plugin.configuration.reload(Configuration.FILE_CONFIG, true);
        this.plugin.sendPluginMessage(sender, "Config reloaded.", false);

        if(Configuration.STING_ENABLE)
            StingTask.start();

        return true;
    }

    @Command(alias = "@...")
    public boolean invalid(CommandSender sender, String value) {
        ChatUtil.sendError(sender, ChatColor.GOLD + "Arguments [" + ChatColor.RED + value + ChatColor.GOLD + "] invalids...");
        sender.sendMessage(ChatColor.GOLD + "Tapez " + ChatColor.RED + "/cr help" + ChatColor.GOLD + " pour les informations sur les commandes.");
        return true;
    }

    /*****************************************************************/
    /*                     Open GUI Inventory                        */
    /*****************************************************************/

    @Command(alias = "edit", permission = "CraftRecipe.edit", sender = Command.SenderType.PLAYER)
    public boolean open(CommandSender sender) {
        new SelectGUI().open((Player) sender);

        return true;
    }

    /*****************************************************************/
    /*                      Modify ItemStack                         */
    /*****************************************************************/

    @Command(alias = "item name @...", permission = "CraftRecipe.item.name", sender = Command.SenderType.PLAYER, usage = "/cr item name <value>")
    public boolean name(CommandSender sender, String name) {
        Player player = (Player) sender;

        if(player.getItemInHand() == null || player.getItemInHand().getType() == Material.AIR) {
            ChatUtil.sendError(sender, ChatColor.RED + "Vous devez tenir un objet en main.");
            return false;
        }

        ItemStack item = player.getItemInHand();
        ItemMeta meta  = item.getItemMeta();
        meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', name));
        item.setItemMeta(meta);
        player.setItemInHand(item);

        this.plugin.sendPluginMessage(player, "Vous venez de renommer l'objet.", false);

        return true;
    }

    @Command(alias = "item lore @ @...", permission = "CraftRecipe.item.lore", sender = Command.SenderType.PLAYER, usage = "/cr item lore <ligne> <value>")
    public boolean lore(CommandSender sender, int line, String value) {
        if (line < 1 || line > 6) {
            ChatUtil.sendError(sender, ChatColor.GOLD + "Le numero de la ligne doit être compris entre 1 et 6.");
            return false;
        }

        Player player  = (Player) sender;
        ItemStack item = player.getItemInHand();
        ItemMeta meta  = item.getItemMeta();
        List<String> lore = meta.hasLore() ? meta.getLore() : new ArrayList<String>();

        while (lore.size() < line)
            lore.add("");

        lore.set(line - 1, ChatColor.translateAlternateColorCodes('&', value));
        meta.setLore(lore);
        item.setItemMeta(meta);
        player.setItemInHand(item);

        plugin.sendPluginMessage(player, "Vous venez de modifier le lore de l'objet.", false);

        return true;
    }

    @Command(alias = "item flag @ @", permission = "CraftRecipe.item.flag", sender = Command.SenderType.PLAYER, usage = "/cr item flag <name> <boolean>")
    public boolean flag(CommandSender sender, ItemFlag flag, boolean value) {
        Player player = (Player) sender;

        ItemStack item = player.getItemInHand();
        ItemMeta meta  = item.getItemMeta();

        if (value)
            meta.addItemFlags(flag);
        else
            meta.removeItemFlags(flag);

        item.setItemMeta(meta);
        player.setItemInHand(item);

        plugin.sendPluginMessage(player, "Vous venez " + (value ? "d'ajouter" : "de retirer") + " le flag [" + ChatColor.WHITE + flag.name().toLowerCase() + ChatColor.BLUE + "] à  l'objet.", false);

        return true;
    }

    @Command(alias = "item atb @ @", permission = "CraftRecipe.item.attributes", sender = Command.SenderType.PLAYER, usage = "/cr item atb <name> <value>")
    public boolean attributes(CommandSender sender, String name, double value) {
        Player player = (Player) sender;
        String atb    = ChatUtil.firstUppercase(name);

        if (!attributes.containsKey(atb)) {
            ChatUtil.sendError(player, ChatColor.RED + "L'attribut [" + ChatColor.WHITE + atb + ChatColor.RED + "] n'existe pas.");
            player.sendMessage(ChatColor.GOLD + " Liste des attributs : ");
            for (String a : attributes.keySet())
                player.sendMessage("    - " + a);

            return false;
        }

        Attributes.AttributeType at_type = attributes.get(atb);

        // Prepare item
        ItemStack item       = player.getItemInHand();
        Attributes attribute = new Attributes(item);

        // Search if attribute already set
        Attributes.Attribute result = null;
        for(int i = 0, size = attribute.size(); i < size; i++) {
            Attributes.Attribute a = attribute.get(i);
            if(a.getAttributeType().equals(at_type)) {
                result = a;
                break;
            }
        }

        // Update or set value
        if(result != null)
            result.setAmount(value);
        else
            attribute.add(Attributes.Attribute.newBuilder().name(atb).type(at_type).amount(value).build());

        // Update player item
        player.setItemInHand(attribute.getStack());

        plugin.sendPluginMessage(player, "Vous venez de modifier l'attribut [" + ChatColor.WHITE + atb + ChatColor.BLUE + "] de l'objet.", false);

        return true;
    }

    @Command(alias = "item glow @", permission = "CraftRecipe.item.glow", sender = Command.SenderType.PLAYER, usage = "/cr item glow <boolean>")
    public boolean glow(CommandSender sender, boolean value) {
        Player player = (Player) sender;

        if(value)
            EnchantGlow.addGlow(player.getItemInHand());
        else
            EnchantGlow.removeGlow(player.getItemInHand());

        return true;
    }

    /*****************************************************************/
    /*                      Private Methods                          */
    /*****************************************************************/

    private boolean setStatus(CommandSender sender, boolean value) {
        Configuration.ENABLE = true;
        this.plugin.configuration.getFileManager().setData(Configuration.FILE_CONFIG, "enable", true);
        this.plugin.sendPluginMessage(sender, "Vous venez " + (value ? "d'activer" : "de désactiver") + " le plugin.", false);

        return true;
    }
}

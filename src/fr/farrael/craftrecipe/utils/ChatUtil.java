package fr.farrael.craftrecipe.utils;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

public class ChatUtil {

    public static String permission = ChatColor.RED + "Vous ne pouvez pas utiliser cette commande.";
    public static String player     = ChatColor.RED + "Impossible d'exécuter cela via la console.";

    public static void sendError(final CommandSender sender, final String message) {
        sender.sendMessage("\n[" + ChatColor.RED + "Error" + ChatColor.WHITE + "] " + message);
    }

    public static String getUsage(final String command){
        String[] splitted = command.split(":");

        String cmd = splitted[0];
        String effect = splitted.length > 1 ? splitted[1] : "";

        cmd = cmd.replace("[", ChatColor.RED + "[").replace("]", "]" + ChatColor.GOLD);
        cmd = cmd.replace("<", ChatColor.GRAY + "<").replace(">", ">" + ChatColor.GOLD);

        return ChatColor.GOLD + cmd + (effect.length() > 1 ? " : " + ChatColor.GREEN + effect : "");
    }

    public static String firstUppercase(String s) {
        return s.substring(0, 1).toUpperCase() + s.toLowerCase().substring(1);
    }
}
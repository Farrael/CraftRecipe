package fr.farrael.craftrecipe.commands;

import fr.farrael.craftrecipe.utils.ChatUtil;
import fr.farrael.rootlib.api.command.modele.MessageHandler;
import org.bukkit.ChatColor;

public class CommandMessage extends MessageHandler {

    /*****************************************************************/
    /*                      Commands Usage                           */
    /*****************************************************************/

    @Override
    public String onWrongUsage(String usage, String parser) {
        // Default error message
        String error = "\n[" + ChatColor.RED + "Error" + ChatColor.WHITE + "] " + ChatColor.GOLD + "Arguments invalids...";

        // Split command
        String[] splitted = usage.split(":");
        String cmd        = splitted[0];
        String effect     = splitted.length > 1 ? splitted[1] : "";

        // Set arguments color
        cmd = cmd.replace("[", ChatColor.RED + "[").replace("]", "]" + ChatColor.GOLD);
        cmd = cmd.replace("<", ChatColor.GRAY + "<").replace(">", ">" + ChatColor.GOLD);

        // Add arguments to the message
        error += "\n" + ChatColor.GOLD + cmd + (effect.length() > 1 ? " : " + ChatColor.GREEN + effect : "");

        // Enum message parsing
        if(parser.startsWith("Enum:")) {
            String[] flags = parser.split("@")[1].split(":");
            error += "\n" + ChatColor.GOLD + "Liste des valeurs :";
            for(String value : flags)
                error += ChatColor.GOLD + "\n    - " + ChatColor.WHITE + ChatUtil.firstUppercase(value);
        }

        return error;
    }
}

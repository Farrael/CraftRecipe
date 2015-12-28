package fr.farrael.craftrecipe;

import fr.farrael.craftrecipe.commands.CommandMessage;
import fr.farrael.craftrecipe.commands.GlobalCommands;
import fr.farrael.craftrecipe.internal.recipes.CraftingRecipe;
import fr.farrael.craftrecipe.internal.recipes.SmeltingRecipe;
import fr.farrael.craftrecipe.internal.tasks.StingTask;
import fr.farrael.craftrecipe.listeners.InventoryListener;
import fr.farrael.craftrecipe.listeners.PlayerListener;
import fr.farrael.rootlib.api.command.CommandManager;
import fr.farrael.rootlib.api.configuration.ConfigManager;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

public class CraftRecipe extends JavaPlugin {
    // Register Class
    static {
        ConfigurationSerialization.registerClass(CraftingRecipe.class);
        ConfigurationSerialization.registerClass(SmeltingRecipe.class);
    }

    //-----------/ Static /------------//
    private static CraftRecipe instance;
    public static  String      label;

    //-------/ Configurations /--------//
    public ConfigManager  configuration;
    public CommandManager commands;

    @Override
    public void onEnable() {
        instance = this;
        label = ChatColor.BLUE + "[" + ChatColor.YELLOW + this.getName() + ChatColor.BLUE + "] ";

        // Register Listeners
        this.registreEvents(new InventoryListener(), new PlayerListener());

        // Register configuration
        this.configuration = new ConfigManager(this);
        this.configuration.register(new Configuration());
        this.configuration.loadAll();

        // Register commands
        this.commands = new CommandManager(this);
        this.commands.register(new GlobalCommands(this));
        this.commands.setDefaultHandler(new CommandMessage());

        // Start Task
        if (Configuration.STING_ENABLE)
            StingTask.start();
    }

    /**
     * Register list of Listeners.
     *
     * @param listeners - Listeners to register
     */
    public void registreEvents(Listener... listeners) {
        for (Listener listener : listeners) {
            getServer().getPluginManager().registerEvents(listener, this);
        }
    }

    /**
     * Send message with plugin label.
     *
     * @param target  - Target
     * @param message - Message to send
     */
    public boolean sendPluginMessage(CommandSender target, String message, boolean isError) {
        ChatColor color = ChatColor.BLUE;
        if(isError)
            color = ChatColor.RED;

        target.sendMessage(label + color + message);
        return false;
    }

    /*****************************************************************/
    /*                          Instance                             */
    /*****************************************************************/

    /**
     * Return the plugin instance
     */
    public static CraftRecipe getInstance() {
        return instance;
    }
}
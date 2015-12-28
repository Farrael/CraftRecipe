package fr.farrael.craftrecipe;

import fr.farrael.craftrecipe.internal.PacketManager;
import fr.farrael.craftrecipe.internal.RecipeManager;
import fr.farrael.craftrecipe.internal.recipes.BaseRecipe;
import fr.farrael.rootlib.api.configuration.ConfigManager;
import fr.farrael.rootlib.api.configuration.annotation.Config;
import fr.farrael.rootlib.api.configuration.internal.ConfigData;

public class Configuration {

    /*****************************************************************/
    /*                   List of Configurations                      */
    /*****************************************************************/

    public static boolean ENABLE;

    // Sting
    public static int     STING_TASK_DELAY;
    public static int     STING_DISTANCE;
    public static String  STING_CONTEXT;
    public static boolean STING_ENABLE;

    // File name
    public static final String FILE_CONFIG = "config.yml";
    public static final String FILE_CRAFT  = "craft.yml";

    /*****************************************************************/
    /*                   Loading Configurations                      */
    /*****************************************************************/

    @Config(file = FILE_CONFIG)
    public void loadConfig(ConfigData config) {
        ConfigManager manager = CraftRecipe.getInstance().configuration;

        // Enable / Disable debugging
        manager.setDebugState((boolean) config.getData("console_debug", false));

        // Get value.
        ENABLE           = (boolean) config.getData("enable"        , true);
        STING_TASK_DELAY = (int)     config.getData("sting_delay"   , 48);
        STING_DISTANCE   = (int)     config.getData("sting_distance", 15);
        STING_CONTEXT    = (String)  config.getData("sting_context" , "Brille à proximité des monstres.");

        STING_ENABLE = STING_TASK_DELAY > 0;

        PacketManager.unregister();
        if(STING_ENABLE)
            PacketManager.register();

        // Set commentaries.
        //config.setComment("enable"         , false, "Active ou désactive le plugin.");
        //config.setComment("console_debug"  , true, "Affiche les messages de debug dans la console.");
        //config.setComment("sting_delay"    , true, "Intervalle de temps entre les vérification (24 = 1sc)");
        //config.setComment("sting_distance" , true, "Distance de detection des monstres pour Sting.");
        //config.setComment("sting_context"  , true, "Lore obligatoire pour les objets de type Sting");
    }

    @Config(file = FILE_CRAFT, load = Config.LoadType.FILLED)
    public void loadCraft(ConfigData config) {
        // Loop through type
        for (String recipe_type : config.getKeys(false)) {
            for(String recipe_slot : config.getSection(recipe_type).getKeys(false)) {
                BaseRecipe recipe = (BaseRecipe) config.getData(recipe_type + "." + recipe_slot, null);
                RecipeManager.register(recipe, Integer.parseInt(recipe_slot));
            }
        }
    }
}
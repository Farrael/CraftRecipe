package fr.farrael.craftrecipe.internal.tasks;

import fr.farrael.craftrecipe.Configuration;
import fr.farrael.craftrecipe.CraftRecipe;
import fr.farrael.craftrecipe.external.EnchantGlow;
import fr.farrael.craftrecipe.external.NbtFactory;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;

public class StingTask implements Runnable {

    private static final NbtFactory.NbtList GLOW;
    static {
        NbtFactory.NbtCompound ENCH = NbtFactory.createCompound();
        ENCH.put("id" , (short) 1);
        ENCH.put("lvl", (short) 1);
        GLOW = NbtFactory.createList(ENCH);
    }

    /*****************************************************************/
    /*                        Task Manager                           */
    /*****************************************************************/

    private static BukkitTask task;

    /**
     * Start the task
     */
    public static void start() {
        if(task == null)
            task = Bukkit.getScheduler().runTaskTimer(CraftRecipe.getInstance(), new StingTask(), Configuration.STING_TASK_DELAY, Configuration.STING_TASK_DELAY);
    }

    /**
     * Stop the task
     */
    public static void stop() {
        if(task != null)
            task.cancel();
    }

    /**
     * Restart the task
     */
    public static void restart() {
        stop();
        start();
    }

    /*****************************************************************/
    /*                         Task Logic                            */
    /*****************************************************************/

    @Override
    public void run() {
        boolean near;

        // Check for all online players
        for(Player player : Bukkit.getOnlinePlayers()) {
            near = false;

            // Is monster near
            for(Entity e : player.getNearbyEntities(Configuration.STING_DISTANCE, 4, Configuration.STING_DISTANCE)) {
                if(e instanceof Monster) {
                    near = true;
                    break;
                }
            }

            // Check inventory
            for(ItemStack item : player.getInventory().getContents())
                this.setGlowing(item, near);

            // Check armor
            for(ItemStack item : player.getInventory().getArmorContents())
                this.setGlowing(item, near);
        }
    }

    private void setGlowing(ItemStack item, boolean glow) {
        if(item != null && item.hasItemMeta() && item.getItemMeta().hasLore()
                && item.getItemMeta().getLore().contains(Configuration.STING_CONTEXT)) {
            if(glow)
                EnchantGlow.addGlow(item);
            else
                EnchantGlow.removeGlow(item);
        }
    }
}

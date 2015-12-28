package fr.farrael.craftrecipe.internal;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.*;
import com.comphenix.protocol.utility.MinecraftReflection;
import com.comphenix.protocol.wrappers.nbt.NbtCompound;
import com.comphenix.protocol.wrappers.nbt.NbtFactory;
import fr.farrael.craftrecipe.Configuration;
import fr.farrael.craftrecipe.CraftRecipe;
import fr.farrael.craftrecipe.external.EnchantGlow;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemStack;

import java.io.DataInputStream;

public class PacketManager {

    // Create packets variables
    private static PacketListener PACKET_ITEMS;
    private static PacketListener PACKET_CREATIVE;
    private static PacketListener PACKET_EQUIPMENT;

    /*****************************************************************/
    /*                    Packets Initialization                     */
    /*****************************************************************/

    static {
        PacketAdapter.AdapterParameteters params;

        // Items filtering params
        params = PacketAdapter.params()
                .plugin(CraftRecipe.getInstance())
                .listenerPriority(ListenerPriority.HIGH)
                .types(PacketType.Play.Server.SET_SLOT,
                        PacketType.Play.Server.WINDOW_ITEMS);

        PACKET_ITEMS = new PacketAdapter(params) {
            public void onPacketSending(PacketEvent event) {
                PacketContainer packet = event.getPacket();
                PacketType      type   = event.getPacketType();

                if (type == PacketType.Play.Server.WINDOW_ITEMS) {
                    ItemStack[] items = packet.getItemArrayModifier().read(0);
                    for (ItemStack item : items)
                        removeEnchantments(item);
                } else {
                    removeEnchantments(packet.getItemModifier().read(0));
                }
            }
        };

        // Update player equipment
        params = PacketAdapter.params()
                .plugin(CraftRecipe.getInstance())
                .listenerPriority(ListenerPriority.HIGH)
                .types(PacketType.Play.Server.ENTITY_EQUIPMENT,
                        PacketType.Play.Server.NAMED_ENTITY_SPAWN);
        PACKET_EQUIPMENT = new PacketAdapter(params) {
            public void onPacketSending(PacketEvent event) {
                PacketContainer packet = event.getPacket();
                PacketType      type   = event.getPacketType();

                if (type == PacketType.Play.Server.ENTITY_EQUIPMENT) {
                    removeEnchantments(packet.getItemModifier().read(0));
                } else {
                    LivingEntity living = (LivingEntity) packet.getEntityModifier(event).read(0);
                    for (ItemStack item : living.getEquipment().getArmorContents())
                        removeEnchantments(item);
                }
            }
        };

        // Items receiving params
        params = PacketAdapter.params()
                .plugin(CraftRecipe.getInstance())
                .listenerPriority(ListenerPriority.HIGH)
                .options(ListenerOptions.INTERCEPT_INPUT_BUFFER)
                .types(PacketType.Play.Client.SET_CREATIVE_SLOT);

        PACKET_CREATIVE = new PacketAdapter(params) {
            public void onPacketReceiving(PacketEvent event) {
                if (event.getPacketType().equals(PacketType.Play.Client.SET_CREATIVE_SLOT)
                        || event.getPacketType().equals(PacketType.Play.Server.SET_SLOT)) {
                    DataInputStream input = event.getNetworkMarker().getInputStream();

                    if (input == null)
                        return;

                    // Read ItemStack
                    ItemStack item = event.getPacket().getItemModifier().read(0);

                    if (item != null) {
                        NbtCompound tag = NbtFactory.asCompound(NbtFactory.fromItemTag(item));
                        if (tag != null && tag.containsKey("CR_ENCH"))
                            tag.put("ench", tag.getList("CR_ENCH"));

                        // Send ItemStack
                        event.getPacket().getItemModifier().write(0, item);
                    }
                }
            }
        };
    }

    /*****************************************************************/
    /*                    Register / Unregister                      */
    /*****************************************************************/

    /**
     * Register ProtocolLib packets
     */
    public static void register() {
        ProtocolLibrary.getProtocolManager().addPacketListener(PACKET_ITEMS);
        ProtocolLibrary.getProtocolManager().addPacketListener(PACKET_CREATIVE);
        ProtocolLibrary.getProtocolManager().addPacketListener(PACKET_EQUIPMENT);
    }

    /**
     * Unregister ProtocolLib packets
     */
    public static void unregister() {
        ProtocolLibrary.getProtocolManager().removePacketListener(PACKET_ITEMS);
        ProtocolLibrary.getProtocolManager().removePacketListener(PACKET_CREATIVE);
        ProtocolLibrary.getProtocolManager().removePacketListener(PACKET_EQUIPMENT);
    }

    /*****************************************************************/
    /*                     Packet Listener Call                      */
    /*****************************************************************/

    /**
     * Remove all enchantments from {@link ItemStack}
     *
     * @param stack - Item to remove enchants
     */
    private static void removeEnchantments(ItemStack stack) {
        if(stack == null || !stack.hasItemMeta() || !stack.getItemMeta().hasLore() || !stack.getItemMeta().getLore().contains(Configuration.STING_CONTEXT))
            return;

        stack         = setEnchantTag(stack);
        Object[] copy = stack.getEnchantments().keySet().toArray();

        for (Object enchantment : copy) {
            if(!enchantment.equals(EnchantGlow.getGlow()))
                stack.removeEnchantment((Enchantment) enchantment);
        }
    }

    /**
     * Save enchantments into NBTag
     *
     * @param item - Item to save enchants
     *
     * @return ItemStack
     */
    private static ItemStack setEnchantTag(ItemStack item) {
        if (!MinecraftReflection.isCraftItemStack(item))
            item = MinecraftReflection.getBukkitItemStack(item);

        NbtCompound tag = NbtFactory.asCompound(NbtFactory.fromItemTag(item));
        if(tag.containsKey("ench"))
            tag.put("CR_ENCH", tag.getList("ench"));

        return item;
    }
}

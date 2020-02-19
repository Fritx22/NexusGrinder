/*
 Copyright 2014 stuntguy3000 (Luke Anderson), coasterman10 and F3DEX22.

 This program is free software; you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation; either version 2 of the License, or
 (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program; if not, write to the Free Software
 Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 MA 02110-1301, USA.
 */
package net.coasterman10.Annihilation;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Random;

import net.coasterman10.Annihilation.object.GameTeam;
import net.coasterman10.Annihilation.object.Kit;
import net.coasterman10.Annihilation.object.PlayerMeta;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.FireworkEffect.Type;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.inventory.meta.ItemMeta;

public class Util {
    /**
     * Particle Effects Lib
     * 
     * @author minnymin3
     * 
     */

    public enum ParticleEffects {

        HUGE_EXPLODE("hugeexplosion", 0), LARGE_EXPLODE("largeexplode", 1), FIREWORK_SPARK("fireworksSpark", 2), AIR_BUBBLE(
                "bubble", 3), SUSPEND("suspend", 4), DEPTH_SUSPEND("depthSuspend", 5), TOWN_AURA("townaura", 6), CRITICAL_HIT(
                "crit", 7), MAGIC_CRITICAL_HIT("magicCrit", 8), MOB_SPELL("mobSpell", 9), MOB_SPELL_AMBIENT(
                "mobSpellAmbient", 10), SPELL("spell", 11), INSTANT_SPELL("instantSpell", 12), BLUE_SPARKLE("witchMagic",
                13), NOTE_BLOCK("note", 14), ENDER("portal", 15), ENCHANTMENT_TABLE("enchantmenttable", 16), EXPLODE(
                "explode", 17), FIRE("flame", 18), LAVA_SPARK("lava", 19), FOOTSTEP("footstep", 20), SPLASH("splash", 21), LARGE_SMOKE(
                "largesmoke", 22), CLOUD("cloud", 23), REDSTONE_DUST("reddust", 24), SNOWBALL_HIT("snowballpoof", 25), DRIP_WATER(
                "dripWater", 26), DRIP_LAVA("dripLava", 27), SNOW_DIG("snowshovel", 28), SLIME("slime", 29), HEART("heart",
                30), ANGRY_VILLAGER("angryVillager", 31), GREEN_SPARKLE("happyVillager", 32), ICONCRACK("iconcrack", 33), TILECRACK(
                "tilecrack", 34);

        private String name;
        private int id;

        ParticleEffects(String name, int id) {
            this.name = name;
            this.id = id;
        }

        /**
         * Gets the name of the Particle Effect
         * 
         * @return The particle effect name
         */
        String getName() {
            return name;
        }

        /**
         * Gets the id of the Particle Effect
         * 
         * @return The id of the Particle Effect
         */
        int getId() {
            return id;
        }

        /**
         * Send a particle effect to a player
         * 
         * @param effect
         *            The particle effect to send
         * @param player
         *            The player to send the effect to
         * @param location
         *            The location to send the effect to
         * @param offsetX
         *            The x range of the particle effect
         * @param offsetY
         *            The y range of the particle effect
         * @param offsetZ
         *            The z range of the particle effect
         * @param speed
         *            The speed (or color depending on the effect) of the particle
         *            effect
         * @param count
         *            The count of effects
         */
        public static void sendToPlayer(ParticleEffects effect, Player player, Location location, float offsetX, float offsetY,
                float offsetZ, float speed, int count) {
            try {
                Object packet = createPacket(effect, location, offsetX, offsetY, offsetZ, speed, count);
                sendPacket(player, packet);
            } catch (Exception e) {
                e.printStackTrace();
            }

        }

        /**
         * Send a particle effect to all players
         * 
         * @param effect
         *            The particle effect to send
         * @param location
         *            The location to send the effect to
         * @param offsetX
         *            The x range of the particle effect
         * @param offsetY
         *            The y range of the particle effect
         * @param offsetZ
         *            The z range of the particle effect
         * @param speed
         *            The speed (or color depending on the effect) of the particle
         *            effect
         * @param count
         *            The count of effects
         */
        public static void sendToLocation(ParticleEffects effect, Location location, float offsetX, float offsetY, float offsetZ, float speed, int count) {
            try {
                Object packet = createPacket(effect, location, offsetX, offsetY, offsetZ, speed, count);
                for (Player player : Bukkit.getOnlinePlayers()) {
                    sendPacket(player, packet);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        private static Object createPacket(ParticleEffects effect, Location location, float offsetX, float offsetY,
                float offsetZ, float speed, int count) throws Exception {
            if (count <= 0) {
                count = 1;
            }
            Class<?> packetClass = getCraftClass("PacketPlayOutWorldParticles");
            Object packet = packetClass.getConstructor(String.class, float.class, float.class, float.class, float.class,
                    float.class, float.class, float.class, int.class).newInstance(effect.name, (float) location.getX(),
                    (float) location.getY(), (float) location.getZ(), offsetX, offsetY, offsetZ, speed, count);
            return packet;
        }

        private static void sendPacket(Player p, Object packet) throws Exception {
            Object eplayer = getHandle(p);
            Field playerConnectionField = eplayer.getClass().getField("playerConnection");
            Object playerConnection = playerConnectionField.get(eplayer);
            for (Method m : playerConnection.getClass().getMethods()) {
                if (m.getName().equalsIgnoreCase("sendPacket")) {
                    m.invoke(playerConnection, packet);
                    return;
                }
            }
        }

        private static Object getHandle(Entity entity) {
            try {
                Method entity_getHandle = entity.getClass().getMethod("getHandle");
                Object nms_entity = entity_getHandle.invoke(entity);
                return nms_entity;
            } catch (Exception ex) {
                ex.printStackTrace();
                return null;
            }
        }

        private static Class<?> getCraftClass(String name) {
            String version = getVersion() + ".";
            String className = "net.minecraft.server." + version + name;
            Class<?> clazz = null;
            try {
                clazz = Class.forName(className);
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
            return clazz;
        }

        private static String getVersion() {
            return Bukkit.getServer().getClass().getPackage().getName().replace(".", ",").split(",")[3];
        }

    }
    
    public static Location parseLocation(World w, String in) {
        String[] params = in.split(",");
        for (String s : params)
        s.replace("0", "0");
        if (params.length == 3 || params.length == 5) {
            double x = Double.parseDouble(params[0]);
            double y = Double.parseDouble(params[1]);
            double z = Double.parseDouble(params[2]);
            Location loc = new Location(w, x, y, z);
            if (params.length == 5) {
                loc.setYaw(Float.parseFloat(params[4]));
                loc.setPitch(Float.parseFloat(params[5]));
            }
            return loc;
        }
        return null;
    }

    public static void sendPlayerToGame(final Player player, Annihilation plugin) {
        final PlayerMeta meta = PlayerMeta.getMeta(player);
        if (meta.getTeam() != null) {
            meta.setAlive(true);
            player.teleport(meta.getTeam().getRandomSpawn());
            
            Bukkit.getScheduler().runTaskLater(plugin, new Runnable() {
                public void run() {
                    meta.getKit().give(player, meta.getTeam());
                    player.setCompassTarget(meta.getTeam().getNexus().getLocation());
                    player.setGameMode(GameMode.ADVENTURE);
                    player.setHealth(player.getMaxHealth());
                    player.setFoodLevel(20);
                    player.setSaturation(20F);
                }
            }, 10L);
        }
    }

    public static boolean isEmptyColumn(Location loc) {
        boolean hasBlock = false;
        Location test = loc.clone();
        for (int y = 0; y < loc.getWorld().getMaxHeight(); y++) {
            test.setY(y);
            if (test.getBlock().getType() != Material.AIR)
                hasBlock = true;
        }
        return !hasBlock;
    }
    
    public static void showClassSelector(Player p, String title) {
        int size = ((Kit.values().length + 8) / 9) * 9;
        Inventory inv = Bukkit.createInventory(p, size, title);
        for (Kit kit : Kit.values()) {
            ItemStack i = kit.getIcon().clone();
            ItemMeta im = i.getItemMeta();
            List<String> lore = im.getLore();
            lore.add(ChatColor.GRAY + "---------------");
            if (kit.isOwnedBy(p)) {
                lore.add(ChatColor.GREEN + "Unlocked");
            } else {
                lore.add(ChatColor.RED + "Locked");
            }
            im.setLore(lore);
            i.setItemMeta(im);
            inv.addItem(i);
        }
        p.openInventory(inv);
    }
    
    public static void spawnFirework(Location loc) {
        Random colour = new Random();
        
        Firework fw = loc.getWorld().spawn(loc, Firework.class);
        FireworkMeta fwMeta = fw.getFireworkMeta();
        
        Type fwType = Type.BALL_LARGE;
        
        int c1i = colour.nextInt(17) + 1;
        int c2i = colour.nextInt(17) + 1;
        
        Color c1 = getFWColor(c1i);
        Color c2 = getFWColor(c2i);
        
        FireworkEffect effect = FireworkEffect.builder().withFade(c2).withColor(c1).with(fwType).build();
        
        fwMeta.addEffect(effect);
        fwMeta.setPower(1);
        fw.setFireworkMeta(fwMeta);
    }
    
    public static void spawnFirework(Location loc, Color c1, Color c2) {
        Firework fw = loc.getWorld().spawn(loc, Firework.class);
        FireworkMeta fwMeta = fw.getFireworkMeta();
        
        Type fwType = Type.BALL_LARGE;
        
       FireworkEffect effect = FireworkEffect.builder().withFade(c2).withColor(c1).with(fwType).build();
        
        fwMeta.addEffect(effect);
        fwMeta.setPower(1);
        fw.setFireworkMeta(fwMeta);
    }
    
    public static Color getFWColor(int c) {
        switch (c) {
        case 1:
            return Color.TEAL;
        default:
        case 2:
            return Color.WHITE;
        case 3:
            return Color.YELLOW;
        case 4:
            return Color.AQUA;
        case 5:
            return Color.BLACK;
        case 6:
            return Color.BLUE;
        case 7:
            return Color.FUCHSIA;
        case 8:
            return Color.GRAY;
        case 9:
            return Color.GREEN;
        case 10:
            return Color.LIME;
        case 11:
            return Color.MAROON;
        case 12:
            return Color.NAVY;
        case 13:
            return Color.OLIVE;
        case 14:
            return Color.ORANGE;
        case 15:
            return Color.PURPLE;
        case 16:
            return Color.RED;
        case 17:
            return Color.SILVER;
        }
    }

    public static String getPhaseColor(int phase) {
        switch (phase) {
        case 1:
            return ChatColor.BLUE.toString();
        case 2:
            return ChatColor.GREEN.toString();
        case 3:
            return ChatColor.YELLOW.toString();
        case 4:
            return ChatColor.GOLD.toString();
        case 5:
            return ChatColor.RED.toString();
        default:
            return ChatColor.WHITE.toString();
        }
    }
    
    public static boolean isTeamTooBig(GameTeam team) {
        int players = team.getPlayers().size();
        for (GameTeam gt : GameTeam.teams())
            if (players >= gt.getPlayers().size() + 3 && gt.getNexus().isAlive())
                return true;
        return false;
    }
}

package me.morpig.NexusGrinder;

import de.kumpelblase2.remoteentities.RemoteEntities;
import de.kumpelblase2.remoteentities.api.DespawnReason;
import de.kumpelblase2.remoteentities.api.RemoteEntity;
import de.kumpelblase2.remoteentities.api.thinking.InteractBehavior;
import de.kumpelblase2.remoteentities.api.thinking.goals.DesireLookAtNearest;
import de.kumpelblase2.remoteentities.api.thinking.goals.DesireLookRandomly;
import de.kumpelblase2.remoteentities.entities.RemotePlayer;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.weather.WeatherChangeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

/**
 * Created by morpig on 1/18/14.
 */
public class NPCBehaviour extends InteractBehavior {

    private static Material defaultItem = Material.DIAMOND_AXE;
    private final Plugin plugin;

    public NPCBehaviour(RemoteEntity inEntity) {
        super(inEntity);

        this.plugin = inEntity.getManager().getPlugin();

        this.onEntityUpdate();
    }

    public void onEntityUpdate()
    {
        if (this.m_entity.getBukkitEntity() == null)
        {
            return;
        }

        this.m_entity.setPushable(false);
        this.m_entity.setStationary(true);

        Player npc = (Player)this.m_entity.getBukkitEntity();
        npc.setCanPickupItems(false);

        // Set tools and armor.
        ItemStack axe = new ItemStack(defaultItem);
        axe.addUnsafeEnchantment(Enchantment.SILK_TOUCH, 10);
        npc.setItemInHand(axe);

        // Armor
        ItemStack[] armor = new ItemStack[4];
        armor[3] = new ItemStack(Material.LEATHER_HELMET);
        armor[2] = new ItemStack(Material.LEATHER_CHESTPLATE);
        armor[1] = new ItemStack(Material.IRON_LEGGINGS);
        armor[0] = new ItemStack(Material.LEATHER_BOOTS);
        // Set it backwards just for our own sanity.

        for (ItemStack itemStack : armor)
            itemStack.addUnsafeEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 10); // Give it that glowing feeling.

        npc.getInventory().setArmorContents(armor); // Set it.
    }

    public void onInteract(Player inPlayer)
    {

    }

    public void onRightClickInteractEventWithPlayer(Player player)
    {
        RemotePlayer behaviorEntity = (RemotePlayer) this.getRemoteEntity();
        Player npc = behaviorEntity.getBukkitEntity();

        ItemStack actionItem = player.getItemInHand();

        if (actionItem.getType() == Material.NAME_TAG) {
            if (actionItem.getItemMeta().hasDisplayName()) {
                String previousName = behaviorEntity.getName();
                behaviorEntity.setName(ChatColor.ITALIC + actionItem.getItemMeta().getDisplayName());
                Bukkit.broadcastMessage(ChatColor.GOLD + previousName + " is now " + behaviorEntity.getName());
                this.onEntityUpdate();

                return;
            }
            else
            {
                player.sendMessage(ChatColor.GOLD + "This entity's ID is \"" + behaviorEntity.getID() + "\".");
                return;
            }
        }

        if (npc.getItemInHand().getType() != defaultItem) {
            player.sendMessage(ChatColor.AQUA + npc.getDisplayName() + ChatColor.RED + " is busy!");
            return;
        } else if (actionItem.getMaxStackSize() != 1) {
            return;
        } else if (actionItem.getType() == Material.POTION) {
            return;
        } else if (actionItem.getDurability() == 0) {
            player.sendMessage(ChatColor.RED + "Item is fully repaired.");
            return;
        }

        // Creative mode users will be using this as a utility.
        if (player.getGameMode() == GameMode.CREATIVE) {
            actionItem.setDurability((short)0);
            return; // Repair instantly and call it a day.
        }

        // Record where the player had the item for their convenience.
        int indexOfPreviouslyHeldItem = player.getInventory().getHeldItemSlot();

        // Take the item from the player and put it in the NPC's hand.
        player.setItemInHand(new ItemStack(Material.AIR));
        npc.setItemInHand(actionItem);

        // Capture the NPC's location and grab the closest anvil.
        Location npcLocation = npc.getLocation();
        Location destination = closestLocation(npcLocation, Material.ANVIL);

        // Disable the NPC gazing around.
        DesireLookRandomly desireLookRandomly = behaviorEntity.getMind().getMovementDesire(DesireLookRandomly.class);
        desireLookRandomly.stopExecuting();

        DesireLookAtNearest desireLookAtNearest = behaviorEntity.getMind().getMovementDesire(DesireLookAtNearest.class);

        behaviorEntity.move(destination);
        behaviorEntity.lookAt(destination);

        double distanceAway = npcLocation.distance(destination);

        class FinishRepairTrip implements Runnable {
            private final Player npc;
            private final Location gaze;

            public FinishRepairTrip(Player npc, Location gaze) {
                this.npc = npc;
                this.gaze = gaze;
            }

            public void run() {

                if (!this.gaze.equals(this.npc.getLocation())) // Pretend he's working at the anvil.
                    npc.setSneaking(true);

                Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
                    public void run() {
                        npc.setSneaking(false); // Return to standing.
                        npc.getWorld().playSound(npc.getLocation(), Sound.ANVIL_USE, 1F, 1F); // Simulate a repair. :P
                    }
                }, 20L);
            }
        }

        Long wait = Math.round((distanceAway / 4) * 20); // Calculates the time needed to wait for the next operation (this is all async, so the delay isn't pre-factored.
        Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new FinishRepairTrip(npc, destination), wait);

        // Actually repair the item.
        actionItem.setDurability((short) 0);

        class ReturnNPC implements Runnable {
            private final Location returnLocation;
            private final RemotePlayer entity;
            private final Player toLookAt;

            public ReturnNPC(Location returnLocation, RemotePlayer entity, Player player) {
                this.returnLocation = returnLocation;
                this.entity = entity;
                this.toLookAt = player;
            }

            public void run() {
                // Walk the NPC back to its original location.
                this.entity.move(this.returnLocation);

                // Turn back to the player to correspond with te walking.
                entity.lookAt(this.toLookAt);
            }
        }

        Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new ReturnNPC(npcLocation, behaviorEntity, player), wait + 20L);

        // Ran once the NPC is actually back near the player.
        class ReturnItem implements Runnable {
            private final Player player;
            private final ItemStack item;
            private final Player npc;
            private final int attemptSlot;

            public ReturnItem(Player player, ItemStack item, Player npc, int attemptSlot) {
                this.player = player;
                this.item = item;
                this.npc = npc;
                this.attemptSlot = attemptSlot;
            }

            public void run() {
                if (this.player.getInventory().getItem(this.attemptSlot) == null && this.player.isOnline()) {
                    // Called if the player still has a free slot where the item originated from.
                    this.player.getInventory().setItem(this.attemptSlot, this.item);
                } else if (this.player.getInventory().firstEmpty() != -1 && this.player.isOnline()) {
                    // Called if the player has a free space, but it's not the origin slot.
                    this.player.getInventory().setItem(this.player.getInventory().firstEmpty(), this.item);
                } else {
                    // Called only if the player isn't online or has a full inventory. Avoid if possible to prevent item destruction.
                    npc.getLocation().getWorld().dropItem(npc.getLocation(), this.item);
                }

                // Display anvil-names, if it has one.
                if (item.getItemMeta().hasDisplayName()) {
                    this.player.sendMessage(ChatColor.AQUA + this.npc.getDisplayName() + ChatColor.GREEN + " repaired " + ChatColor.YELLOW + item.getItemMeta().getDisplayName() + ChatColor.GREEN + ".");
                } else {
                    this.player.sendMessage(ChatColor.AQUA + this.npc.getDisplayName() + ChatColor.GREEN + " repaired your " + ChatColor.YELLOW + item.getType().name().replace("_", " ").toLowerCase() + ChatColor.GREEN + ".");
                }

                // Put the blacksmith's default item back in his hand.
                ItemStack axe = new ItemStack(defaultItem);
                axe.addEnchantment(Enchantment.SILK_TOUCH, 1);
                npc.setItemInHand(axe);

                // Grab the RemoteEntity to perform logic operations.
                RemoteEntity entity = RemoteEntities.getRemoteEntityFromEntity(this.npc);

                // Restore normal livelihood into the blacksmith.
                DesireLookAtNearest desireLookAtNearest = entity.getMind().getMovementDesire(DesireLookAtNearest.class);
                DesireLookRandomly desireLookRandomly = entity.getMind().getMovementDesire(DesireLookRandomly.class);

//                desireLookAtNearest.setLookPossibility(1.0F);
                desireLookRandomly.startExecuting();
            }
        }

        Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new ReturnItem(player, actionItem, npc, indexOfPreviouslyHeldItem), (wait * 2) + 20L);
    }


    // Grab the nearest location of a block with a certain material/type.
    private Location closestLocation(Location origin, Material type) {
        World world = origin.getWorld();

        for (int x = origin.getBlockX() - 4; x <= origin.getBlockX() + 8; x = x + 1) {
            for (int z = origin.getBlockZ() - 4; z <= origin.getBlockZ() + 8; z = z + 1) {
                for (int y = origin.getBlockY() - 4; y <= origin.getBlockY() + 8; y = y + 1) {
                    Block block = world.getBlockAt(x, y, z);
                    if (block.getType() == type)
                        return block.getLocation();
                }
            }
        }

        return origin;
    }
}

package me.morpig.NexusGrinder;

import de.kumpelblase2.remoteentities.RemoteEntities;
import de.kumpelblase2.remoteentities.api.DespawnReason;
import de.kumpelblase2.remoteentities.api.RemoteEntity;
import de.kumpelblase2.remoteentities.api.RemoteEntityType;
import de.kumpelblase2.remoteentities.api.thinking.InteractBehavior;
import de.kumpelblase2.remoteentities.api.thinking.goals.DesireLookAtNearest;
import de.kumpelblase2.remoteentities.api.thinking.goals.DesireLookRandomly;
import de.kumpelblase2.remoteentities.entities.RemotePlayer;
import de.kumpelblase2.remoteentities.entities.RemoteSheep;
import me.morpig.NexusGrinder.object.GameTeam;
import me.morpig.NexusGrinder.object.PlayerMeta;
import net.minecraft.server.v1_7_R1.EntityPlayer;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Sheep;
import org.bukkit.event.weather.WeatherChangeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.Wool;
import org.bukkit.plugin.Plugin;

public class NPCBehaviour extends InteractBehavior {
    private static Material defaultItem = Material.DIAMOND_AXE;
    private final Plugin plugin;
    private static GameTeam team;
    private static Player player;

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


        for (GameTeam t : GameTeam.teams()) {
            int size = 0;

            for (Player p : Bukkit.getOnlinePlayers()) {
                PlayerMeta meta = PlayerMeta.getMeta(p);
                if (meta.getTeam() == t)
                    size++;
            }

            if (size != 1) {

                this.m_entity.setName(ChatColor.GOLD + "" + size + "Players | " + ChatColor.GOLD + "Join Blue!");
            } else {
                this.m_entity.setName(ChatColor.GOLD + "" + size + "Player | " + ChatColor.GOLD + "Join Blue!");
            }
        }



        Sheep npc = (Sheep)this.m_entity.getBukkitEntity();


    }

    public void onInteract(Player inPlayer)
    {

    }

    public void onRightClickInteractEventWithPlayer(Player player)
    {
        PlayerMeta meta = PlayerMeta.getMeta(player);

        RemoteSheep behaviorEntity = (RemoteSheep) this.getRemoteEntity();
        LivingEntity npc = behaviorEntity.getBukkitEntity();

        String name = behaviorEntity.getName();
        if (name == "Join Team Blue") {

            GameTeam target;
            target = GameTeam.BLUE;



            player.sendMessage(ChatColor.DARK_AQUA + "You joined "
                    + target.coloredName());
            meta.setTeam(target);






        }
    }
    private void listTeams(CommandSender sender) {
        sender.sendMessage(ChatColor.GRAY + "============[ "
                + ChatColor.DARK_AQUA + "Teams" + ChatColor.GRAY
                + " ]============");
        for (GameTeam t : GameTeam.teams()) {
            int size = 0;

            for (Player p : Bukkit.getOnlinePlayers()) {
                PlayerMeta meta = PlayerMeta.getMeta(p);
                if (meta.getTeam() == t)
                    size++;
            }

            if (size != 1) {
                sender.sendMessage(t.coloredName() + " - " + size + " players");
            } else {
                sender.sendMessage(t.coloredName() + " - " + size + " player");
            }
        }
        sender.sendMessage(ChatColor.GRAY + "===============================");
    }
}
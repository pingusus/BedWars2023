package com.tomkeuper.bedwars.listeners;

import com.tomkeuper.bedwars.api.arena.IArena;
import com.tomkeuper.bedwars.arena.Arena;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;

public class BuildLimiter implements Listener {

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        IArena arena = Arena.getArenaByPlayer(player);

        // Ignore if player is not in an arena or is a spectator
        if (arena == null) return;
        if (!arena.isPlayer(player)) return;

        Location blockLoc = event.getBlock().getLocation();
        Location center = arena.getConfig().getArenaLoc("waiting.Loc");

        if (center == null) return;

        // We recycle the existing 'worldBorder' value to act as our build limit diameter
        int size = arena.getConfig().getYml().getInt("worldBorder");
        double radius = size / 2.0;

        double dx = Math.abs(blockLoc.getX() - center.getX());
        double dz = Math.abs(blockLoc.getZ() - center.getZ());

        // Check if the block is placed outside the radius bounds
        if (dx > radius || dz > radius) {
            event.setCancelled(true);
            player.sendMessage(ChatColor.RED + "You cannot build outside the arena boundary!");
        }
    }
}

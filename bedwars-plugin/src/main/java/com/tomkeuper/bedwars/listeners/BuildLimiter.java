package com.tomkeuper.bedwars.listeners;

import com.tomkeuper.bedwars.api.arena.IArena;
import com.tomkeuper.bedwars.arena.Arena;
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

        if (arena == null) return;
        if (!arena.isPlayer(player)) return;

        Location blockLoc = event.getBlock().getLocation();
        Location center = arena.getConfig().getArenaLoc("waiting.Loc");

        if (center == null) return;

        int size = arena.getConfig().getYml().getInt("worldBorder");
        double radius = size / 2.0;

        double dx = Math.abs(blockLoc.getX() - center.getX());
        double dz = Math.abs(blockLoc.getZ() - center.getZ());

        if (dx > radius || dz > radius) {
            event.setCancelled(true);
        }
    }
}

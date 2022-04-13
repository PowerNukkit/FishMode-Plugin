/*
 * FishMode Plugin for PowerNukkit
 * Copyright (C) 2022  José Roberto de Araújo Júnior <joserobjr@powernukkit.org>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package org.powernukkit.plugins.fishmode;

import cn.nukkit.Player;
import cn.nukkit.entity.EntityLiving;
import cn.nukkit.entity.data.ShortEntityData;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.EventPriority;
import cn.nukkit.event.Listener;
import cn.nukkit.event.player.*;
import cn.nukkit.event.server.DataPacketReceiveEvent;
import cn.nukkit.network.protocol.DataPacket;
import cn.nukkit.network.protocol.SetEntityDataPacket;

import javax.annotation.Nonnull;

public class FishModeEventHandler implements Listener {
    @Nonnull
    private final FishModePlugin plugin;

    @Nonnull
    private final BreathingHandler breathingHandler;
    public FishModeEventHandler(@Nonnull FishModePlugin plugin) {
        this.plugin = plugin;
        this.breathingHandler = plugin.getBreathingHandler();
    }

    private void updateFishStatus(Player player) {
        if (player.isOnline()) {
            breathingHandler.updateFishStatus(player);
        }
    }

    private void updateFishStatus(PlayerEvent event) {
        updateFishStatus(event.getPlayer());
    }

    private void updateFishStatusNextTick(PlayerEvent event) {
        plugin.getServer().getScheduler().scheduleDelayedTask(plugin, ()-> updateFishStatus(event), 1);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(@Nonnull PlayerJoinEvent event) {
        updateFishStatusNextTick(event);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerTeleport(@Nonnull PlayerTeleportEvent event) {
        if (event.getFrom().getLevel() != event.getTo().getLevel()) {
            updateFishStatusNextTick(event);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerRespawn(@Nonnull PlayerRespawnEvent event) {
        if (event.getRespawnPosition().getLevel() != event.getPlayer().getLevel()) {
            updateFishStatusNextTick(event);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(@Nonnull PlayerQuitEvent event) {
        breathingHandler.release(event.getPlayer().getUniqueId());
    }

    @EventHandler
    private void onPacket(@Nonnull DataPacketReceiveEvent event) {
        DataPacket dataPacket = event.getPacket();
        if (!(dataPacket instanceof SetEntityDataPacket)) {
            return;
        }
        SetEntityDataPacket packet = (SetEntityDataPacket) dataPacket;
        Player player = event.getPlayer();
        if (packet.eid != player.getId()) {
            return;
        }

        if (!breathingHandler.isFish(player)) {
            return;
        }
        packet.metadata.put(new ShortEntityData(EntityLiving.DATA_AIR, breathingHandler.getAirTicks(player)));
    }
}

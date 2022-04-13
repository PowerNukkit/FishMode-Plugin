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

package org.powernukkit.plugins.fishmode.cmd;

import cn.nukkit.IPlayer;
import cn.nukkit.OfflinePlayer;
import cn.nukkit.Server;
import cn.nukkit.command.Command;
import cn.nukkit.command.CommandExecutor;
import cn.nukkit.command.CommandSender;
import cn.nukkit.plugin.PluginLogger;
import cn.nukkit.utils.Config;
import org.powernukkit.plugins.fishmode.BreathingHandler;
import org.powernukkit.plugins.fishmode.FishModePlugin;

import javax.annotation.Nonnull;

public abstract class FishCommandBase implements CommandExecutor {
    @Nonnull
    protected final FishModePlugin plugin;

    public FishCommandBase(@Nonnull FishModePlugin plugin) {
        this.plugin = plugin;
    }

    @Nonnull
    protected Server getServer() {
        return plugin.getServer();
    }

    @Nonnull
    protected PluginLogger getLogger() {
        return plugin.getLogger();
    }

    @Nonnull
    protected BreathingHandler getBreathingHandler() {
        return plugin.getBreathingHandler();
    }

    @Nonnull
    protected Config getConfig() {
        return plugin.getConfig();
    }

    protected IPlayer findPlayer(String playerName) {
        IPlayer player = getServer().getPlayer(playerName);
        if (player == null) {
            return getServer().lookupName(playerName)
                    .map(uuid -> new OfflinePlayer(getServer(), uuid, playerName))
                    .orElse(null);
        }
        return player;
    }

    @Override
    public abstract boolean onCommand(@Nonnull CommandSender sender, @Nonnull Command command, @Nonnull String label, @Nonnull String[] args);
}

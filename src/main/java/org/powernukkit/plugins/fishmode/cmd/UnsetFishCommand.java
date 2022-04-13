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
import cn.nukkit.command.Command;
import cn.nukkit.command.CommandSender;
import cn.nukkit.utils.TextFormat;
import org.powernukkit.plugins.fishmode.FishModePlugin;

import javax.annotation.Nonnull;
import java.util.Arrays;

public class UnsetFishCommand extends FishCommandBase{
    public UnsetFishCommand(@Nonnull FishModePlugin plugin) {
        super(plugin);
    }

    @Override
    public boolean onCommand(@Nonnull CommandSender sender, @Nonnull Command command, @Nonnull String label, @Nonnull String[] args) {
        String playerName = FishModePlugin.normalizePlayerName(Arrays.stream(args));
        if (playerName.isEmpty()) {
            return false;
        }
        IPlayer player = findPlayer(playerName);
        if (player == null) {
            sender.sendMessage(TextFormat.RED + "Player not found: " + playerName);
            return true;
        }

        try {
            getBreathingHandler().unsetFishFlag(player);
            String msg = "The direct fish flag was removed from \"" + player.getName() + "\"";
            getLogger().info(msg + " by \"" + sender.getName() + "\"");
            sender.sendMessage(TextFormat.GREEN + msg);
        } catch (Exception e) {
            getLogger().info("Error when removing the fish flag from \"" + player.getName() + "\" by \"" + sender.getName() + "\"");
            sender.sendMessage(TextFormat.RED + "Failed: " + e.getClass().getSimpleName() + ": " + e.getCause());
        }
        return true;
    }
}

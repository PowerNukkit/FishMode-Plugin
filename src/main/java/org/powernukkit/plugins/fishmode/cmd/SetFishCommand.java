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
import cn.nukkit.utils.OptionalBoolean;
import cn.nukkit.utils.TextFormat;
import org.powernukkit.plugins.fishmode.FishModePlugin;

import javax.annotation.Nonnull;
import java.util.Arrays;

public class SetFishCommand extends FishCommandBase {
    public SetFishCommand(@Nonnull FishModePlugin plugin) {
        super(plugin);
    }

    @Override
    public boolean onCommand(@Nonnull CommandSender sender, @Nonnull Command command, @Nonnull String label, @Nonnull String[] args) {
        if (args.length < 2) {
            return false;
        }
        OptionalBoolean isFishOpt = FishModePlugin.parseBooleanStrict(args[0]);
        if (!isFishOpt.isPresent()) {
            return false;
        }
        boolean isFish = isFishOpt.getAsBoolean();

        String playerName = FishModePlugin.normalizePlayerName(Arrays.stream(args).skip(1));
        if (playerName.isEmpty()) {
            return false;
        }
        IPlayer player = findPlayer(playerName);
        if (player == null) {
            sender.sendMessage(TextFormat.RED + "Player not found: " + playerName);
            return true;
        }

        try {
            getBreathingHandler().setFishFlag(player, isFish);
            String set = isFish? "set" : "unset";
            getLogger().info("\"" + player.getName() + "\" was " + set + " as fish by \"" + sender.getName() + "\"");
            if (isFish) {
                sender.sendMessage(TextFormat.BLUE + "\"" + player.getName() + "\" is now a fish");
            } else {
                sender.sendMessage(TextFormat.GREEN + "\"" + player.getName() + "\" is no longer a fish");
            }
        } catch (Exception e) {
            String setting = isFish? "setting" : "unsetting";
            getLogger().info("Error when "+setting+" \"" + player.getName() + "\" as fish by \"" + sender.getName() + "\"");
            sender.sendMessage(TextFormat.RED + "Failed: " + e.getClass().getSimpleName() + ": " + e.getCause());
        }
        return true;
    }
}

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
import cn.nukkit.Player;
import cn.nukkit.command.Command;
import cn.nukkit.command.CommandSender;
import cn.nukkit.level.Level;
import cn.nukkit.utils.OptionalBoolean;
import cn.nukkit.utils.TextFormat;
import org.powernukkit.plugins.fishmode.BreathingHandler;
import org.powernukkit.plugins.fishmode.FishModePermissions;
import org.powernukkit.plugins.fishmode.FishModePlugin;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.Optional;

public class IsFishCommand extends FishCommandBase {
    public IsFishCommand(@Nonnull FishModePlugin plugin) {
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

        BreathingHandler breathingHandler = getBreathingHandler();
        boolean isFish = breathingHandler.isFish(player);
        OptionalBoolean flag = breathingHandler.getFishFlag(player);
        Optional<Player> onlinePlayer = Optional.ofNullable(player.getPlayer());
        Optional<Level> level = onlinePlayer.map(Player::getLevel);
        OptionalBoolean fishWorld = level.map(l-> OptionalBoolean.of(
                        breathingHandler.isFishModeActiveIn(l))
                ).orElse(OptionalBoolean.EMPTY);

        StringBuilder msg = new StringBuilder();
        if (isFish) {
            msg.append(TextFormat.BLUE).append('"').append(playerName).append("\" is a fish");
        } else {
            msg.append(TextFormat.GREEN).append('"').append(playerName).append("\" is not a fish");
        }
        msg.append(TextFormat.RESET).append(" (Flag: ");
        switch (flag) {
            case TRUE:
                msg.append(TextFormat.GREEN).append("true");
                break;
            case FALSE:
                msg.append(TextFormat.RED).append("false");
                break;
            default:
            case EMPTY:
                msg.append(TextFormat.GRAY).append("empty");
                break;
        }
        if (onlinePlayer.isPresent()) {
            Player p = onlinePlayer.get();
            if (FishModePermissions.NEVER_FISH.hasPermission(p)) {
                msg.append(TextFormat.RESET).append(", ").append(TextFormat.RED).append("Never Fish by Permission");
            }
            if (FishModePermissions.ALWAYS_FISH.hasPermission(p)) {
                msg.append(TextFormat.RESET).append(", ").append(TextFormat.BLUE).append("Always Fish by Permission");
            }
        }

        msg.append(TextFormat.RESET).append(')');
        if (level.isPresent()) {
            msg.append(", Level: \"").append(level.get().getName()).append("\" (");
            if (fishWorld == OptionalBoolean.TRUE) {
                msg.append(TextFormat.BLUE).append("It's a Fish Mode World");
                if (breathingHandler.isAllPlayersInFishWorldsFish()) {
                    msg.append(", all-players-are-fish is active");
                }
            } else {
                msg.append(TextFormat.GREEN).append("It's not a Fish Mode World");
            }
            msg.append(TextFormat.RESET).append(')');
        } else {
            msg.append('.').append(TextFormat.GRAY).append(" The player is offline");
        }
        sender.sendMessage(msg.toString());
        return true;
    }
}

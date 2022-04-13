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

import cn.nukkit.api.UsedByReflection;
import cn.nukkit.command.PluginCommand;
import cn.nukkit.plugin.PluginBase;
import cn.nukkit.utils.OptionalBoolean;
import org.powernukkit.plugins.fishmode.cmd.FishModeReloadCommand;
import org.powernukkit.plugins.fishmode.cmd.IsFishCommand;
import org.powernukkit.plugins.fishmode.cmd.SetFishCommand;
import org.powernukkit.plugins.fishmode.cmd.UnsetFishCommand;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Locale;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@UsedByReflection
public class FishModePlugin extends PluginBase {
    private BreathingHandler breathingHandler;

    @Override
    public void onEnable() {
        logLicense();
        saveDefaultConfig();
        reloadConfig();
        breathingHandler = new BreathingHandler(this);
        getServer().getPluginManager().registerEvents(new FishModeEventHandler(this), this);
        registerCommands();
    }

    @SuppressWarnings("ConstantConditions")
    private void registerCommands() {
        ((PluginCommand<?>) getCommand("fishmode-reload")).setExecutor(new FishModeReloadCommand(this));
        ((PluginCommand<?>) getCommand("setfish")).setExecutor(new SetFishCommand(this));
        ((PluginCommand<?>) getCommand("unsetfish")).setExecutor(new UnsetFishCommand(this));
        ((PluginCommand<?>) getCommand("isfish")).setExecutor(new IsFishCommand(this));
    }

    private void logLicense() {
        getLogger().info("FishMod Plugin  Copyright (C) 2022  José Roberto de Araújo Júnior <joserobjr@powernukkit.org>");
        getLogger().info("This program comes with ABSOLUTELY NO WARRANT.");
        getLogger().info("This is free software, and you are welcome to redistribute it");
        getLogger().info("under certain conditions; for details, visit https://www.gnu.org/licenses/agpl-3.0.html");
    }

    @Nonnull
    public BreathingHandler getBreathingHandler() {
        return breathingHandler;
    }

    @Nonnull
    public static OptionalBoolean parseBooleanStrict(@Nullable String str) {
        if (str == null) {
            return OptionalBoolean.EMPTY;
        }
        String lowered = str.toLowerCase(Locale.ROOT);
        if ("true".equals(lowered)) {
            return OptionalBoolean.TRUE;
        } else if ("false".equals(lowered)) {
            return OptionalBoolean.FALSE;
        } else {
            return OptionalBoolean.EMPTY;
        }
    }

    @Nonnull
    public static String normalizePlayerName(@Nonnull Stream<String> argStream) {
        String playerName = argStream.collect(Collectors.joining(" ", "", ""));
        if (playerName.length() > 1 && playerName.charAt(0) == '"' && playerName.charAt(playerName.length() - 1) == '"') {
            return playerName.substring(1, playerName.length() - 1);
        }
        return playerName;
    }
}

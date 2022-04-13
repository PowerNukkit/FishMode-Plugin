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

import cn.nukkit.IPlayer;
import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.event.entity.EntityDamageEvent;
import cn.nukkit.level.Level;
import cn.nukkit.math.NukkitMath;
import cn.nukkit.plugin.PluginLogger;
import cn.nukkit.potion.Effect;
import cn.nukkit.scheduler.TaskHandler;
import cn.nukkit.utils.Config;
import cn.nukkit.utils.OptionalBoolean;
import com.google.common.io.Files;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import java.util.stream.Collectors;

public class BreathingHandler {
    @Nonnull
    private final FishModePlugin plugin;

    @Nonnull
    private final Server server;

    @Nullable
    private TaskHandler tickTask;

    @Nonnull
    private final  Map<UUID, Boolean> fishes = new WeakHashMap<>();

    @Nonnull
    private final Map<UUID, OptionalBoolean> fishFlags = new WeakHashMap<>();

    @Nonnull
    private final Map<UUID, Integer> fishAirTicks = new WeakHashMap<>();

    @Nonnull
    private final Map<Level, Boolean> activeWorlds = new WeakHashMap<>();

    @Nonnull
    private final File fishesFolder;

    @Nonnull
    private Pattern[] worldPatterns = new Pattern[0];

    @Nonnull
    private Set<String> worlds = Collections.emptySet();

    private boolean fishByDefault;
    private boolean affectOnlyFW;
    private boolean allPlayersInFWareFish;
    private boolean notNether;
    private boolean allWorlds;

    public BreathingHandler(@Nonnull FishModePlugin plugin) {
        this.plugin = plugin;
        this.server = plugin.getServer();
        fishesFolder = new File(plugin.getDataFolder(), "fishes");
        //noinspection ResultOfMethodCallIgnored
        fishesFolder.mkdirs();
        reload();
    }

    protected int getAirTicks(Player player) {
        return fishAirTicks.computeIfAbsent(player.getUniqueId(), uuid -> player.getAirTicks());
    }

    private void setAirTicks(Player player, int airTicks) {
        fishAirTicks.put(player.getUniqueId(), airTicks);
        player.setAirTicks(airTicks);
    }

    private void tickPlayer(Player player, int tickDiff) {
        Effect effect = player.getEffect(Effect.WATER_BREATHING);
        if (effect == null || effect.getDuration() < 11*20) {
            player.addEffect(Effect.getEffect(Effect.WATER_BREATHING).setDuration(15*20).setAmbient(true).setVisible(false));
        }
        if (!player.isInsideOfWater()) {
            int airTicks = getAirTicks(player);
            if (airTicks == -20) {
                airTicks = 400 - tickDiff;
            } else {
                airTicks -= tickDiff;
            }
            if (airTicks == -20) {
                airTicks--;
            }

            if (airTicks <= -41) {
                airTicks = -21;
                player.attack(new EntityDamageEvent(player, EntityDamageEvent.DamageCause.DROWNING, 2));
                boolean isSwimming = player.isSwimming();
                if (!isSwimming){
                    player.setSwimming(true);
                    if (!player.isInsideOfWater()) {
                        player.setSwimming(false);
                    }
                }
            }

            setAirTicks(player, airTicks);
        } else {
            int airTicks = getAirTicks(player);

            if (airTicks == -20 || airTicks >= 400) {
                setAirTicks(player, -20);
            } else {
                setAirTicks(player, Math.min(400, airTicks + tickDiff * 5));
            }
        }
    }

    protected void onTick(int tickDiff) {
        server.getOnlinePlayers().values().forEach(player -> {
            if (isFishModeActiveTo(player)) {
                tickPlayer(player, tickDiff);
            }
        });
    }

    public void updateFishStatus(@Nonnull Player player) {
        boolean isFish = isFishModeActiveTo(player);
        fishes.put(player.getUniqueId(), isFish);
        if (isFish) {
            player.addEffect(Effect.getEffect(Effect.WATER_BREATHING).setDuration(10*20).setAmbient(true).setVisible(false));
        } else {
            fishAirTicks.remove(player.getUniqueId());
        }
    }

    private boolean computePlayerFishMode(@Nonnull Player player, boolean fallback) {
        OptionalBoolean fishFlag = getFishFlag(player);
        if (fishFlag.isPresent()) {
            return fishFlag.getAsBoolean();
        }
        if (FishModePermissions.NEVER_FISH.hasPermission(player)) {
            return false;
        }
        if (FishModePermissions.ALWAYS_FISH.hasPermission(player)) {
            return true;
        }
        return fallback;
    }

    private boolean isFishModeActiveTo(@Nonnull Player player) {
        if (!affectOnlyFW) {
            return computePlayerFishMode(player, fishByDefault);
        }

        if (!isFishModeActiveIn(player.getLevel())) {
            return false;
        }

        if (allPlayersInFWareFish) {
            return computePlayerFishMode(player, true);
        }

        return computePlayerFishMode(player, fishByDefault);
    }

    public boolean isFishModeActiveIn(@Nonnull Level world) {
        return activeWorlds.computeIfAbsent(world, level-> {
            if (notNether && level.getDimension() == Level.DIMENSION_NETHER) {
                return false;
            }
            if (allWorlds) {
                return true;
            }
            String name = level.getName();
            if (worlds.contains(name)) {
                return true;
            }
            for (Pattern worldPattern : worldPatterns) {
                if (worldPattern.matcher(name).matches()) {
                    return true;
                }
            }
            return false;
        });
    }

    @Nonnull
    private File boolFile(@Nonnull UUID uuid) {
        return new File(fishesFolder, uuid + ".bool");
    }

    @Nonnull
    public OptionalBoolean getFishFlag(@Nonnull IPlayer player) {
        return fishFlags.computeIfAbsent(player.getUniqueId(), uuid -> {
            File boolFile = boolFile(uuid);
            if (!boolFile.isFile()) {
                return OptionalBoolean.EMPTY;
            }
            try {
                return FishModePlugin.parseBooleanStrict(
                        Objects.requireNonNull(Files.asCharSource(boolFile, StandardCharsets.UTF_8).readFirstLine())
                                .trim().toLowerCase(Locale.ROOT)
                );
            } catch (IOException|NullPointerException e) {
                getLogger().error("Failed to load the boolean status for \""+player.getName()+"\" -- " + player.getUniqueId(), e);
                return OptionalBoolean.EMPTY;
            }
        });
    }

    public void reload() {
        Config config = plugin.getConfig();
        affectOnlyFW = config.getBoolean("fish-player.only-in-fish-worlds", true);
        fishByDefault = config.getBoolean("fish-player.players-are-fish-by-default", false);
        allPlayersInFWareFish = config.getBoolean("fish-worlds.all-players-are-fish", true);
        allWorlds = config.getBoolean("fish-worlds.all", false);
        notNether = config.getBoolean("fish-worlds.not-nether", false);
        worlds = config.getStringList("fish-worlds.by-name").stream().map(name-> name.toLowerCase(Locale.ROOT)).collect(Collectors.toSet());
        worldPatterns = config.getStringList("fish-worlds.regex").stream()
                        .map(regex -> {
                            try {
                                return Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
                            } catch (PatternSyntaxException e) {
                                getLogger().warning("Bad regex for fish-worlds.regex: " + regex, e);
                                return null;
                            }
                        }).filter(Objects::nonNull)
                        .toArray(Pattern[]::new);
        fishes.clear();
        fishFlags.clear();
        activeWorlds.clear();
        server.getOnlinePlayers().values().forEach(this::updateFishStatus);
        getLogger().info("The fish status for all players were reloaded");
        int tickRate = NukkitMath.clamp(config.getInt("tick-rate", 1), 1, (5*20) - 1);
        if (tickTask != null) {
            tickTask.cancel();
        }
        tickTask = server.getScheduler().scheduleRepeatingTask(plugin, ()-> onTick(tickRate), tickRate);
    }

    public boolean isFish(@Nonnull IPlayer player) {
        return OptionalBoolean.ofNullable(fishes.get(player.getUniqueId()))
                .orElseGet(()-> getFishFlag(player).orElse(fishByDefault));
    }

    public void setFishFlag(@Nonnull IPlayer player, boolean fish) throws IOException {
        Files.asCharSink(boolFile(player.getUniqueId()), StandardCharsets.UTF_8).write(Boolean.toString(fish));
        Player onlinePlayer = player.getPlayer();
        if (onlinePlayer != null) {
            fishFlags.put(player.getUniqueId(), OptionalBoolean.of(fish));
            updateFishStatus(onlinePlayer);
        } else {
            fishFlags.remove(player.getUniqueId());
        }
    }

    public boolean unsetFishFlag(@Nonnull IPlayer player) throws IOException {
        File boolFile = boolFile(player.getUniqueId());
        if (!boolFile.isFile()) {
            return false;
        }
        if (!boolFile.delete()) {
            throw new IOException("Could not remove the file " + boolFile);
        }
        fishFlags.remove(player.getUniqueId());
        Player onlinePlayer = player.getPlayer();
        if (onlinePlayer != null) {
            updateFishStatus(onlinePlayer);
        }
        return true;
    }

    @Nonnull
    private PluginLogger getLogger() {
        return plugin.getLogger();
    }

    public void release(@Nonnull UUID playerUuid) {
        fishes.remove(playerUuid);
        fishFlags.remove(playerUuid);
        fishAirTicks.remove(playerUuid);
    }

    public boolean isAllPlayersInFishWorldsFish() {
        return allPlayersInFWareFish;
    }
}

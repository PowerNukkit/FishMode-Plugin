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

import cn.nukkit.permission.Permissible;

import javax.annotation.Nonnull;

public enum FishModePermissions {
    NEVER_FISH("fishmode.fish.never"),
    ALWAYS_FISH("fishmode.fish.always"),
    SETFISH("fishmode.cmd.setfish"),
    UNSETFISH("fishmode.cmd.unsetfish"),
    RELOAD_FISHMODE("fishmode.cmd.reload-fishmode"),
    ISFISH("fishmode.cmd.isfish");

    @Nonnull
    private final String permission;

    FishModePermissions(@Nonnull String permission) {
        this.permission = permission;
    }

    @Nonnull
    public String getPermission() {
        return permission;
    }

    public boolean hasPermission(Permissible permissible) {
        return permissible.hasPermission(getPermission());
    }
}

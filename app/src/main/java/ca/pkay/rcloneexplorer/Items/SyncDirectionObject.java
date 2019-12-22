package ca.pkay.rcloneexplorer.Items;

import android.content.Context;

import ca.pkay.rcloneexplorer.R;

/**
 * Copyright (C) 2019  Felix Nüsse
 * Created on 22.12.19 - 14:46
 * <p>
 * Edited by: Felix Nüsse felix.nuesse(at)t-online.de
 * <p>
 * rcloneExplorer
 * <p>
 * This program is released under the GPLv3 license
 * <p>
 * <p>
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301  USA
 */
public class SyncDirectionObject {

    public static final int SYNC_LOCAL_TO_REMOTE = 1;
    public static final int SYNC_REMOTE_TO_LOCAL = 2;
    public static final int COPY_LOCAL_TO_REMOTE = 3;
    public static final int COPY_REMOTE_TO_LOCAL = 4;

    public static String[] getOptionsArray(Context context) {
        return new String[] {
                context.getResources().getString(R.string.sync_direction_local_remote),
                context.getString(R.string.sync_direction_remote_local),
                context.getString(R.string.sync_direction_copy_local_remote),
                context.getString(R.string.sync_direction_copy_remote_local)
        };
    }
}

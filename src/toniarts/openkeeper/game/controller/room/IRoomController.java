/*
 * Copyright (C) 2014-2017 OpenKeeper
 *
 * OpenKeeper is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * OpenKeeper is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenKeeper.  If not, see <http://www.gnu.org/licenses/>.
 */
package toniarts.openkeeper.game.controller.room;

import com.simsilica.es.EntityId;
import toniarts.openkeeper.common.RoomInstance;
import toniarts.openkeeper.game.controller.room.storage.IRoomObjectControl;
import toniarts.openkeeper.tools.convert.map.Room;

import java.awt.*;
import java.util.Set;

/**
 * Controls rooms and provides services related to rooms
 *
 * @author Toni Helenius <helenius.toni@gmail.com>
 */
public interface IRoomController {

    /**
     * Constructs a room
     */
    void construct();

    /**
     * Checks if the given tile is accessible, from an adjacent tile. If no from
     * tile is given, checks general accessibility
     *
     * @param from from tile, can be {@code null}
     * @param to   the target tile
     * @return true is the tile is accessible
     */
    boolean isTileAccessible(Point from, Point to);

    /**
     * Get the actual room instance representation of the room
     *
     * @return the room instance
     */
    RoomInstance getRoomInstance();

    /**
     * Get the number of floor furniture in a room
     *
     * @return floor furniture count
     */
    int getFloorFurnitureCount();

    /**
     * Get the number of wall furniture in a room
     *
     * @return wall furniture count
     */
    int getWallFurnitureCount();

    /**
     * Get the floor furniture IDs
     *
     * @return floor furniture
     */
    Set<EntityId> getFloorFurniture();

    /**
     * Get the wall furniture IDs
     *
     * @return wall furniture
     */
    Set<EntityId> getWallFurniture();

    boolean canStoreGold();

    boolean hasObjectControl(AbstractRoomController.ObjectType objectType);

    <T extends IRoomObjectControl> T getObjectControl(AbstractRoomController.ObjectType objectType);

    Room getRoom();

    /**
     * Are we the dungeon heart?
     *
     * @return are we?
     */
    boolean isDungeonHeart();

    /**
     * Notify and mark the room as destroyed
     */
    void destroy();

    /**
     * Is this room instance destroyed? Not in the world anymore.
     *
     * @return is the room destroyed
     * @see #destroy()
     */
    boolean isDestroyed();

    /**
     * Signal that the room has been captured
     *
     * @param playerId the new owner ID
     */
    void captured(short playerId);

    boolean isFullCapacity();

}

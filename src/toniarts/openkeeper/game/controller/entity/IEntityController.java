/*
 * Copyright (C) 2014-2019 OpenKeeper
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
package toniarts.openkeeper.game.controller.entity;

import com.jme3.math.Vector3f;
import com.simsilica.es.EntityId;
import toniarts.openkeeper.game.controller.creature.ICreatureController;
import toniarts.openkeeper.game.map.MapTile;

/**
 * Common interface for all kinds of entities
 *
 * @author Toni Helenius <helenius.toni@gmail.com>
 */
public interface IEntityController extends Comparable<IEntityController> {

    EntityId getEntityId();

    short getOwnerId();

    Vector3f getPosition();

    MapTile getTile();

    int getHealth();

    int getMaxHealth();

    boolean isFullHealth();

    /**
     * Get percentage of health
     *
     * @return human formatted percentage
     */
    default int getHealthPercentage() {
        return (int) ((getHealth() * 100.0f) / getMaxHealth());
    }

    boolean isPickedUp();

    /**
     * Removes the entity from the world immediately. Entity loses all its
     * possession
     *
     * @see #removePossession()
     */
    void remove();

    /**
     * Removes all possession from the entity, the possession is dropped back to
     * world
     *
     * @see #remove()
     */
    void removePossession();

    /**
     * Is the entity removed from the world (destroyed, dead...)
     *
     * @return true if the entity does not exist anymore
     */
    boolean isRemoved();

    /**
     * Assigns the given creature haul us
     *
     * @param creature the creature hauling us
     */
    void setHaulable(ICreatureController creature);

    /**
     * Is the entity being dragged, or hauled
     *
     * @return true is hauled
     */
    boolean isDragged();

}

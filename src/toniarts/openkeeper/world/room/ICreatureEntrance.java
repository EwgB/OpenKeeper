/*
 * Copyright (C) 2014-2016 OpenKeeper
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
package toniarts.openkeeper.world.room;

import com.jme3.app.Application;
import toniarts.openkeeper.world.ThingLoader;
import toniarts.openkeeper.world.creature.CreatureControl;

import java.awt.*;
import java.util.List;

/**
 * Signifies that a room is an entrance for creatures
 *
 * @author Toni Helenius <helenius.toni@gmail.com>
 */
@Deprecated
public interface ICreatureEntrance {

    /**
     * Get the coordinate for the creature to spawn on this entrance
     *
     * @return the coordinate
     */
    Point getEntranceCoordinate();

    /**
     * Get creatures attracted on this entrance
     *
     * @return
     */
    List<CreatureControl> getAttractedCreatures();

    CreatureControl spawnCreature(short creatureId, short level,
                                  Application app, ThingLoader thingLoader);

    CreatureControl spawnCreature(short creatureId, Application app, ThingLoader thingLoader);
}

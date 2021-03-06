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
package toniarts.openkeeper.game.state.session;

import com.jme3.math.Vector2f;
import com.jme3.network.service.rmi.Asynchronous;
import com.simsilica.es.EntityId;
import toniarts.openkeeper.game.state.CheatState;

import java.awt.*;

/**
 * Listener for the service. To listen to clients' requests
 *
 * @author Toni Helenius <helenius.toni@gmail.com>
 */
public interface GameSessionServiceListener {

    /**
     * Build a building to the wanted area
     *
     * @param start    start coordinates
     * @param end      end coordinates
     * @param roomId   room to build
     * @param playerId the player who builds the room
     */
    @Asynchronous
    void onBuild(Vector2f start, Vector2f end, short roomId, short playerId);

    /**
     * Sell building(s) from the wanted area
     *
     * @param start    start coordinates
     * @param end      end coordinates
     * @param playerId the player who sells the tile
     */
    @Asynchronous
    void onSell(Vector2f start, Vector2f end, short playerId);

    /**
     * Set some tiles selected/undetected
     *
     * @param start    start coordinates
     * @param end      end coordinates
     * @param select   select or deselect
     * @param playerId the player who selected the tile
     */
    @Asynchronous
    void onSelectTiles(Vector2f start, Vector2f end, boolean select, short playerId);

    /**
     * Player wants to interact with an entity
     *
     * @param entity   the entity to be interacted upon
     * @param playerId the player who interacts
     */
    @Asynchronous
    void onInteract(EntityId entity, short playerId);

    /**
     * Player wants to pick up an entity
     *
     * @param entity   the entity to be picked up
     * @param playerId the player who picks up
     */
    @Asynchronous
    void onPickUp(EntityId entity, short playerId);

    /**
     * Player wants to drop an entity
     *
     * @param entity       the entity to be dropped
     * @param tile         the tile 2D coordinates to drop to
     * @param coordinates  the real world coordinates on the tile
     * @param dropOnEntity if there is an entity to which we drop this one to
     * @param playerId     the player who drops
     */
    @Asynchronous
    void onDrop(EntityId entity, Point tile, Vector2f coordinates, EntityId dropOnEntity, short playerId);

    /**
     * Player wants to make a withdrawal of funds
     *
     * @param amount   the amount requested
     * @param playerId who requested
     */
    @Asynchronous
    void onGetGold(int amount, short playerId);

    /**
     * Player UI transition has ended
     *
     * @param playerId the player whose transition ended
     */
    @Asynchronous
    void onTransitionEnd(short playerId);

    /**
     * Player has requested to pause the game
     *
     * @param playerId the player who wants to pause the game
     */
    @Asynchronous
    void onPauseRequest(short playerId);

    /**
     * Player has requested to resume the game
     *
     * @param playerId the player who wants to resume the game
     */
    @Asynchronous
    void onResumeRequest(short playerId);

    /**
     * Player has exited the game
     *
     * @param playerId the player who quits the game
     */
    @Asynchronous
    void onExitGame(short playerId);

    /**
     * Player has triggered a cheat
     *
     * @param cheat    the cheat triggered
     * @param playerId the player who wants to cheat
     */
    @Asynchronous
    void onCheatTriggered(CheatState.CheatType cheat, short playerId);

}

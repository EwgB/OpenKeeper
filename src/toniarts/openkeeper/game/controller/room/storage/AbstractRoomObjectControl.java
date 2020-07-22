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
package toniarts.openkeeper.game.controller.room.storage;

import com.simsilica.es.EntityId;
import toniarts.openkeeper.game.component.Decay;
import toniarts.openkeeper.game.component.ObjectComponent;
import toniarts.openkeeper.game.component.Owner;
import toniarts.openkeeper.game.component.RoomStorage;
import toniarts.openkeeper.game.controller.IGameTimer;
import toniarts.openkeeper.game.controller.IObjectsController;
import toniarts.openkeeper.game.controller.ObjectsController;
import toniarts.openkeeper.game.controller.room.IRoomController;
import toniarts.openkeeper.tools.convert.map.GameObject;
import toniarts.openkeeper.tools.convert.map.KwdFile;

import java.awt.*;
import java.util.List;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Room object controller. FIXME: Cache the coordinates and listen to changes in
 * rooms
 *
 * @param <V> the value type to add
 * @author Toni Helenius <helenius.toni@gmail.com>
 */
public abstract class AbstractRoomObjectControl<V> implements IRoomObjectControl<V> {

    protected final KwdFile kwdFile;
    protected final IRoomController parent;
    protected final IObjectsController objectsController;
    private final IGameTimer gameTimer;
    protected final Map<Point, Collection<EntityId>> objectsByCoordinate = new HashMap<>();

    public AbstractRoomObjectControl(KwdFile kwdFile, IRoomController parent, IObjectsController objectsController,
                                     IGameTimer gameTimer) {
        this.kwdFile = kwdFile;
        this.parent = parent;
        this.objectsController = objectsController;
        this.gameTimer = gameTimer;
    }

    protected abstract int getObjectsPerTile();

    protected abstract int getNumberOfAccessibleTiles();

    /**
     * Get a room objects
     *
     * @param p the object from point
     * @return the objects in given point
     */
    @Override
    public Collection<EntityId> getItems(Point p) {
        return objectsByCoordinate.get(p);
    }

    /**
     * Get the max capacity of the room
     *
     * @return max capacity in number of objects
     */
    @Override
    public int getMaxCapacity() {
        return getObjectsPerTile() * getNumberOfAccessibleTiles();
    }

    /**
     * Is the room at full capacity
     *
     * @return max capacity used
     */
    @Override
    public boolean isFullCapacity() {
        return getCurrentCapacity() >= getMaxCapacity();
    }

    /**
     * Remove an item
     *
     * @param object the object
     */
    @Override
    public void removeItem(EntityId object) {
        objectsController.getEntityData().removeComponent(object, RoomStorage.class);
        addNoRoomDecay(object);
        for (Collection<EntityId> objects : objectsByCoordinate.values()) {
            if (objects.remove(object)) {
                break;
            }
        }
    }

    /**
     * Removes all objects (for real)
     */
    protected void removeAllObjects() {
        List<Collection<EntityId>> objectList = new ArrayList<>(objectsByCoordinate.values());
        for (Collection<EntityId> objects : objectList) {
            for (EntityId obj : objects) {
                //obj.removeObject();
                objectsController.getEntityData().removeEntity(obj);
            }
        }
    }

    /**
     * Gets all coordinates, coordinates that can handle the objects
     *
     * @return list of all coordinates
     */
    protected Collection<Point> getCoordinates() {
        return parent.getRoomInstance()
                .getCoordinates()
                .stream()
                .filter(p -> parent.isTileAccessible(null, p))
                .collect(Collectors.toList());
    }

    /**
     * Gets available coordinates
     *
     * @return list of available coordinates
     */
    @Override
    public Collection<Point> getAvailableCoordinates() {
        return getCoordinates().stream()
                .filter(p -> {
                    Collection<EntityId> items = getItems(p);
                    return items == null || items.size() != getObjectsPerTile();
                })
                .collect(Collectors.toList());
    }

    protected void setRoomStorageToItem(EntityId entityId, boolean changeOwner) {
        objectsController.getEntityData().setComponent(entityId, new RoomStorage(getObjectType()));
        ObjectComponent objectComponent = objectsController.getEntityData().getComponent(entityId, ObjectComponent.class);
        if (objectComponent != null && kwdFile.getObject(objectComponent.objectId).getFlags().contains(GameObject.ObjectFlag.DIE_OVER_TIME_IF_NOT_IN_ROOM)) {
            objectsController.getEntityData().removeComponent(entityId, Decay.class);
        }

        if (changeOwner) {

            // Also set the owner if there is one already
            changeEntityOwner(entityId, parent.getRoomInstance().getOwnerId());
        }
    }

    @Override
    public void captured(short playerId) {

        // The new owner shall also own the riches we hold
        List<Collection<EntityId>> objectList = new ArrayList<>(objectsByCoordinate.values());
        for (Collection<EntityId> objects : objectList) {
            for (EntityId obj : objects) {
                changeEntityOwner(obj, playerId);
            }
        }
    }

    private void changeEntityOwner(EntityId entity, short playerId) {
        Owner owner = objectsController.getEntityData().getComponent(entity, Owner.class);
        if (owner != null && owner.ownerId != playerId) {
            objectsController.getEntityData().setComponent(entity, new Owner(playerId));
        }
    }

    /**
     * Adds decay for the object for not being in the room if such is defined on
     * the object
     *
     * @param object the entity ID
     */
    protected void addNoRoomDecay(EntityId object) {
        ObjectComponent objectComponent = objectsController.getEntityData().getComponent(object, ObjectComponent.class);
        if (objectComponent != null && kwdFile.getObject(objectComponent.objectId).getFlags().contains(GameObject.ObjectFlag.DIE_OVER_TIME_IF_NOT_IN_ROOM)) {
            objectsController.getEntityData().setComponent(object, new Decay(gameTimer.getGameTime(), ObjectsController.OBJECT_TIME_TO_LIVE));
        }
    }

}

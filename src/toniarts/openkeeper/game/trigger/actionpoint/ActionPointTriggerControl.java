/*
 * Copyright (C) 2014-2015 OpenKeeper
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
package toniarts.openkeeper.game.trigger.actionpoint;

import toniarts.openkeeper.game.controller.*;
import toniarts.openkeeper.game.controller.creature.ICreatureController;
import toniarts.openkeeper.game.data.ActionPoint;
import toniarts.openkeeper.game.logic.IEntityPositionLookup;
import toniarts.openkeeper.game.map.MapData;
import toniarts.openkeeper.game.map.MapTile;
import toniarts.openkeeper.game.trigger.TriggerControl;
import toniarts.openkeeper.game.trigger.TriggerGenericData;
import toniarts.openkeeper.tools.convert.map.TriggerGeneric;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author ArchDemon
 */
public class ActionPointTriggerControl extends TriggerControl {

    private static final Logger LOGGER = Logger.getLogger(ActionPointTriggerControl.class.getName());

    private ActionPoint ap;
    private IEntityPositionLookup entityPositionLookup;

    public ActionPointTriggerControl() { // empty serialization constructor
        super();
    }

    public ActionPointTriggerControl(final IGameController gameController, final ILevelInfo levelInfo, final IGameTimer gameTimer,
            final IMapController mapController, final ICreaturesController creaturesController, int triggerId, ActionPoint ap,
            final IEntityPositionLookup entityPositionLookup) {
        super(gameController, levelInfo, gameTimer, mapController, creaturesController, triggerId);
        this.ap = ap;
        this.entityPositionLookup = entityPositionLookup;
    }

    @Override
    protected boolean isActive(TriggerGenericData trigger) {
        boolean result = false;

        int target = 0;
        int value = 0;

        TriggerGeneric.TargetType targetType = trigger.getType();
        switch (targetType) {
            case AP_CONGREGATE_IN:
                short playerId = trigger.getUserData("playerId", short.class);
                short targetId = trigger.getUserData("targetId", short.class);
                value = trigger.getUserData("value", int.class);
                short type = trigger.getUserData("targetType", short.class);
                switch (type) {
                    case 0:
                    case 3: // Creature
                        for (int x = ap.getStart().x; x <= ap.getEnd().x; x++) {
                            for (int y = ap.getStart().y; y <= ap.getEnd().y; y++) {
                                for (ICreatureController creature : entityPositionLookup.getEntityTypesInLocation(x, y, ICreatureController.class)) {
                                    if ((playerId == 0 || creature.getOwnerId() == playerId) && (targetId == 0 || creature.getCreature().getCreatureId() == targetId)) {
                                        target++;
                                    }
                                }
                            }
                        }
                        break;
                    case 6: // Object
                        break;
                    default:
                        LOGGER.log(Level.WARNING, "AP_CONGREGATE_IN unknown targetType {0}", type);
                        break;
                }
                return false;

            case AP_POSSESSED_CREATURE_ENTERS:
                playerId = trigger.getUserData("playerId", short.class);
                targetId = trigger.getUserData("targetId", short.class);
                value = trigger.getUserData("value", int.class);
                type = trigger.getUserData("targetType", short.class);
                switch (type) {
                    //case 0:
                    case 3: // Creature
                        break;
                    case 6: // Object. Chicken
                        break;
                    default:
                        LOGGER.warning("AP_POSSESSED_CREATURE_ENTERS unknown targetType");
                        break;
                }
                return false;

            case AP_CLAIM_PART_OF:
                playerId = trigger.getUserData("playerId", short.class);
                value = trigger.getUserData("value", int.class);

                MapData map = mapController.getMapData();
                for (int x = ap.getStart().x; x <= ap.getEnd().x; x++) {
                    for (int y = ap.getStart().y; y <= ap.getEnd().y; y++) {
                        if (playerId == map.getTile(x, y).getOwnerId()) {
                            target++;
                        }
                    }
                }
                break;

            case AP_CLAIM_ALL_OF:
                playerId = trigger.getUserData("playerId", short.class);
                // value = trigger.getUserData("value", int.class); // Not useful?
                map = mapController.getMapData();
                for (int x = ap.getStart().x; x <= ap.getEnd().x; x++) {
                    for (int y = ap.getStart().y; y <= ap.getEnd().y; y++) {
                        if (playerId != map.getTile(x, y).getOwnerId()) {
                            return false;
                        }
                    }
                }
                return true;

            case AP_SLAB_TYPES:
                playerId = trigger.getUserData("playerId", short.class);
                targetId = trigger.getUserData("terrainId", short.class);
                value = trigger.getUserData("value", int.class);

                map = mapController.getMapData();
                for (int x = ap.getStart().x; x <= ap.getEnd().x; x++) {
                    for (int y = ap.getStart().y; y <= ap.getEnd().y; y++) {
                        MapTile tile = map.getTile(x, y);

                        if (playerId != 0 && playerId != tile.getOwnerId() || targetId != tile.getTerrainId()) {
                            continue;
                        }

                        target++;
                    }
                }
                break;

            case AP_TAG_PART_OF:
                playerId = trigger.getUserData("playerId", short.class);
                value = trigger.getUserData("value", int.class);

                map = mapController.getMapData();
                for (int x = ap.getStart().x; x <= ap.getEnd().x; x++) {
                    for (int y = ap.getStart().y; y <= ap.getEnd().y; y++) {
                        // TODO check who tagged tile
                        if (map.getTile(x, y).isSelected(playerId)) {
                            target++;
                        }
                    }
                }
                break;

            case AP_TAG_ALL_OF:
                playerId = trigger.getUserData("playerId", short.class);
                // value = trigger.getUserData("value", int.class); // Not useful?
                map = mapController.getMapData();
                for (int x = ap.getStart().x; x <= ap.getEnd().x; x++) {
                    for (int y = ap.getStart().y; y <= ap.getEnd().y; y++) {
                        // TODO check who tagged tile
                        if (!map.getTile(x, y).isSelected(playerId)) {
                            return false;
                        }
                    }
                }
                return true;

            default:
                return super.isActive(trigger);
        }

        TriggerGeneric.ComparisonType comparisonType = trigger.getComparison();
        if (comparisonType != null && comparisonType != TriggerGeneric.ComparisonType.NONE) {
            result = compare(target, comparisonType, value);
        }

        return result;
    }
}

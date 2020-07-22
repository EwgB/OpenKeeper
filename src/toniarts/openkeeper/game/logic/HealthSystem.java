/*
 * Copyright (C) 2014-2018 OpenKeeper
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
package toniarts.openkeeper.game.logic;

import com.jme3.util.SafeArrayList;
import com.simsilica.es.Entity;
import com.simsilica.es.EntityData;
import com.simsilica.es.EntityId;
import com.simsilica.es.EntitySet;
import toniarts.openkeeper.game.component.*;
import toniarts.openkeeper.game.controller.ICreaturesController;
import toniarts.openkeeper.game.controller.creature.CreatureState;
import toniarts.openkeeper.game.map.MapTile;
import toniarts.openkeeper.tools.convert.map.Creature;
import toniarts.openkeeper.tools.convert.map.KwdFile;
import toniarts.openkeeper.tools.convert.map.Variable;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Manages and monitors thing healthiness. Beeb... beeb... beeeeeeeeeeeeeeeeeeeb
 * :)<br>
 * This creates some counter etc. that basically needs to be saved then, but I
 * felt wrong to add them to the components, idk
 *
 * @author Toni Helenius <helenius.toni@gmail.com>
 */
public class HealthSystem implements IGameLogicUpdatable {

    private final KwdFile kwdFile;
    private final EntitySet healthEntities;
    private final EntityData entityData;
    private final SafeArrayList<EntityId> entityIds;
    private final IEntityPositionLookup entityPositionLookup;
    private final ICreaturesController creaturesController;
    private final int timeToDeath;
    private final Map<EntityId, Double> timeOnOwnLandByEntityId = new HashMap<>();
    private final Map<EntityId, Double> timeUnconsciousByEntityId = new HashMap<>();

    public HealthSystem(EntityData entityData, KwdFile kwdFile, IEntityPositionLookup entityPositionLookup,
            Map<Variable.MiscVariable.MiscType, Variable.MiscVariable> gameSettings,
            ICreaturesController creaturesController) {
        this.kwdFile = kwdFile;
        this.entityData = entityData;
        this.entityPositionLookup = entityPositionLookup;
        this.creaturesController = creaturesController;
        entityIds = new SafeArrayList<>(EntityId.class);

        timeToDeath = (int) gameSettings.get(Variable.MiscVariable.MiscType.CREATURE_DYING_STATE_DURATION_SECONDS).getValue();

        healthEntities = entityData.getEntities(Health.class);
        processAddedEntities(healthEntities);
    }

    @Override
    public void processTick(float tpf, double gameTime) {
        if (healthEntities.applyChanges()) {

            processAddedEntities(healthEntities.getAddedEntities());

            processDeletedEntities(healthEntities.getRemovedEntities());

            processChangedEntities(healthEntities.getChangedEntities(), gameTime);
        }

        // Bring death to those unfortunate and increase the health of the fortunate
        for (EntityId entityId : entityIds.getArray()) {
            Health health = entityData.getComponent(entityId, Health.class);

            // From unconsciousness we start the countdown to death
            if (health.unconscious) {
                if (gameTime - timeUnconsciousByEntityId.get(entityId) >= timeToDeath) {
                    processDeath(entityId, gameTime);
                }
                continue;
            }

            // Normal health related routines
            if (health.health <= 0) {

                // Death or destruction!!!!
                CreatureComponent creatureComponent = entityData.getComponent(entityId, CreatureComponent.class);
                if (creatureComponent != null && kwdFile.getCreature(creatureComponent.creatureId).getFlags().contains(Creature.CreatureFlag.GENERATE_DEAD_BODY)) {
                    entityData.setComponent(entityId, new Health(health.ownLandHealthIncrease, 0, health.maxHealth, true));
                    //entityData.setComponent(entityId, new CreatureAi(gameTime, CreatureState.UNCONSCIOUS, creatureComponent.creatureId)); // Hmm
                    creaturesController.createController(entityId).getStateMachine().changeState(CreatureState.UNCONSCIOUS);
                    entityData.removeComponent(entityId, Navigation.class);
                    timeUnconsciousByEntityId.put(entityId, gameTime);
                } else {
                    entityPositionLookup.getEntityController(entityId).remove();
                }
            } else if (health.ownLandHealthIncrease > 0 && health.health != health.maxHealth) {
                MapTile tile = entityPositionLookup.getEntityLocation(entityId);
                Owner owner = entityData.getComponent(entityId, Owner.class);
                if (tile != null && owner != null && tile.getOwnerId() == owner.ownerId) {

                    // In own land
                    Double lastTimeOnOwnLand = timeOnOwnLandByEntityId.get(entityId);
                    if (lastTimeOnOwnLand == null) {
                        timeOnOwnLandByEntityId.put(entityId, gameTime);
                    } else if (gameTime - lastTimeOnOwnLand >= 1) {

                        // Increase health
                        entityData.setComponent(entityId, new Health(health.ownLandHealthIncrease, Math.max(health.health + health.ownLandHealthIncrease, health.maxHealth), health.maxHealth, false));
                    }
                } else {

                    // At someones elses land, reset counter
                    timeOnOwnLandByEntityId.replace(entityId, null);
                }
            }
        }
    }

    private void processDeath(EntityId entityId, double gameTime) {
        entityData.removeComponent(entityId, Health.class);
        entityData.removeComponent(entityId, CreatureAi.class);
        entityData.removeComponent(entityId, ChickenAi.class);
        entityData.removeComponent(entityId, ObjectViewState.class);
        entityData.removeComponent(entityId, Navigation.class);
        entityData.removeComponent(entityId, Interaction.class);
        entityData.setComponent(entityId, new Death(gameTime));
    }

    private void processAddedEntities(Set<Entity> entities) {
        for (Entity entity : entities) {
            int index = Collections.binarySearch(entityIds, entity.getId());
            entityIds.add(~index, entity.getId());
        }
    }

    private void processDeletedEntities(Set<Entity> entities) {
        for (Entity entity : entities) {
            int index = Collections.binarySearch(entityIds, entity.getId());
            entityIds.remove(index);
            timeOnOwnLandByEntityId.remove(entity.getId());
            timeUnconsciousByEntityId.remove(entity.getId());
        }
    }

    private void processChangedEntities(Set<Entity> entities, double gameTime) {
        for (Entity entity : entities) {

            // If the health is changed (either by us or damage)...
            // Reset the health regen counter
            timeOnOwnLandByEntityId.replace(entity.getId(), null);
        }
    }

    @Override
    public void start() {

    }

    @Override
    public void stop() {
        healthEntities.release();
        timeOnOwnLandByEntityId.clear();
        timeUnconsciousByEntityId.clear();
    }

}

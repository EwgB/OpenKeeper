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
package toniarts.openkeeper.game.controller.creature;

import com.badlogic.gdx.ai.fsm.StateMachine;
import com.simsilica.es.EntityId;
import toniarts.openkeeper.game.controller.entity.IEntityController;
import toniarts.openkeeper.game.controller.object.IObjectController;
import toniarts.openkeeper.game.data.ObjectiveType;
import toniarts.openkeeper.game.logic.IGameLogicUpdatable;
import toniarts.openkeeper.game.navigation.pathfinding.INavigable;
import toniarts.openkeeper.game.task.Task;
import toniarts.openkeeper.tools.convert.map.Creature;
import toniarts.openkeeper.tools.convert.map.Thing;

import java.awt.*;

/**
 * Controls creature entities
 *
 * @author Toni Helenius <helenius.toni@gmail.com>
 */
public interface ICreatureController extends IGameLogicUpdatable, INavigable, IEntityController {

    boolean shouldFleeOrAttack();

    /**
     * Checks what the creature sees and hears
     */
    void checkSurroundings();

    public void unassignCurrentTask();

    void navigateToRandomPoint();

    IPartyController getParty();

    StateMachine<ICreatureController, CreatureState> getStateMachine();

    boolean hasObjective();

    boolean followObjective();

    boolean needsLair();

    boolean hasLair();

    boolean findLair();

    boolean isNeedForSleep();

    boolean goToSleep();

    boolean findWork();

    boolean isWorker();

    boolean isTooMuchGold();

    boolean dropGoldToTreasury();

    boolean isStopped();

    void navigateToAssignedTask();

    boolean isAtAssignedTaskTarget();

    void dropGold();

    boolean isWorkNavigationRequired();

    boolean isAssignedTaskValid();

    ICreatureController getAttackTarget();

    boolean isWithinAttackDistance(EntityId attackTarget);

    void stopCreature();

    void executeAttack(EntityId attackTarget);

    void navigateToAttackTarget(EntityId attackTarget);

    ICreatureController getFollowTarget();

    Task getAssignedTask();

    float getDistanceToCreature(EntityId target);

    void navigateToRandomPointAroundTarget(EntityId target, int radius);

    void resetFollowTarget();

    void flee();

    boolean isAttacked();

    boolean isEnoughSleep();

    boolean isIncapacitated();

    boolean isTimeToReEvaluate();

    void resetReEvaluationTimer();

    int getGold();

    int getMaxGold();

    void subtractGold(int amount);

    Point getLairLocation();

    boolean isUnconscious();

    Point getCreatureCoordinates();

    void setAssignedTask(Task task);

    void executeAssignedTask();

    Creature getCreature();

    void addGold(int amount);

    int getObjectiveTargetActionPointId();

    void setObjectiveTargetActionPointId(int actionPointId);

    Thing.HeroParty.Objective getObjective();

    void setObjective(Thing.HeroParty.Objective objective);

    boolean isDead();

    boolean isImprisoned();

    boolean isTortured();

    boolean isStunned();

    int getLevel();

    void attachPortalGem();

    void setObjectiveTargetPlayerId(short playerId);

    short getObjectiveTargetPlayerId();

    void setPlayerObjective(ObjectiveType objective);

    void setCreatureLair(EntityId lairId);

    /**
     * Evaluates the time spent in current state and compares it to the
     * creatures target time in a state. The target time would be set by either
     * animation or level variable
     *
     * @return {@code true} if state should be changed
     */
    boolean isStateTimeExceeded();

    void sleep();

    /**
     * Set a target for us to follow
     *
     * @param target target to follow
     */
    void setFollowTarget(EntityId target);

    boolean shouldNavigateToFollowTarget();

    boolean isSlapped();

    boolean isPortalGemInPossession();

    /**
     * When a creature is hauled to prison, call this to properly seal the enemy
     * to the prison
     */
    void imprison();

    /**
     * Is the (neutral) creature claimed
     *
     * @return returns {@code true} if the creature is owned by a keeper
     */
    boolean isClaimed();

    boolean isHungry();

    boolean goToEat();

    /**
     * Makes the creature eat the target
     *
     * @param target the devouree
     */
    void eat(IEntityController target);

    /**
     * Marks that we have eaten a single ration of food
     */
    void sate();

    /**
     * Get research per second attribute
     *
     * @return research per second
     */
    int getResearchPerSecond();

    /**
     * Gives object to creature
     *
     * @param object the object, might be food, gold, or what ever
     */
    void giveObject(IObjectController object);

}

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
package toniarts.openkeeper.game.state;

import com.jme3.math.Vector3f;
import com.simsilica.es.EntityId;
import de.lessvoid.nifty.screen.ScreenController;

/**
 * Offers services to the player screen, that is the in-game UI
 *
 * @author ArchDemon
 */
public interface IPlayerScreenController extends ScreenController {

    String SCREEN_HUD_ID = "hud";
    String SCREEN_POSSESSION_ID = "possession";
    String SCREEN_CINEMATIC_ID = "cinematic";
    String SCREEN_EMPTY_ID = "empty";

    /**
     * Select active item on HUD
     *
     * @param iState name of InteractionState#Type
     * @param id     id of selected item
     * @see toniarts.openkeeper.view.PlayerInteractionState.InteractionState.Type
     */
    void select(String iState, String id);

    void playSound(String category, String id);

    void playButtonSound(String category);

    void togglePanel();

    void toggleObjective();

    void pauseMenu();

    void onPaused(boolean paused);

    void pauseMenuNavigate(String menu, String backMenu,
                           String confirmationTitle, String confirmMethod);

    void zoomToDungeon();

    void workersAmount(String uiState);

    void grabGold();

    String getTooltipText(String bundleId);

    void quitToMainMenu();

    void quitToOS();

    /**
     *
     * @param tpf
     */
    void update(float tpf);

    void cleanup();

    /**
     * Zoom to entity
     *
     * @param entityId the entity ID to zoom to
     */
    void zoomToEntity(EntityId entityId);

    /**
     * Zoom to position
     *
     * @param position the position to zoom to
     */
    void zoomToPosition(Vector3f position);

    /**
     * Pick up an entity
     *
     * @param entityId the entity ID to pick up
     */
    void pickUpEntity(EntityId entityId);
}

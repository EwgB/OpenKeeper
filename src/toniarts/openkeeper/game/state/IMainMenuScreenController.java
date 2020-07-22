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

import de.lessvoid.nifty.screen.ScreenController;

/**
 * @author ArchDemon
 */
public interface IMainMenuScreenController extends ScreenController {

    String SOUND_BUTTON_ID = "buttonClick";
    String SOUND_MENU_ID = "menuClick";
    String SCREEN_EMPTY_ID = "empty";
    String SCREEN_START_ID = "start";
    String SCREEN_DEBRIEFING_ID = "debriefing";
    String SCREEN_OPTIONS_MAIN_ID = "optionsMain";
    String PLAYER_LIST_ID = "playersTable";

    void applySoundSettings();

    //// screen id="multiplayerCreate"  ////
    //public void multiplayerSend()
    void addComputerPlayer();

    //// screen id="skirmish" and id="multiplayerCreate" ////
    void changeAI();

    void kickPlayer();

    void setPlayerReady();

    //public void gameSettings();
    void selectRandomMap();

    /**
     * Start local multiplayer
     */
    void startSkirmish();

    //// screen id="myPetDungeon" ////
    /**
     * Select a my pet dungeon level
     *
     * @param number the level number as a string
     */
    void selectMPDLevel(String number);

    //// screen id="skirmishMapSelect" ////
    void cancelMapSelection();

    void mapSelected();

    //// screen id="multiplayerLocal" ////
    void connectToServer();

    void multiplayerCreate();

    void multiplayerConnect();

    void multiplayerRefresh();

    //// screen id="extras" ////
    void playMovie(String movieFile);

    //// screen id="optionsGraphics"  ////
    /**
     * Save the graphics settings
     */
    void applyGraphicsSettings();

    //// screen id="campaign" ////
    void startLevel(String type);

    /**
     * Cancel level selection and go back to the campaign map selection
     */
    void cancelLevelSelect();

    //// see CreditsControl.xml ////
    /**
     * TODO name of function set to a variable Called by the gui to restart the
     * autoscroll effect
     */
    void restartCredits();

    //// screen id="quitGame" ////
    void quitToOS();

    //// screen id="start" and ... ////
    /**
     * Switch to another screen
     *
     * @param screen the screen id as a string
     */
    void goToScreen(String screen);

    /**
     * Go to screen with cinematic transition
     *
     * @param transition the transition code
     * @param screen the screen to go to
     * @param transitionStatic the transition for the finishing position. Not
     * all the transitions return perfectly so this is a workaround
     */
    void doTransition(String transition, String screen, String transitionStatic);

    /**
     * Cancel multiplayer lobby
     */
    void cancelMultiplayer();
}

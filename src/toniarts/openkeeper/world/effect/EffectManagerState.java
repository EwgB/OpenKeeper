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
package toniarts.openkeeper.world.effect;

import com.jme3.app.Application;
import com.jme3.app.state.AbstractAppState;
import com.jme3.app.state.AppStateManager;
import com.jme3.asset.AssetManager;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import toniarts.openkeeper.tools.convert.map.KwdFile;
import toniarts.openkeeper.world.WorldState;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

/**
 * An app state to manage ALL the effects in the world. Mainly their lifetime.
 *
 * @author ArchDemon
 * @author Toni Helenius <helenius.toni@gmail.com>
 */
@Deprecated
public class EffectManagerState extends AbstractAppState {

    public static final int ROOM_CLAIM_ID = 2;

    private final KwdFile kwdFile;
    private final AssetManager assetManager;
    private final List<VisualEffect> activeEffects = new ArrayList<>();
    private AppStateManager stateManager;
    private static final Logger logger = Logger.getLogger(EffectManagerState.class.getName());

    public EffectManagerState(KwdFile kwdFile, AssetManager assetManager) {
        this.kwdFile = kwdFile;
        this.assetManager = assetManager;
    }

    @Override
    public void initialize(AppStateManager stateManager, Application app) {
        super.initialize(stateManager, app);
        this.stateManager = stateManager;
    }

    @Override
    public void update(float tpf) {

        // Maintain the effects (on every frame?)
        activeEffects.removeIf(visualEffect -> !visualEffect.update(tpf));
    }

    /**
     * Loads up an particle effect
     *
     * @param node the node to attach the effect to
     * @param location particle effect node location, maybe {@code null}
     * @param effectId the effect ID to load
     * @param infinite the effect should restart always, infinite effect (room
     * effects...?)
     */
    public void loadSingleEffect(Node node, Vector3f location, int effectId, boolean infinite) {

        clearActiveEffects();
        // Load the effect
        load(node, location, effectId, infinite);
    }

    public void clearActiveEffects() {
        for (VisualEffect visualEffect : activeEffects) {
            visualEffect.removeEffect();
            visualEffect.update(-1);
        }
        activeEffects.clear();
    }

    /**
     * Loads up an particle effect
     *
     * @param node the node to attach the effect to
     * @param location particle effect node location, maybe {@code null}
     * @param effectId the effect ID to load
     * @param infinite the effect should restart always, infinite effect (room
     * effects...?)
     */
    public void load(Node node, Vector3f location, int effectId, boolean infinite) {

        // Load the effect
        if (effectId == 0) {
            return;
        }
        VisualEffect visualEffect = new VisualEffect(this, node, location, kwdFile.getEffect(effectId), infinite);
        activeEffects.add(visualEffect);
    }

    public WorldState getWorldState() {
        return stateManager.getState(WorldState.class);
    }

    public AssetManager getAssetManger() {
        return assetManager;
    }

    public KwdFile getKwdFile() {
        return kwdFile;
    }
}

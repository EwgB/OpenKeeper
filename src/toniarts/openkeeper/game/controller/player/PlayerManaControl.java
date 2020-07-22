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
package toniarts.openkeeper.game.controller.player;

import com.jme3.util.SafeArrayList;
import toniarts.openkeeper.game.data.Keeper;
import toniarts.openkeeper.game.listener.PlayerManaListener;
import toniarts.openkeeper.tools.convert.map.Variable;
import toniarts.openkeeper.tools.convert.map.Variable.MiscVariable.MiscType;

import java.util.Map;

/**
 * Controller of player mana
 *
 * @author ArchDemon
 */
public class PlayerManaControl {

    private final Keeper keeper;
    private final SafeArrayList<PlayerManaListener> listeners = new SafeArrayList<>(PlayerManaListener.class);

    public PlayerManaControl(Keeper keeper,
            Map<Variable.MiscVariable.MiscType, Variable.MiscVariable> gameSettings) {
        this.keeper = keeper;

        this.keeper.setMaxMana((int) gameSettings.get(MiscType.MAXIMUM_MANA_THRESHOLD).getValue());
    }

    /**
     * Updates gain and loss and adds/removes mana accordingly to the player
     *
     * @param gain mana gain
     * @param loss mana loss
     */
    public void updateMana(int gain, int loss) {

        keeper.setManaGain(gain);
        keeper.setManaLoss(loss);

        addMana(keeper.getManaGain() - keeper.getManaLoss());

        updateListeners();
    }

    public void addMana(int value) {
        value = Math.max(0, keeper.getMana() + value);
        keeper.setMana(Math.min(value, keeper.getMaxMana()));
    }

    private void updateListeners() {
        for (PlayerManaListener listener : listeners.getArray()) {
            listener.onManaChange(keeper.getId(), keeper.getMana(), keeper.getManaLoss(), keeper.getManaGain());
        }
    }

    public void addListener(PlayerManaListener listener) {
        listeners.add(listener);
    }

    public void removeListener(PlayerManaListener listener) {
        listeners.remove(listener);
    }
}

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
package toniarts.openkeeper.game.state.lobby;

import toniarts.openkeeper.game.data.Keeper;
import toniarts.openkeeper.tools.convert.map.Player;

import java.util.*;

import static java.util.stream.Collectors.toList;

/**
 * Some helper functions for game lobby management
 *
 * @author Toni Helenius <helenius.toni@gmail.com>
 */
public class LobbyUtils {

    private LobbyUtils() {
        // No, nope...
    }

    /**
     * Get the next available keeper
     *
     * @param ai AI or human
     * @param players the current player list
     * @return the next available Keeper
     */
    public static Keeper getNextKeeper(boolean ai, Set<ClientInfo> players) {
        short id = Player.KEEPER1_ID;
        List<Short> keepers = players.stream()
                .map(ClientInfo::getKeeper)
                .map(Keeper::getId)
                .sorted()
                .collect(toList());
        while (Collections.binarySearch(keepers, id) >= 0) {
            id++;
        }
        return new Keeper(ai, id);
    }

    /**
     * Kicks out AI players that do not fit to the map, fixes Keepers so that
     * they are in order
     *
     * @param players the player list
     * @param maxPlayers the max player count
     * @return a list of players to kick
     */
    public static Set<ClientInfo> compactPlayers(Set<ClientInfo> players, int maxPlayers) {
        Set<ClientInfo> kickedPlayers = Collections.emptySet();

        // Kick excessive players (AI only)
        if (players.size() > maxPlayers) {
            List<ClientInfo> keepers = new ArrayList<>(players);
            keepers.sort((ClientInfo o1, ClientInfo o2) -> Short.compare(o2.getKeeper().getId(), o1.getKeeper().getId()));
            int playersToKick = players.size() - maxPlayers;
            int playersKicked = 0;
            kickedPlayers = new HashSet<>(playersToKick);
            for (ClientInfo keeper : keepers) {
                if (keeper.getKeeper().isAi()) {
                    kickedPlayers.add(keeper);
                    playersKicked++;
                }
                if (playersKicked == playersToKick) {
                    break;
                }
            }
        }

        // Compact the keeper IDs
        List<ClientInfo> keepers = new ArrayList<>(players);
        keepers.sort(Comparator.comparingInt((ClientInfo o) -> o.getKeeper().getId()));
        short id = Player.KEEPER1_ID;
        for (ClientInfo keeper : keepers) {
            if (!kickedPlayers.contains(keeper)) {
                keeper.getKeeper().setId(id);
                id++;
            }
        }

        return kickedPlayers;
    }

}

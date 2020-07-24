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
package toniarts.openkeeper.utils;

import com.badlogic.gdx.math.Vector2;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import toniarts.openkeeper.game.data.ActionPoint;
import toniarts.openkeeper.game.map.MapData;
import toniarts.openkeeper.game.map.MapTile;
import toniarts.openkeeper.world.MapLoader;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Contains transforms from tile indexes and world coordinates
 *
 * @author archdemon
 */
public class WorldUtils {

    private WorldUtils() {
        // Nope
    }

    /**
     * Get a standard camera position vector on given map point
     *
     * @param x tile x coordinate
     * @param y tile y coordinate
     * @return position on 3D world with y = 0
     */
    public static Vector3f pointToVector3f(final int x, final int y) {
        return new Vector3f(x * MapLoader.TILE_WIDTH, 0, y * MapLoader.TILE_WIDTH);
    }

    /**
     * calculates position from center ActionPoint
     *
     * @param ap
     * @return position on 3D world with y = 0
     */
    public static Vector3f ActionPointToVector3f(final ActionPoint ap) {

        return new Vector3f(
                (ap.getStart().x + ap.getEnd().x) / 2.0f * MapLoader.TILE_WIDTH,
                0,
                (ap.getStart().y + ap.getEnd().y) / 2.0f * MapLoader.TILE_WIDTH);
    }

    /**
     * calculates position from center ActionPoint
     *
     * @param ap
     * @return position on 2D world
     */
    public static Vector2f ActionPointToVector2f(final ActionPoint ap) {

        return new Vector2f(
                (ap.getStart().x + ap.getEnd().x) / 2.0f * MapLoader.TILE_WIDTH,
                (ap.getStart().y + ap.getEnd().y) / 2.0f * MapLoader.TILE_WIDTH);
    }

    /**
     *
     * @param p
     * @return position on 3D world with y = 0
     */
    public static Vector3f pointToVector3f(final Point p) {
        return pointToVector3f(p.x, p.y);
    }

    public static Vector2f pointToVector2f(final int x, final int y) {
        return new Vector2f(x * MapLoader.TILE_WIDTH, y * MapLoader.TILE_WIDTH);
    }

    public static Vector2f pointToVector2f(final Point p) {
        return pointToVector2f(p.x, p.y);
    }

    public static Vector2 pointToVector2(final int x, final int y) {
        return new Vector2(x * MapLoader.TILE_WIDTH, y * MapLoader.TILE_WIDTH);
    }

    public static Vector2 pointToVector2(final Point p) {
        return pointToVector2(p.x, p.y);
    }

    public static Point vectorToPoint(final Vector2f v) {
        return vectorToPoint(v.x, v.y);
    }

    public static Point vectorToPoint(final Vector2 v) {
        return vectorToPoint(v.x, v.y);
    }

    public static Point vectorToPoint(final Vector3f v) {
        return vectorToPoint(v.x, v.z);
    }

    public static Point vectorToPoint(final float x, final float y) {
        return new Point(Math.round(x / MapLoader.TILE_WIDTH), Math.round(y / MapLoader.TILE_WIDTH));
    }

    public static Vector2 vector3fToVector2(Vector3f v) {
        return new Vector2(v.x, v.z);
    }

    /**
     * Get surrounding tile coordinates
     *
     * @param mapData the map data
     * @param point starting coordinate whose surroundings you want
     * @param diagonal whether to also include diagonally attached tiles
     * @return surrounding tile coordinates
     */
    public static Point[] getSurroundingTiles(MapData mapData, Point point, boolean diagonal) {

        // Get all surrounding tiles
        List<Point> tileCoords = new ArrayList<>(diagonal ? 9 : 5);
        tileCoords.add(point);

        addIfValidCoordinate(mapData, point.x, point.y - 1, tileCoords); // North
        addIfValidCoordinate(mapData, point.x + 1, point.y, tileCoords); // East
        addIfValidCoordinate(mapData, point.x, point.y + 1, tileCoords); // South
        addIfValidCoordinate(mapData, point.x - 1, point.y, tileCoords); // West
        if (diagonal) {
            addIfValidCoordinate(mapData, point.x - 1, point.y - 1, tileCoords); // NW
            addIfValidCoordinate(mapData, point.x + 1, point.y - 1, tileCoords); // NE
            addIfValidCoordinate(mapData, point.x - 1, point.y + 1, tileCoords); // SW
            addIfValidCoordinate(mapData, point.x + 1, point.y + 1, tileCoords); // SE
        }

        return tileCoords.toArray(new Point[0]);
    }

    private static void addIfValidCoordinate(MapData mapData, final int x, final int y, List<Point> tileCoords) {
        MapTile tile = mapData.getTile(x, y);
        if (tile != null) {
            tileCoords.add(tile.getLocation());
        }
    }

    /**
     * Calculates manhattan distance between two points
     *
     * @param p1 point 1
     * @param p2 point 2
     * @return distance between the to points
     */
    public static int calculateDistance(Point p1, Point p2) {
        if (p1 == null || p2 == null) {
            return Short.MAX_VALUE; // With the points added, int max value would overflow
        }
        return Math.abs(p1.x - p2.x) + Math.abs(p1.y - p2.y);
    }

}

package com.alicornlunaa.spacegame.objects.Planet;

import java.util.HashMap;

import com.alicornlunaa.spacegame.App;
import com.alicornlunaa.spacegame.objects.Entity;
import com.badlogic.gdx.math.Vector2;

/**
 * The World object will hold the data for the world's tiles
 * as well as the how to render the circular planet in space.
 */

public class Planet extends Entity {

    // Variables
    private HashMap<Vector2, Tile> map = new HashMap<>();
    private Vector2 cursor = new Vector2();

    // Constructor
    public Planet(final App game){
        // Initialize a cube for testing
        int initialRad = 15;
        for(int x = -initialRad; x <= initialRad; x++){
            for(int y = -initialRad; y <= initialRad; y++){
                map.put(new Vector2(x, y), new Tile(game, x, y, Tile.TileType.STONE));
            }
        }
    }

    // Functions
    public Tile getTile(int x, int y){
        cursor.set(x, y);
        return map.get(cursor);
    }

    public HashMap<Vector2, Tile> getMap(){
        return map;
    }
    
}

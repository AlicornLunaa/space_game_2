package com.alicornlunaa.spacegame.objects.Planet;

import java.util.HashMap;

import com.alicornlunaa.spacegame.App;
import com.alicornlunaa.spacegame.objects.Entity;
import com.alicornlunaa.spacegame.util.OpenSimplexNoise;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;

/**
 * The World object will hold the data for the world's tiles
 * as well as the how to render the circular planet in space.
 */

public class Planet extends Entity {

    // Variables
    private HashMap<Vector2, Chunk> map = new HashMap<>();
    private Vector2 cursor = new Vector2();
    private final OpenSimplexNoise noise;
    private final long seed = 123;

    private Body worldBody;

    // Constructor
    public Planet(final App game, final World world){
        // Create noise function
        noise = new OpenSimplexNoise(seed);

        // Create body
        BodyDef def = new BodyDef();
        def.type = BodyType.StaticBody;
        def.position.set(0, 0);
        worldBody = world.createBody(def);

        // Initialize a cube for testing
        int initialRad = 2;
        for(int x = -initialRad; x <= initialRad; x++){
            for(int y = -initialRad; y <= initialRad; y++){
                map.put(new Vector2(x, y), new Chunk(game, worldBody, noise, x, y));
            }
        }
    }

    // Functions
    public Chunk getChunk(int x, int y){
        cursor.set(x, y);
        return map.get(cursor);
    }

    public Tile getTile(int x, int y){
        // Gets the tile from the world position
        cursor.set(x / Chunk.CHUNK_SIZE, y / Chunk.CHUNK_SIZE); // Get chunk of the block
        return map.get(cursor).getTile(x, y);
    }

    public HashMap<Vector2, Chunk> getMap(){
        return map;
    }
    
}

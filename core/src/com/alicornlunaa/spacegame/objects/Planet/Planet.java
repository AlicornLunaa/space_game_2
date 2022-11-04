package com.alicornlunaa.spacegame.objects.Planet;

import java.util.HashMap;

import com.alicornlunaa.spacegame.App;
import com.alicornlunaa.spacegame.objects.Entity;
import com.alicornlunaa.spacegame.util.OpenSimplexNoise;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;

/**
 * The World object will hold the data for the world's tiles
 * as well as the how to render the circular planet in space.
 */

public class Planet extends Entity {

    // Variables
    private final App game;
    private final World world;

    private float size;

    private HashMap<Vector2, Chunk> map = new HashMap<>();
    private Vector2 cursor = new Vector2();
    private final OpenSimplexNoise noise;
    private final long seed = 123;

    // Constructor
    public Planet(final App game, final World world, float size){
        super();
        this.game = game;
        this.world = world;

        this.size = size;

        // Update world
        world.setGravity(new Vector2(0, 600 * getGravityScale()));

        // Create noise function
        noise = new OpenSimplexNoise(seed);

        // Initialize a cube for testing
        int initialRad = 3;
        for(int x = -initialRad; x <= initialRad; x++){
            for(int y = -initialRad; y <= initialRad; y++){
                map.put(new Vector2(x, y), new Chunk(game, world, noise, x, y));
            }
        }
    }

    // Functions
    public float getGravityScale(){ return size / -1000.0f; }

    // Chunking functions
    public Chunk createChunk(int x, int y){
        Chunk c = new Chunk(game, world, noise, x, y);
        cursor.set(x, y);
        map.put(new Vector2(x, y), c);

        return c;
    }

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

package com.alicornlunaa.spacegame.objects.Planet;

import java.util.HashMap;

import com.alicornlunaa.spacegame.App;
import com.alicornlunaa.spacegame.objects.Entity;
import com.alicornlunaa.spacegame.states.PlanetState;
import com.alicornlunaa.spacegame.util.OpenSimplexNoise;
import com.badlogic.gdx.graphics.g2d.Batch;
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

    private PlanetState state = new PlanetState();

    private HashMap<Vector2, Chunk> map = new HashMap<>();
    private Vector2 cursor = new Vector2();
    private final OpenSimplexNoise noise;
    private final long seed = 123;

    // Constructor
    public Planet(final App game, final World world, float radius){
        super();
        this.game = game;
        this.world = world;

        // Update world
        world.setGravity(new Vector2(0, 600 * getGravityScale()));

        // Create noise function
        noise = new OpenSimplexNoise(seed);

        // Initialize a cube for testing
        int initialRad = 3;
        for(int x = -initialRad; x <= initialRad; x++){
            for(int y = -initialRad; y <= initialRad; y++){
                map.put(new Vector2(x, y), new Chunk(game, world, noise, state, x, y));
            }
        }
    }

    // Functions
    public float getGravityScale(){ return state.radius / -1000.0f; }

    @Override
    public void draw(Batch batch, float parentAlpha){
        // Draw each chunk rotated around, theta = x, r = y
        for(Chunk chunk : map.values()){
            for(Tile[] ySlice : chunk.getTiles()){
                for(Tile tile : ySlice){
                    if(tile == null) continue;
                    float worldX = tile.getX() * Tile.TILE_SIZE;
                    float worldY = tile.getY() * Tile.TILE_SIZE;

                    // Convert X to a 0-1 value representing the entire circumference
                    float theta = worldX / (2 * (float)Math.PI * state.radius);
                    float height = Math.abs(worldY) / state.radius;

                    // Convert to X and Y positions on the circle edge
                    float circX = (float)Math.cos(Math.toRadians(theta * 360)) * state.radius * height;
                    float circY = (float)Math.sin(Math.toRadians(theta * 360)) * state.radius * height;

                    // Draw sprite to position
                    tile.getSprite().draw(
                        batch,
                        circX,
                        circY,
                        Tile.TILE_SIZE / 2,
                        Tile.TILE_SIZE / 2,
                        Tile.TILE_SIZE,
                        Tile.TILE_SIZE,
                        0.75f + height,
                        0.75f + height,
                        theta * 360 - 90
                    );
                }
            }
        }
    }

    // Chunking functions
    public Chunk createChunk(int x, int y){
        Chunk c = new Chunk(game, world, noise, state, x, y);
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

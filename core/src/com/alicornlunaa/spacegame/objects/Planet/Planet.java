package com.alicornlunaa.spacegame.objects.Planet;

import java.util.ArrayList;
import java.util.HashMap;

import com.alicornlunaa.spacegame.App;
import com.alicornlunaa.spacegame.objects.Entity;
import com.alicornlunaa.spacegame.states.PlanetState;
import com.alicornlunaa.spacegame.util.Constants;
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

    public PlanetState state;

    private HashMap<Vector2, Chunk> map = new HashMap<>();
    private TerrainGenerator generator;
    private Vector2 cursor = new Vector2();

    private ArrayList<Entity> planetEnts = new ArrayList<>();

    // Constructor
    public Planet(final App game, final World world, PlanetState state, float physScale){
        super();
        this.game = game;
        this.world = world;
        this.state = state;
        setPhysScale(physScale);

        // Initialize generator
        generator = new TerrainGenerator(game, state.seed, (int)(2 * Math.PI * state.radius / Chunk.CHUNK_SIZE / Tile.TILE_SIZE), (int)(state.radius / Chunk.CHUNK_SIZE / Tile.TILE_SIZE));

        // Update world
        world.setGravity(new Vector2(0, -1 * getGravityScale() / Constants.PLANET_PPM * 0));

        // Initialize a cube for testing
        int initialRad = 3;
        for(int x = -initialRad; x <= initialRad; x++){
            for(int y = -initialRad; y <= initialRad; y++){
                map.put(new Vector2(x, y), new Chunk(game, world, generator, state, x, y));
            }
        }
    }

    // Functions
    public float getGravityScale(){ return (SpacePlanet.GRAVITY_CONSTANT * state.mass) / (state.radius * state.radius); }

    public TerrainGenerator getGenerator(){ return generator; }

    /**
     * Converts the entity to 2d planet planar coordinates
     * @param e Entity to be converted
     */
    public void addEntity(Entity e){
        // Formula: x = theta, y = radius
        Vector2 localPos = e.getPosition().sub(state.position);
        float worldWidthUnits = generator.getWidth() * Chunk.CHUNK_SIZE * Tile.TILE_SIZE;
        float x = (localPos.angleDeg() / 360 * worldWidthUnits);
        float y = localPos.len();

        e.setPosition(x, y);
        planetEnts.add(e);
    }

    /**
     * Convert the entity to the space planet circular coordinates
     * @param e Entity to be converted
     */
    public void delEntity(Entity e){
        // Formula: theta = x, radius = y
        float worldWidthUnits = generator.getWidth() * Chunk.CHUNK_SIZE * Tile.TILE_SIZE;
        double theta = ((e.getX() / worldWidthUnits) * Math.PI * 2);
        float radius = e.getY();

        float x = (float)(Math.cos(theta) * radius) + state.position.x;
        float y = (float)(Math.sin(theta) * radius) + state.position.y;

        e.setPosition(x, y);
        planetEnts.remove(e);
    }

    public ArrayList<Entity> getEntities(){ return planetEnts; }

    @Override
    public void draw(Batch batch, float parentAlpha){
        // Draw each chunk rotated around, theta = x, r = y
        for(Chunk chunk : map.values()){
            chunk.draw(batch);
        }

        // Draw every entity
        for(Entity e : planetEnts){
            e.draw(batch, parentAlpha);
        }
    }

    // Chunking functions
    public Chunk createChunk(int x, int y){
        Chunk c = new Chunk(game, world, generator, state, x, y);
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

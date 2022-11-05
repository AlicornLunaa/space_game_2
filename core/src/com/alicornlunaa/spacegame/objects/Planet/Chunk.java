package com.alicornlunaa.spacegame.objects.Planet;

import com.alicornlunaa.spacegame.App;
import com.alicornlunaa.spacegame.objects.Planet.Tile.TileType;
import com.alicornlunaa.spacegame.util.OpenSimplexNoise;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;

/**
 * Memory management helper class, break the world into chunks
 * and only use active chunks for physics updates, also stream
 * chunks as theyre needed.
 */
public class Chunk {

    // Static
    public static final int CHUNK_SIZE = 16; // Block count in a square for a chunk
    public static final int ACTIVE_DISTANCE = 1; // Chunks to render and generate
    public static final int RENDER_DISTANCE = 4; // Chunks to render and generate

    // Variables
    private int chunkX;
    private int chunkY;
    private boolean active = false; // If the chunk should get updates
    private Tile[][] map;

    private Body body;

    // Private functions
    private void generate(final App game, final OpenSimplexNoise noise){
        // Generate a tile for each chunk
        for(int x = 0; x < CHUNK_SIZE; x++){
            for(int y = 0; y < CHUNK_SIZE; y++){
                TileType type = TileType.STONE;
                int blockX = x + (chunkX * CHUNK_SIZE);
                int blockY = y + (chunkY * CHUNK_SIZE);

                if((blockX * blockX) + (blockY * blockY) > 10000.0f) continue;
                if(noise.eval(blockX / 10.0f, blockY / 10.0f) < 0) continue;

                map[x][y] = new Tile(game, body, blockX, blockY, chunkX, chunkY, type);
            }
        }
    }

    // Constructor
    public Chunk(final App game, final World world, final OpenSimplexNoise noise, int chunkX, int chunkY){
        this.chunkX = chunkX;
        this.chunkY = chunkY;

        // Create chunk body
        float wSize = Tile.TILE_SIZE * CHUNK_SIZE;

        BodyDef def = new BodyDef();
        def.type = BodyType.StaticBody;
        def.position.set(wSize - wSize, wSize - wSize);
        body = world.createBody(def);
        body.setActive(active);

        // Generate tile map
        map = new Tile[CHUNK_SIZE][CHUNK_SIZE];
        generate(game, noise);
    }

    // Functions
    public Vector2 blockToChunk(Vector2 v){ return new Vector2(v.x % CHUNK_SIZE, v.y % CHUNK_SIZE); }
    public Vector2 chunkToBlock(Vector2 v){ return new Vector2(v.x + (chunkX * CHUNK_SIZE), v.y + (chunkY * CHUNK_SIZE)); }
    public Tile getTileLocal(int x, int y){ return map[x][y]; }
    public Tile getTile(int x, int y){ return map[x % CHUNK_SIZE][y % CHUNK_SIZE]; }
    public Vector2 getChunkPos(){ return new Vector2(chunkX, chunkY); }
    public boolean isActive(){ return active; }
    public void setActive(boolean a){ active = a; }

    public void update(float delta){
        body.setActive(active);
        
        if(!active) return;

        for(Tile[] ySlice : map){
            for(Tile tile : ySlice){
                if(tile == null) continue;

                tile.update(delta);
            }
        }

        active = false;
    }

    public void draw(Batch batch){
        for(Tile[] ySlice : map){
            for(Tile tile : ySlice){
                if(tile == null) continue;
                
                tile.draw(batch);
            }
        }
    }

    public void dispose(){
        for(Tile[] ySlice : map){
            for(Tile tile : ySlice){
                tile.dispose();
            }
        }
    }
    
}

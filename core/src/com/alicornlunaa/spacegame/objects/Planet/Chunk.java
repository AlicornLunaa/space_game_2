package com.alicornlunaa.spacegame.objects.Planet;

import com.alicornlunaa.spacegame.App;
import com.alicornlunaa.spacegame.objects.Planet.Tile.TileType;
import com.alicornlunaa.spacegame.util.OpenSimplexNoise;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Vector2;

/**
 * Memory management helper class, break the world into chunks
 * and only use active chunks for physics updates, also stream
 * chunks as theyre needed.
 */
public class Chunk {

    // Static
    public static final int CHUNK_SIZE = 16; // Block count in a square for a chunk

    // Variables
    private int chunkX;
    private int chunkY;
    private boolean active = true; // If the chunk should get updates
    private Tile[][] map;

    // Private functions
    private void generate(final App game, final OpenSimplexNoise noise){
        // Generate a tile for each chunk
        for(int x = 0; x < CHUNK_SIZE; x++){
            for(int y = 0; y < CHUNK_SIZE; y++){
                TileType type = TileType.STONE;
                int blockX = x + (chunkX * CHUNK_SIZE);
                int blockY = y + (chunkY * CHUNK_SIZE);

                if(noise.eval(blockX / 10.0f, blockY / 10.0f) < 0) type = TileType.DIRT;
                if(blockY > 0) continue;

                map[x][y] = new Tile(game, blockX, blockY, chunkX, chunkY, type);
            }
        }
    }

    // Constructor
    public Chunk(final App game, final OpenSimplexNoise noise, int chunkX, int chunkY){
        this.chunkX = chunkX;
        this.chunkY = chunkY;

        map = new Tile[CHUNK_SIZE][CHUNK_SIZE];
        generate(game, noise);
    }

    // Functions
    public Vector2 worldToChunk(Vector2 v){ return new Vector2(v.x % CHUNK_SIZE, v.y % CHUNK_SIZE); }
    public Vector2 chunkToWorld(Vector2 v){ return new Vector2(v.x + (chunkX * CHUNK_SIZE), v.y + (chunkY * CHUNK_SIZE)); }
    public Tile getTileLocal(int x, int y){ return map[x][y]; }
    
    public Tile getTile(int x, int y){ return map[x % CHUNK_SIZE][y % CHUNK_SIZE]; }

    public void update(float delta){
        if(!active) return;

        for(Tile[] ySlice : map){
            for(Tile tile : ySlice){
                if(tile == null) continue;

                tile.update(delta);
            }
        }
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

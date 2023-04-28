package com.alicornlunaa.spacegame.objects.planet;

import com.alicornlunaa.spacegame.App;
import com.alicornlunaa.spacegame.engine.phys.PhysWorld;
import com.alicornlunaa.spacegame.objects.blocks.Tile;
import com.alicornlunaa.spacegame.objects.planet.terrain.Chunk;
import com.alicornlunaa.spacegame.util.Constants;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.utils.Array;

/**
 * Holds world chunks and manages which is loaded or not
 */
public class WorldBody extends Group {

    // Variables
    private final App game;
    private Chunk[][] chunks;
    private Array<Chunk> loadedChunks = new Array<>();

    // Constructor
    public WorldBody(final App game, PhysWorld world, int width, int height){
        this.setTransform(false);
        this.game = game;
        chunks = new Chunk[width][height];

        // Generate everything as a test
        for(int y = 0; y < height; y++) for(int x = 0; x < width; x++){
            chunks[x][y] = new Chunk(game, world, x, y);
            this.addActor(chunks[x][y]);
        }
    }

    // Functions
    public void update(){
        // Update based on the player's positioning and time active
        Vector2 plyPos = game.player.getCenter();
        
        // Get chunk coordinates for the player
        int loadDist = Constants.CHUNK_LOAD_DISTANCE;
        int plyChunkX = Math.floorMod((int)(plyPos.x / Chunk.CHUNK_SIZE / Tile.TILE_SIZE) + 1, chunks.length);
        int plyChunkY = Math.floorMod((int)(plyPos.y / Chunk.CHUNK_SIZE / Tile.TILE_SIZE) + 1, chunks[0].length);

        // Iterate over loaded chunks and unload if they are out of range
        for(int i = 0; i < loadedChunks.size; i++){
            Chunk chunk = loadedChunks.get(i);
            int currentChunkX = chunk.getChunkX();
            int currentChunkY = chunk.getChunkY();
            int distance = Math.max(Math.abs(plyChunkX - currentChunkX), Math.abs(plyChunkY - currentChunkY));
            
            // Unload if too far
            if(distance > loadDist * 2){
                loadedChunks.removeIndex(i);
                chunk.unload();
                i--;
            }
        }

        // Iterate over the players surrounding chunks and load them
        for(int y = loadDist * -1; y < loadDist; y++){
            for(int x = loadDist * -1; x < loadDist; x++){
                Chunk chunk = chunks[Math.floorMod(plyChunkX + x, chunks.length)][Math.floorMod(plyChunkY + y, chunks[0].length)];

                // Load if not loaded
                if(!chunk.isLoaded()){
                    loadedChunks.add(chunk);
                    chunk.load();
                }
            }
        }
    }

    @Override
    public void draw(Batch batch, float a){
        Matrix4 trans = batch.getTransformMatrix().cpy();
        super.draw(batch, a);
        batch.setTransformMatrix(trans);
    }
    
}

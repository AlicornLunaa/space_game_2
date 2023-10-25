package com.alicornlunaa.spacegame.objects.simulation.cellular;

import com.alicornlunaa.selene_engine.core.BaseEntity;
import com.alicornlunaa.spacegame.util.Constants;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.utils.Null;

// CellWorld stores and updates all the tiles in a falling sand simulation
// type of algorithm. This will break the ECS in order to keep up speedy
// interactions.
public class CellWorld extends BaseEntity {
    // Variables
    public final int width;
    public final int height;
    private CellChunk[][] chunks;
    private boolean[][] updateMap;

    // Constructor
    public CellWorld(int width, int height){
        chunks = new CellChunk[width][height];
        updateMap = new boolean[width * Constants.CHUNK_SIZE][height * Constants.CHUNK_SIZE];

        this.width = width;
        this.height = height;

        for(int y = 0; y < height; y++) for(int x = 0; x < width; x++){
            chunks[x][y] = new CellChunk(x, y);
        }

        for(int y = 0; y < height * Constants.CHUNK_SIZE; y++) for(int x = 0; x < width * Constants.CHUNK_SIZE; x++){
            updateMap[x][y] = false;
        }
    }

    // Functions
    public boolean inBounds(int x, int y){
        return !(x < 0 || y < 0 || x >= Constants.CHUNK_SIZE * width || y >= Constants.CHUNK_SIZE * height);
    }

    public void setTile(int x, int y, @Null CellBase tile){
        if(tile != null)
            tile.setPosition(x, y);

        int chunkX = x / Constants.CHUNK_SIZE;
        int chunkY = y / Constants.CHUNK_SIZE;
        CellChunk chunk = chunks[chunkX][chunkY];

        if(chunk != null)
            chunk.setTile(x, y, tile);
    }

    public @Null CellBase getTile(int x, int y){
        // Guard clauses
        if(!inBounds(x, y)) return null;

        // Get chunk the tile is in
        int chunkX = x / Constants.CHUNK_SIZE;
        int chunkY = y / Constants.CHUNK_SIZE;
        CellChunk chunk = chunks[chunkX][chunkY];

        // Return tile
        if(chunk != null)
            return chunk.getTile(x, y);

        return null;
    }

    public void setVisited(int x, int y){
        updateMap[x][y] = true;
    }

    public boolean getVisited(int x, int y){
        return updateMap[x][y];
    }

    public void resetMap(){
        for(int y = 0; y < height * Constants.CHUNK_SIZE; y++) for(int x = 0; x < width * Constants.CHUNK_SIZE; x++){
            updateMap[x][y] = false;
        }
    }

    public void step(){
        for(CellChunk[] chunkColumn : chunks) for(CellChunk chunk : chunkColumn){
            if(chunk == null) continue;
            chunk.step(this);
        }

        for(CellChunk[] chunkColumn : chunks) for(CellChunk chunk : chunkColumn){
            if(chunk == null) continue;
            chunk.commitActions(this);
        }

        resetMap();
    }

    public void draw(Batch batch){
        for(CellChunk[] chunkColumn : chunks) for(CellChunk chunk : chunkColumn){
            if(chunk == null) continue;
            chunk.draw(batch);
        }
    }
}

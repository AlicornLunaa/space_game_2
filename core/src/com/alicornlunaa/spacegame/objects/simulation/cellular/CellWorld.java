package com.alicornlunaa.spacegame.objects.simulation.cellular;

import com.alicornlunaa.selene_engine.core.BaseEntity;
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

    // Constructor
    public CellWorld(int width, int height){
        chunks = new CellChunk[width][height];

        this.width = width;
        this.height = height;

        for(int y = 0; y < height; y++) for(int x = 0; x < width; x++){
            chunks[x][y] = new CellChunk(x, y);
        }
    }

    // Functions
    public void setTile(int x, int y, @Null CellBase tile){
        
    }

    public @Null CellBase getTile(int x, int y){
        return null;
    }

    public void draw(Batch batch){
        for(CellChunk[] chunkColumn : chunks) for(CellChunk chunk : chunkColumn){
            if(chunk == null) continue;
            chunk.draw(batch);
        }
    }
}

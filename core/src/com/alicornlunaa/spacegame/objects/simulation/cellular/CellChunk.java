package com.alicornlunaa.spacegame.objects.simulation.cellular;

import com.alicornlunaa.spacegame.util.Constants;
import com.badlogic.gdx.graphics.g2d.Batch;

public class CellChunk {
    // Variables
    public final int chunkX;
    public final int chunkY;
    public boolean loaded = false;

    private CellBase[] tiles = new CellBase[Constants.CHUNK_SIZE * Constants.CHUNK_SIZE];

    // Private functions
    private int getIndex(int x, int y){
        return x + y * Constants.CHUNK_SIZE;
    }

    // Constructor
    public CellChunk(int x, int y){
        chunkX = x;
        chunkY = y;

        tiles[0] = new CellBase("stone");
    }

    // Functions
    public void draw(Batch batch){
        for(int i = 0; i < tiles.length; i++){
            int x = i % Constants.CHUNK_SIZE;
            int y = i / Constants.CHUNK_SIZE;
            CellBase tile = tiles[i];

            if(tile == null) continue;

            batch.draw(
                tile.texture,
                (x + chunkX * Constants.CHUNK_SIZE) * Constants.TILE_SIZE, (y + chunkY * Constants.CHUNK_SIZE) * Constants.TILE_SIZE,
                0, 0,
                Constants.TILE_SIZE, Constants.TILE_SIZE,
                1, 1,
                0
            );
        }
    }
}

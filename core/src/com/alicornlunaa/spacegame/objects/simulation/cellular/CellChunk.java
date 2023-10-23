package com.alicornlunaa.spacegame.objects.simulation.cellular;

import com.alicornlunaa.spacegame.objects.simulation.cellular.actions.Action;
import com.alicornlunaa.spacegame.util.Constants;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Null;

public class CellChunk {
    // Variables
    public final int chunkX;
    public final int chunkY;
    public boolean loaded = false;

    private Array<Action> changes = new Array<>();
    private CellBase[] tiles = new CellBase[Constants.CHUNK_SIZE * Constants.CHUNK_SIZE];

    // Private functions
    private int getIndex(int x, int y){
        return (x - chunkX * Constants.CHUNK_SIZE) + (y - chunkY * Constants.CHUNK_SIZE) * Constants.CHUNK_SIZE;
    }

    // Constructor
    public CellChunk(int x, int y){
        chunkX = x;
        chunkY = y;
    }

    // Functions
    public void setTile(int x, int y, @Null CellBase tile){
        tiles[getIndex(x, y)] = tile;
    }

    public @Null CellBase getTile(int x, int y){
        return tiles[getIndex(x, y)];
    }

    public void step(CellWorld world){
        for(int i = 0; i < tiles.length; i++){
            if(tiles[i] != null)
                tiles[i].step(world, changes);
        }

        commitActions(world);
    }

    public void commitActions(CellWorld world){
        while(changes.size > 0){
            changes.pop().commit(world);
        }
    }

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

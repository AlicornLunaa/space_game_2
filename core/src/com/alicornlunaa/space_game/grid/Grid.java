package com.alicornlunaa.space_game.grid;

import java.util.HashMap;

import com.alicornlunaa.space_game.grid.tiles.AbstractTile;
import com.alicornlunaa.space_game.util.Constants;
import com.alicornlunaa.space_game.util.Vector2i;
import com.badlogic.gdx.utils.Null;

// Storage class for tiles
public class Grid {
    // Static classes
    private static class Chunk {
        // Variables
        public final int chunkX, chunkY; // World position of chunks
        private boolean[] occupancyMap; // Whether or not the current tile is occupied
        private AbstractTile[] tileMap; // Actual tiles being stored

        // Constructor
        public Chunk(int chunkX, int chunkY){
            this.chunkX = chunkX;
            this.chunkY = chunkY;
            occupancyMap = new boolean[Constants.CHUNK_SIZE * Constants.CHUNK_SIZE];
            tileMap = new AbstractTile[Constants.CHUNK_SIZE * Constants.CHUNK_SIZE];
        }

        // Functions
        public boolean isOccupied(int x, int y){
            return occupancyMap[y * Constants.CHUNK_SIZE + x];
        }

        public void setTile(int x, int y, AbstractTile tile){
            // TODO: Take tile width, height, and rotation into account when setting occupancy and removing old tiles
            occupancyMap[y * Constants.CHUNK_SIZE + x] = true;
            tileMap[y * Constants.CHUNK_SIZE + x] = tile;
        }

        public void removeTile(int x, int y){
            // TODO: Take tile width, height, and rotation into account when setting occupancy and removing old tiles
            occupancyMap[y * Constants.CHUNK_SIZE + x] = false;
            tileMap[y * Constants.CHUNK_SIZE + x] = null;
        }

        public @Null AbstractTile getTile(int x, int y){
            return tileMap[y * Constants.CHUNK_SIZE + x];
        }
    };

    // Variables
    private HashMap<Vector2i, Chunk> chunks = new HashMap<>();
    
    // Constructor
    public Grid(){

    }

    // Functions

}

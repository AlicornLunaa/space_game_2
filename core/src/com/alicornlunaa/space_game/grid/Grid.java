package com.alicornlunaa.space_game.grid;

import java.util.HashMap;

import com.alicornlunaa.space_game.grid.tiles.AbstractTile;
import com.alicornlunaa.space_game.util.Constants;
import com.alicornlunaa.space_game.util.Vector2i;
import com.badlogic.gdx.utils.Null;

// Storage class for tiles
public class Grid {
    // Static classes
    @SuppressWarnings("unused")
    private static class Chunk {
        // Variables
        public final int chunkX, chunkY; // World position of chunks
        private boolean[] occupancyMap; // Whether or not the current tile is occupied
        private AbstractTile[] tileMap; // Actual tiles being stored
        private int tileCount = 0;

        // Constructor
        public Chunk(int chunkX, int chunkY){
            this.chunkX = chunkX;
            this.chunkY = chunkY;
            occupancyMap = new boolean[Constants.CHUNK_SIZE * Constants.CHUNK_SIZE];
            tileMap = new AbstractTile[Constants.CHUNK_SIZE * Constants.CHUNK_SIZE];
        }

        // Functions
        public int getTileCount(){
            return tileCount;
        }

        public boolean isOccupied(int x, int y){
            return occupancyMap[y * Constants.CHUNK_SIZE + x];
        }

        public void setTile(int x, int y, AbstractTile tile){
            occupancyMap[y * Constants.CHUNK_SIZE + x] = true;
            tileMap[y * Constants.CHUNK_SIZE + x] = tile;
            tileCount++;
        }

        public void removeTile(int x, int y){
            occupancyMap[y * Constants.CHUNK_SIZE + x] = false;
            tileMap[y * Constants.CHUNK_SIZE + x] = null;
            tileCount--;
        }

        public @Null AbstractTile getTile(int x, int y){
            return tileMap[y * Constants.CHUNK_SIZE + x];
        }
    };

    public static interface GridIterator {
        public void iterate(AbstractTile tile);
    }

    // Variables
    private HashMap<Vector2i, Chunk> chunks = new HashMap<>();
    private Vector2i selection = new Vector2i();
    
    // Constructor
    public Grid(){
    }

    // Functions
    private Chunk newChunkFromWorld(int x, int y){
        int chunkX = Math.floorDiv(x, Constants.CHUNK_SIZE);
        int chunkY = Math.floorDiv(y, Constants.CHUNK_SIZE);
        Chunk chunk = chunks.get(selection.set(chunkX, chunkY));

        if(chunk == null){
            chunk = new Chunk(chunkX, chunkY);
            chunks.put(selection, chunk);
        }

        return chunk;
    }

    private @Null Chunk getChunkFromWorld(int x, int y){
        int chunkX = Math.floorDiv(x, Constants.CHUNK_SIZE);
        int chunkY = Math.floorDiv(y, Constants.CHUNK_SIZE);
        return chunks.get(selection.set(chunkX, chunkY));
    }

    private void pruneChunkFromWorld(int x, int y){
        int chunkX = Math.floorDiv(x, Constants.CHUNK_SIZE);
        int chunkY = Math.floorDiv(y, Constants.CHUNK_SIZE);
        Chunk chunk = chunks.get(selection.set(chunkX, chunkY));

        if(chunk != null && chunk.getTileCount() <= 0){
            chunks.remove(selection.set(chunkX, chunkY));
        }
    }

    public boolean isOccupied(int x, int y){
        Chunk chunk = getChunkFromWorld(x, y);

        if(chunk != null)
            return chunk.isOccupied(
                Math.floorMod(x, Constants.CHUNK_SIZE),
                Math.floorMod(y, Constants.CHUNK_SIZE)
            );

        return false;
    }

    public boolean isRegionOccupied(int x, int y, int rotation, int width, int height){
        for(int w = 0; w < width; w++){
            for(int h = 0; h < height; h++){
                switch(rotation){
                    default:
                        if(isOccupied(x + w, y + h))
                            return true;

                    case 1:
                        if(isOccupied(x + h, y - w))
                            return true;

                    case 2:
                        if(isOccupied(x - w, y - h))
                            return true;
                            
                    case 3:
                        if(isOccupied(x - h, y + w))
                            return true;
                }
            }
        }

        return false;
    }

    public boolean setTile(int x, int y, AbstractTile tile){
        Chunk chunk;

        if(isRegionOccupied(x, y, tile.rotation, tile.width, tile.height))
            return false;

        for(int w = 0; w < tile.width; w++){
            for(int h = 0; h < tile.height; h++){
                switch(tile.rotation){
                    default:
                        chunk = newChunkFromWorld(x + w, y + h);
                        chunk.setTile(Math.floorMod(x + w, Constants.CHUNK_SIZE), Math.floorMod(y + h, Constants.CHUNK_SIZE), tile);
                        break;

                    case 1:
                        chunk = newChunkFromWorld(x + h, y - w);
                        chunk.setTile(Math.floorMod(x + h, Constants.CHUNK_SIZE), Math.floorMod(y - w, Constants.CHUNK_SIZE), tile);
                        break;

                    case 2:
                        chunk = newChunkFromWorld(x - w, y - h);
                        chunk.setTile(Math.floorMod(x - w, Constants.CHUNK_SIZE), Math.floorMod(y - h, Constants.CHUNK_SIZE), tile);
                        break;
                            
                    case 3:
                        chunk = newChunkFromWorld(x - h, y + w);
                        chunk.setTile(Math.floorMod(x - h, Constants.CHUNK_SIZE), Math.floorMod(y + w, Constants.CHUNK_SIZE), tile);
                        break;
                }
            }
        }

        return true;
    }

    public void removeTile(int x, int y){
        AbstractTile tile = getTile(x, y);
        Chunk chunk;

        x = tile.x;
        y = tile.y;

        for(int w = 0; w < tile.width; w++){
            for(int h = 0; h < tile.height; h++){
                switch(tile.rotation){
                    default:
                        chunk = newChunkFromWorld(x + w, y + h);
                        chunk.removeTile(Math.floorMod(x + w, Constants.CHUNK_SIZE), Math.floorMod(y + h, Constants.CHUNK_SIZE));
                        pruneChunkFromWorld(x + w, y + h);
                        break;

                    case 1:
                        chunk = newChunkFromWorld(x + h, y - w);
                        chunk.removeTile(Math.floorMod(x + h, Constants.CHUNK_SIZE), Math.floorMod(y - w, Constants.CHUNK_SIZE));
                        pruneChunkFromWorld(x + h, y - w);
                        break;

                    case 2:
                        chunk = newChunkFromWorld(x - w, y - h);
                        chunk.removeTile(Math.floorMod(x - w, Constants.CHUNK_SIZE), Math.floorMod(y - h, Constants.CHUNK_SIZE));
                        pruneChunkFromWorld(x - w, y - h);
                        break;
                            
                    case 3:
                        chunk = newChunkFromWorld(x - h, y + w);
                        chunk.removeTile(Math.floorMod(x - h, Constants.CHUNK_SIZE), Math.floorMod(y + w, Constants.CHUNK_SIZE));
                        pruneChunkFromWorld(x - h, y + w);
                        break;
                }
            }
        }
    }

    public @Null AbstractTile getTile(int x, int y){
        Chunk chunk = getChunkFromWorld(x, y);

        if(chunk != null)
            return chunk.getTile(
                Math.floorMod(x, Constants.CHUNK_SIZE),
                Math.floorMod(y, Constants.CHUNK_SIZE)
            );

        return null;
    }

    public void iterate(GridIterator iter){
        for (Chunk chunk : chunks.values()) {
            for(AbstractTile tile : chunk.tileMap){
                if(tile == null)
                    continue;

                iter.iterate(tile);
            }
        }
    }
}

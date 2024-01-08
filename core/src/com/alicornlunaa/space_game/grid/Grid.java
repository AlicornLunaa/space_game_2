package com.alicornlunaa.space_game.grid;

import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONObject;

import com.alicornlunaa.selene_engine.ecs.BodyComponent;
import com.alicornlunaa.selene_engine.phys.Collider;
import com.alicornlunaa.space_game.grid.tiles.AbstractTile;
import com.alicornlunaa.space_game.grid.tiles.TileElement;
import com.alicornlunaa.space_game.grid.tiles.TileEntity;
import com.alicornlunaa.space_game.util.Constants;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
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
            return occupancyMap[y * Constants.CHUNK_SIZE + x] == true;
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
    private HashMap<String, Chunk> chunks = new HashMap<>();
    private Array<Collider> colliders = new Array<>();

    public String gridName = "unnamed_grid";
    
    // Constructor
    public Grid(){
    }

    // Functions
    private Chunk newChunkFromWorld(int x, int y){
        int chunkX = Math.floorDiv(x, Constants.CHUNK_SIZE);
        int chunkY = Math.floorDiv(y, Constants.CHUNK_SIZE);
        Chunk chunk = chunks.get(chunkX + ":" + chunkY);

        if(chunk == null){
            chunk = new Chunk(chunkX, chunkY);
            chunks.put(chunkX + ":" + chunkY, chunk);
        }

        return chunk;
    }

    private @Null Chunk getChunkFromWorld(int x, int y){
        int chunkX = Math.floorDiv(x, Constants.CHUNK_SIZE);
        int chunkY = Math.floorDiv(y, Constants.CHUNK_SIZE);
        return chunks.get(chunkX + ":" + chunkY);
    }

    private void pruneChunkFromWorld(int x, int y){
        int chunkX = Math.floorDiv(x, Constants.CHUNK_SIZE);
        int chunkY = Math.floorDiv(y, Constants.CHUNK_SIZE);
        Chunk chunk = chunks.get(chunkX + ":" + chunkY);

        if(chunk != null && chunk.getTileCount() <= 0){
            chunks.remove(chunkX + ":" + chunkY);
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
                        break;

                    case 1:
                        if(isOccupied(x + h, y - w))
                            return true;
                        break;

                    case 2:
                        if(isOccupied(x - w, y - h))
                            return true;
                        break;
                            
                    case 3:
                        if(isOccupied(x - h, y + w))
                            return true;
                        break;
                }
            }
        }

        return false;
    }

    public boolean setTile(int x, int y, AbstractTile tile){
        Chunk chunk;

        if(isRegionOccupied(x, y, tile.rotation, tile.width, tile.height))
            return false;

        tile.x = x;
        tile.y = y;

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

        if(tile == null)
            return;

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

    public void disassemble(){
        // Build the colliders onto the body component listed
        for(Collider collider : colliders){
            collider.detach();
        }

        colliders.clear();
    }

    public void assemble(BodyComponent bodyComponent){
        // Build the colliders onto the body component listed
        disassemble();
        
        Vector2 v = new Vector2();
        bodyComponent.clearColliders();

        for (Chunk chunk : chunks.values()) {
            for(AbstractTile tile : chunk.tileMap){
                Collider collider = null;

                if(tile == null)
                    continue;

                if(tile instanceof TileElement)
                    collider = new Collider(((TileElement)tile).shape.collider);

                if(tile instanceof TileEntity && ((TileEntity)tile).collider != null)
                    collider = ((TileEntity)tile).collider;
                    
                if(collider == null)
                    continue;

                collider.setPosition(v.set(tile.x * Constants.TILE_SIZE + Constants.TILE_SIZE / 2.f, tile.y * Constants.TILE_SIZE + Constants.TILE_SIZE / 2.f));
                collider.setOrigin(v.set(Constants.TILE_SIZE * (tile.width - 1) / 2.f, Constants.TILE_SIZE * (tile.height - 1) / 2.f));
                collider.setRotation(tile.rotation * -90);
                colliders.add(collider);
                bodyComponent.addCollider(collider);
            }
        }
    }

    public JSONObject serialize(){
        final JSONArray arr = new JSONArray();

        iterate(new GridIterator() {
            @Override
            public void iterate(AbstractTile tile) {
                arr.put(tile.serialize());
            }
        });
        
        JSONObject obj = new JSONObject();
        obj.put("grid_name", gridName);
        obj.put("tiles", arr);
        return obj;
    }

    public static Grid unserialize(JSONObject obj){
        try {
            JSONArray tiles = obj.getJSONArray("tiles");
            Grid grid = new Grid();

            for(int i = 0; i < tiles.length(); i++){
                AbstractTile tile = TileFactory.unserialize(tiles.getJSONObject(i));
                grid.setTile(tile.x, tile.y, tile);
            }
            
            return grid;
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return null;
    }
}

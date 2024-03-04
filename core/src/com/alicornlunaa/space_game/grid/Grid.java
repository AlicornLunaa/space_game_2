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
    public static enum Layer { BOTTOM, MIDDLE, TOP };

    @SuppressWarnings("unused")
    private static class Chunk {
        // Variables
        public final int chunkX, chunkY; // World position of chunks
        private boolean[] occupancyMap; // Whether or not the current tile is occupied
        private AbstractTile[] tileMap; // Actual tiles being stored
        private TileElement[] bottomLayer; // Back wall
        private TileElement[] topLayer; // Front wall
        private int tileCount = 0;

        // Constructor
        public Chunk(int chunkX, int chunkY){
            this.chunkX = chunkX;
            this.chunkY = chunkY;
            occupancyMap = new boolean[Constants.CHUNK_SIZE * Constants.CHUNK_SIZE];
            tileMap = new AbstractTile[Constants.CHUNK_SIZE * Constants.CHUNK_SIZE];
            bottomLayer = new TileElement[Constants.CHUNK_SIZE * Constants.CHUNK_SIZE];
            topLayer = new TileElement[Constants.CHUNK_SIZE * Constants.CHUNK_SIZE];
        }

        // Functions
        public int getTileCount(){
            return tileCount;
        }

        public boolean isOccupied(int x, int y, Layer layer){
            switch(layer){
                case BOTTOM:
                    return bottomLayer[y * Constants.CHUNK_SIZE + x] != null;

                case MIDDLE:
                    return occupancyMap[y * Constants.CHUNK_SIZE + x] == true;

                case TOP:
                    return topLayer[y * Constants.CHUNK_SIZE + x] != null;

                default:
                    return false;
            }
        }

        public void setTile(int x, int y, AbstractTile tile, Layer layer){
            switch(layer){
                case BOTTOM:
                    if(tile instanceof TileElement)
                        bottomLayer[y * Constants.CHUNK_SIZE + x] = (TileElement)tile;
                    break;

                case MIDDLE:
                    occupancyMap[y * Constants.CHUNK_SIZE + x] = true;
                    tileMap[y * Constants.CHUNK_SIZE + x] = tile;
                    tileCount++;
                    break;

                case TOP:
                    if(tile instanceof TileElement)
                        topLayer[y * Constants.CHUNK_SIZE + x] = (TileElement)tile;
                    break;
            }
        }

        public void removeTile(int x, int y, Layer layer){
            switch(layer){
                case BOTTOM:
                    bottomLayer[y * Constants.CHUNK_SIZE + x] = null;
                    break;

                case MIDDLE:
                    occupancyMap[y * Constants.CHUNK_SIZE + x] = false;
                    tileMap[y * Constants.CHUNK_SIZE + x] = null;
                    tileCount--;
                    break;

                case TOP:
                    topLayer[y * Constants.CHUNK_SIZE + x] = null;
                    break;
            }
        }

        public @Null AbstractTile getTile(int x, int y, Layer layer){
            switch(layer){
                case BOTTOM:
                    return bottomLayer[y * Constants.CHUNK_SIZE + x];

                case MIDDLE:
                    return tileMap[y * Constants.CHUNK_SIZE + x];

                case TOP:
                    return topLayer[y * Constants.CHUNK_SIZE + x];

                default:
                    return null;
            }
        }
    };

    public static interface GridIterator {
        public void iterate(AbstractTile tile);
    }

    // Variables
    private HashMap<String, Chunk> chunks = new HashMap<>();
    private Array<Collider> colliders = new Array<>();

    public String gridName = "unnamed_grid";
    public boolean drawTop = true;
    
    // Constructor
    public Grid(){}

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

    public boolean isOccupied(int x, int y, Layer layer){
        Chunk chunk = getChunkFromWorld(x, y);

        if(chunk != null)
            return chunk.isOccupied(
                Math.floorMod(x, Constants.CHUNK_SIZE),
                Math.floorMod(y, Constants.CHUNK_SIZE),
                layer
            );

        return false;
    }

    public boolean isRegionOccupied(int x, int y, int rotation, int width, int height, Layer layer){
        for(int w = 0; w < width; w++){
            for(int h = 0; h < height; h++){
                switch(rotation){
                    default:
                        if(isOccupied(x + w, y + h, layer))
                            return true;
                        break;

                    case 1:
                        if(isOccupied(x + h, y - w, layer))
                            return true;
                        break;

                    case 2:
                        if(isOccupied(x - w, y - h, layer))
                            return true;
                        break;
                            
                    case 3:
                        if(isOccupied(x - h, y + w, layer))
                            return true;
                        break;
                }
            }
        }

        return false;
    }

    public boolean setTile(int x, int y, AbstractTile tile, Layer layer){
        Chunk chunk;

        if(isRegionOccupied(x, y, tile.rotation, tile.width, tile.height, layer))
            return false;

        tile.x = x;
        tile.y = y;

        for(int w = 0; w < tile.width; w++){
            for(int h = 0; h < tile.height; h++){
                switch(tile.rotation){
                    default:
                        chunk = newChunkFromWorld(x + w, y + h);
                        chunk.setTile(Math.floorMod(x + w, Constants.CHUNK_SIZE), Math.floorMod(y + h, Constants.CHUNK_SIZE), tile, layer);
                        break;

                    case 1:
                        chunk = newChunkFromWorld(x + h, y - w);
                        chunk.setTile(Math.floorMod(x + h, Constants.CHUNK_SIZE), Math.floorMod(y - w, Constants.CHUNK_SIZE), tile, layer);
                        break;

                    case 2:
                        chunk = newChunkFromWorld(x - w, y - h);
                        chunk.setTile(Math.floorMod(x - w, Constants.CHUNK_SIZE), Math.floorMod(y - h, Constants.CHUNK_SIZE), tile, layer);
                        break;
                            
                    case 3:
                        chunk = newChunkFromWorld(x - h, y + w);
                        chunk.setTile(Math.floorMod(x - h, Constants.CHUNK_SIZE), Math.floorMod(y + w, Constants.CHUNK_SIZE), tile, layer);
                        break;
                }
            }
        }

        return true;
    }

    public void removeTile(int x, int y, Layer layer){
        AbstractTile tile = getTile(x, y, layer);
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
                        chunk.removeTile(Math.floorMod(x + w, Constants.CHUNK_SIZE), Math.floorMod(y + h, Constants.CHUNK_SIZE), layer);
                        pruneChunkFromWorld(x + w, y + h);
                        break;

                    case 1:
                        chunk = newChunkFromWorld(x + h, y - w);
                        chunk.removeTile(Math.floorMod(x + h, Constants.CHUNK_SIZE), Math.floorMod(y - w, Constants.CHUNK_SIZE), layer);
                        pruneChunkFromWorld(x + h, y - w);
                        break;

                    case 2:
                        chunk = newChunkFromWorld(x - w, y - h);
                        chunk.removeTile(Math.floorMod(x - w, Constants.CHUNK_SIZE), Math.floorMod(y - h, Constants.CHUNK_SIZE), layer);
                        pruneChunkFromWorld(x - w, y - h);
                        break;
                            
                    case 3:
                        chunk = newChunkFromWorld(x - h, y + w);
                        chunk.removeTile(Math.floorMod(x - h, Constants.CHUNK_SIZE), Math.floorMod(y + w, Constants.CHUNK_SIZE), layer);
                        pruneChunkFromWorld(x - h, y + w);
                        break;
                }
            }
        }
    }

    public @Null AbstractTile getTile(int x, int y, Layer layer){
        Chunk chunk = getChunkFromWorld(x, y);

        if(chunk != null)
            return chunk.getTile(
                Math.floorMod(x, Constants.CHUNK_SIZE),
                Math.floorMod(y, Constants.CHUNK_SIZE),
                layer
            );

        return null;
    }

    public void iterate(Layer layer, GridIterator iter){
        AbstractTile[] layerArray;

        for (Chunk chunk : chunks.values()) {
            switch(layer){
                case BOTTOM:
                    layerArray = chunk.bottomLayer;
                    break;
    
                case TOP:
                    layerArray = chunk.topLayer;
                    break;
    
                default:
                    layerArray = chunk.tileMap;
                    break;
            }

            for(AbstractTile tile : layerArray){
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

        // Hull for colliding with world
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

    public void clear(){
        disassemble();
        chunks.clear();
    }

    public void center(){
        // Take the average position of everything and center it
        Array<AbstractTile> bottomLayer = new Array<>();
        Array<AbstractTile> middleLayer = new Array<>();
        Array<AbstractTile> topLayer = new Array<>();
        int tileCount = 0;
        int avgX = 0;
        int avgY = 0;
        
        for(Chunk chunk : chunks.values()) {
            for(AbstractTile tile : chunk.bottomLayer){
                if(tile == null)
                    continue;

                tile.updatedThisTick = false;
            }
            
            for(AbstractTile tile : chunk.tileMap){
                if(tile == null)
                    continue;

                tile.updatedThisTick = false;
            }

            for(AbstractTile tile : chunk.topLayer){
                if(tile == null)
                    continue;

                tile.updatedThisTick = false;
            }
        }

        for(Chunk chunk : chunks.values()) {
            for(AbstractTile tile : chunk.bottomLayer){
                if(tile == null || tile.updatedThisTick)
                    continue;

                tile.updatedThisTick = true;
                tileCount++;
                avgX += tile.x;
                avgY += tile.y;
                bottomLayer.add(tile);
            }

            for(AbstractTile tile : chunk.tileMap){
                if(tile == null || tile.updatedThisTick)
                    continue;

                tile.updatedThisTick = true;
                tileCount++;
                avgX += tile.x;
                avgY += tile.y;
                middleLayer.add(tile);
            }

            for(AbstractTile tile : chunk.topLayer){
                if(tile == null || tile.updatedThisTick)
                    continue;

                tile.updatedThisTick = true;
                tileCount++;
                avgX += tile.x;
                avgY += tile.y;
                topLayer.add(tile);
            }
        }

        avgX /= tileCount;
        avgY /= tileCount;

        // Clear the tiles and remake the ship
        clear();

        for(AbstractTile tile : bottomLayer){
            setTile(tile.x - avgX, tile.y - avgY, tile, Layer.BOTTOM);
        }
        
        for(AbstractTile tile : middleLayer){
            setTile(tile.x - avgX, tile.y - avgY, tile, Layer.MIDDLE);
        }

        for(AbstractTile tile : topLayer){
            setTile(tile.x - avgX, tile.y - avgY, tile, Layer.TOP);
        }
    }

    public byte[] serialize(){
        final JSONArray bottomLayer = new JSONArray();
        final JSONArray middleLayer = new JSONArray();
        final JSONArray topLayer = new JSONArray();
        center();

        iterate(Layer.BOTTOM, new GridIterator() {
            @Override
            public void iterate(AbstractTile tile) {
                bottomLayer.put(tile.serialize());
            }
        });

        iterate(Layer.MIDDLE, new GridIterator() {
            @Override
            public void iterate(AbstractTile tile) {
                middleLayer.put(tile.serialize());
            }
        });

        iterate(Layer.TOP, new GridIterator() {
            @Override
            public void iterate(AbstractTile tile) {
                topLayer.put(tile.serialize());
            }
        });
        
        JSONObject obj = new JSONObject();
        obj.put("grid_name", gridName);
        obj.put("bottom_tiles", bottomLayer);
        obj.put("tiles", middleLayer);
        obj.put("top_tiles", topLayer);

        return obj.toString().getBytes();
    }

    public static Grid unserialize(byte[] data){
        try {
            JSONObject obj = new JSONObject(new String(data));
            JSONArray bottomLayer = obj.getJSONArray("bottom_tiles");
            JSONArray middleLayer = obj.getJSONArray("tiles");
            JSONArray topLayer = obj.getJSONArray("top_tiles");
            Grid grid = new Grid();

            for(int i = 0; i < bottomLayer.length(); i++){
                AbstractTile tile = TileFactory.unserialize(bottomLayer.getJSONObject(i));
                grid.setTile(tile.x, tile.y, tile, Layer.BOTTOM);
            }

            for(int i = 0; i < middleLayer.length(); i++){
                AbstractTile tile = TileFactory.unserialize(middleLayer.getJSONObject(i));
                grid.setTile(tile.x, tile.y, tile, Layer.MIDDLE);
            }

            for(int i = 0; i < topLayer.length(); i++){
                AbstractTile tile = TileFactory.unserialize(topLayer.getJSONObject(i));
                grid.setTile(tile.x, tile.y, tile, Layer.TOP);
            }
            
            return grid;
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return null;
    }
}

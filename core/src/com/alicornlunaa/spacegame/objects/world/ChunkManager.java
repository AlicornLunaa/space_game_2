package com.alicornlunaa.spacegame.objects.world;

import com.alicornlunaa.selene_engine.phys.PhysWorld;
import com.alicornlunaa.spacegame.App;
import com.alicornlunaa.spacegame.objects.blocks.Tile;
import com.alicornlunaa.spacegame.util.Constants;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.EdgeShape;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Null;

/**
 * Holds world chunks and manages which is loaded or not
 */
public class ChunkManager extends Group {
    // Variables
    private TerrainGenerator generator;
    private PhysWorld world;
    private Chunk[][] chunks;

    private Array<Chunk> loadedChunks = new Array<>();
    private Array<Chunk> visibleChunks = new Array<>();
    private Array<Fixture> activeFixtures = new Array<>();

    private Body worldBody;

    private boolean tileUpdate = false;
    private int lastPlayerX = -1;
    private int lastPlayerY = -1;

    // Private functions
    private void generateChunkHull(){
        // Update based on the player's positioning and time active
        Vector2 plyPos = App.instance.gameScene.player.getCenter();
        
        // Get chunk coordinates for the player
        int loadDist = Constants.CHUNK_LOAD_DISTANCE;
        int plyTileX = (int)(plyPos.x / Constants.TILE_SIZE);
        int plyTileY = (int)(plyPos.y / Constants.TILE_SIZE);
        
        // Destroy all current fixtures
        while(activeFixtures.size > 0){
            worldBody.destroyFixture(activeFixtures.pop());
        }

        // Algorithm: Go from start to end of the entire load distance,
        // Keep track of the start of an edge and create the edge when it ends.
        // Repeat until finished. Run again but for Y.
        EdgeShape hullShape = new EdgeShape();
        float physTileSize = Constants.TILE_SIZE;
        int startTop = -1;
        int startBottom = -1;
        int startLeft = -1;
        int startRight = -1;

        for(int y = loadDist * -1 * Constants.CHUNK_SIZE; y < loadDist * Constants.CHUNK_SIZE; y++){
            for(int x = loadDist * -1 * Constants.CHUNK_SIZE; x < loadDist * Constants.CHUNK_SIZE; x++){
                // Get the chunk coordinates and tile coordinates from the global x and y
                Tile tile = getTileFromGlobal(x + plyTileX, y + plyTileY);
                Tile tileAbove = getTileFromGlobal(x + plyTileX, y + plyTileY + 1);
                Tile tileBelow = getTileFromGlobal(x + plyTileX, y + plyTileY - 1);

                if(tile != null && tileAbove == null && startTop == -1){
                    // Create a new shape on the edge of this tile on the top
                    startTop = x + plyTileX;
                } else if((tile == null || tileAbove != null) && startTop != -1){
                    hullShape.set(
                        startTop * physTileSize,
                        (y + plyTileY) * physTileSize + physTileSize,
                        (x - 1 + plyTileX) * physTileSize + physTileSize,
                        (y + plyTileY) * physTileSize + physTileSize
                    );
                    activeFixtures.add(worldBody.createFixture(hullShape, 0.f));
                    startTop = -1;
                }

                if(tile != null && tileBelow == null && startBottom == -1){
                    // Create a new shape on the edge of this tile on the top
                    startBottom = x + plyTileX;
                } else if((tile == null || tileBelow != null) && startBottom != -1){
                    hullShape.set(
                        startBottom * physTileSize,
                        (y + plyTileY) * physTileSize,
                        (x - 1 + plyTileX) * physTileSize + physTileSize,
                        (y + plyTileY) * physTileSize
                    );
                    activeFixtures.add(worldBody.createFixture(hullShape, 0.f));
                    startBottom = -1;
                }
            }

            if(startTop != -1){
                hullShape.set(
                    startTop * physTileSize,
                    (y + plyTileY) * physTileSize + physTileSize,
                    (loadDist * Constants.CHUNK_SIZE - 1 + plyTileX) * physTileSize + physTileSize,
                    (y + plyTileY) * physTileSize + physTileSize
                );
                activeFixtures.add(worldBody.createFixture(hullShape, 0.f));
            }

            if(startBottom != -1){
                hullShape.set(
                    startBottom * physTileSize,
                    (y + plyTileY) * physTileSize,
                    (loadDist * Constants.CHUNK_SIZE - 1 + plyTileX) * physTileSize + physTileSize,
                    (y + plyTileY) * physTileSize
                );
                activeFixtures.add(worldBody.createFixture(hullShape, 0.f));
            }

            startTop = -1;
            startBottom = -1;
        }

        for(int x = loadDist * -1 * Constants.CHUNK_SIZE; x < loadDist * Constants.CHUNK_SIZE; x++){
            for(int y = loadDist * -1 * Constants.CHUNK_SIZE; y < loadDist * Constants.CHUNK_SIZE; y++){
                // Get the chunk coordinates and tile coordinates from the global x and y
                Tile tile = getTileFromGlobal(x + plyTileX, y + plyTileY);
                Tile tileLeft = getTileFromGlobal(x - 1 + plyTileX, y + plyTileY);
                Tile tileRight = getTileFromGlobal(x + 1 + plyTileX, y + plyTileY);

                if(tile != null && tileLeft == null && startLeft == -1){
                    // Create a new shape on the edge of this tile on the top
                    startLeft = y + plyTileY;
                } else if((tile == null || tileLeft != null) && startLeft != -1){
                    hullShape.set(
                        (x + plyTileX) * physTileSize,
                        startLeft * physTileSize,
                        (x + plyTileX) * physTileSize,
                        (y - 1 + plyTileY) * physTileSize + physTileSize
                    );
                    activeFixtures.add(worldBody.createFixture(hullShape, 0.f));
                    startLeft = -1;
                }

                if(tile != null && tileRight == null && startRight == -1){
                    // Create a new shape on the edge of this tile on the top
                    startRight = y + plyTileY;
                } else if((tile == null || tileRight != null) && startRight != -1){
                    hullShape.set(
                        (x + plyTileX) * physTileSize + physTileSize,
                        startRight * physTileSize,
                        (x + plyTileX) * physTileSize + physTileSize,
                        (y - 1 + plyTileY) * physTileSize + physTileSize
                    );
                    activeFixtures.add(worldBody.createFixture(hullShape, 0.f));
                    startRight = -1;
                }
            }

            if(startLeft != -1){
                hullShape.set(
                    (x + plyTileX) * physTileSize,
                    startLeft * physTileSize,
                    (x + plyTileX) * physTileSize,
                    (loadDist * Constants.CHUNK_SIZE - 1 + plyTileY) * physTileSize + physTileSize
                );
                activeFixtures.add(worldBody.createFixture(hullShape, 0.f));
            }

            if(startRight != -1){
                hullShape.set(
                    (x + plyTileX) * physTileSize + physTileSize,
                    startRight * physTileSize,
                    (x + plyTileX) * physTileSize + physTileSize,
                    (loadDist * Constants.CHUNK_SIZE - 1 + plyTileY) * physTileSize
                );
                activeFixtures.add(worldBody.createFixture(hullShape, 0.f));
            }

            startLeft = -1;
            startRight = -1;
        }

        hullShape.dispose();
    }

    // Constructor
    public ChunkManager(TerrainGenerator generator, PhysWorld world, int width, int height){
        this.setTransform(false);
        this.world = world;
        this.generator = generator;
        chunks = new Chunk[width][height];

        BodyDef def = new BodyDef();
        def.type = BodyType.StaticBody;
        def.position.set(0, 0);
        worldBody = world.getBox2DWorld().createBody(def);
    }

    // Functions
    public @Null Tile getTileFromGlobal(int x, int y){
        if(chunks.length <= 0) return null;
        if(y < 0) return null;
        if(y >= chunks[0].length * Constants.CHUNK_SIZE) return null;

        int chunkX = Math.floorMod(Math.floorDiv(x, Constants.CHUNK_SIZE), chunks.length);
        int chunkY = Math.floorMod((int)(y / Constants.CHUNK_SIZE), chunks[0].length);
        int tileX = Math.floorMod(x, Constants.CHUNK_SIZE);
        int tileY = Math.floorMod(y, Constants.CHUNK_SIZE);
        Chunk chunk = chunks[chunkX][chunkY];

        if(chunk == null) return null;
        return chunk.getTile(tileX, tileY);
    }

    public void loadChunk(int x, int y){
        if(chunks[x][y] == null){
            // Generate new chunk because it didnt exist before
            chunks[x][y] = new Chunk(generator, world, x, y);
        }

        if(!chunks[x][y].isLoaded()){
            // Load if not loaded
            loadedChunks.add(chunks[x][y]);
            chunks[x][y].setLoaded(true);
        }

        // Load every active entity within the chunk
        // for(BaseEntity e : world.getEntities()){
        //     if(e.getBody().isActive() || e instanceof Player) continue;

        //     Vector2 entPos = e.getPosition();
        //     int entChunkX = (int)(entPos.x / Constants.CHUNK_SIZE / Tile.TILE_SIZE);
        //     int entChunkY = (int)(entPos.y / Constants.CHUNK_SIZE / Tile.TILE_SIZE);

        //     if(entChunkX == x && entChunkY == y){
        //         e.getBody().setActive(true);
        //     }
        // }
    }

    public void unloadChunk(int x, int y){
        if(chunks[x][y] == null) return;
        loadedChunks.removeValue(chunks[x][y], true);
        chunks[x][y].setLoaded(false);

        // Unload every active entity within the chunk
        // for(BaseEntity e : world.getEntities()){
        //     if(!e.getBody().isActive() || e instanceof Player) continue;

        //     Vector2 entPos = e.getCenter();
        //     int entChunkX = (int)(entPos.x / Constants.CHUNK_SIZE / Tile.TILE_SIZE);
        //     int entChunkY = (int)(entPos.y / Constants.CHUNK_SIZE / Tile.TILE_SIZE);

        //     if(entChunkX == x && entChunkY == y){
        //         e.getBody().setActive(false);
        //     }
        // }
    }

    public void update(){
        // Update based on the player's positioning and time active
        Vector2 plyPos = App.instance.gameScene.player.getCenter();
        
        // Get chunk coordinates for the player
        int loadDist = Constants.CHUNK_LOAD_DISTANCE;
        int viewDist = (int)(App.instance.camera.viewportWidth * App.instance.camera.zoom / Constants.TILE_SIZE / Constants.CHUNK_SIZE / 2 + 1); // TODO: Fix size
        int plyChunkX = (int)(plyPos.x / Constants.CHUNK_SIZE / Constants.TILE_SIZE);
        int plyChunkY = (int)(plyPos.y / Constants.CHUNK_SIZE / Constants.TILE_SIZE);
        int containedX = Math.min(Math.max(plyChunkX, 0), chunks.length);
        int containedY = Math.min(Math.max(plyChunkY, 0), chunks[0].length - loadDist);

        // Iterate over loaded chunks and unload if they are out of range
        for(int i = 0; i < loadedChunks.size; i++){
            Chunk chunk = loadedChunks.get(i);
            int currentChunkX = chunk.getChunkX();
            int currentChunkY = chunk.getChunkY();
            int distance = Math.max(Math.abs(containedX - currentChunkX), Math.abs(containedY - currentChunkY));
            
            // Unload if too far
            if(distance > loadDist){
                unloadChunk(currentChunkX, currentChunkY);
                i--;
            }

            if(chunk.chunkUpdate){
                chunk.chunkUpdate = false;
                tileUpdate = true;
            }
        }

        for(int i = 0; i < visibleChunks.size; i++){
            Chunk chunk = visibleChunks.get(i);
            int currentChunkX = chunk.getChunkX();
            int currentChunkY = chunk.getChunkY();
            int distance = Math.max(Math.abs(containedX - currentChunkX), Math.abs(containedY - currentChunkY));
            
            // Unload if too far
            if(distance > viewDist){
                chunk.setVisible(false);
                visibleChunks.removeIndex(i);
                removeActor(chunk);
                i--;
            }
        }

        // Iterate over the players surrounding chunks and load them
        for(int y = loadDist * -1; y < loadDist; y++){
            if(containedY + y > chunks[0].length) continue;

            for(int x = loadDist * -1; x < loadDist + 1; x++){
                int wrappedX = Math.floorMod(containedX + x, chunks.length);
                int wrappedY = Math.floorMod(containedY + y, chunks[0].length);
                loadChunk(wrappedX, wrappedY);
            }
        }

        for(int y = viewDist * -1; y < viewDist; y++){
            if(plyChunkY + y > chunks[0].length) continue;

            for(int x = viewDist * -1; x < viewDist + 1; x++){
                int wrappedX = Math.floorMod(plyChunkX + x, chunks.length);
                int wrappedY = Math.floorMod(plyChunkY + y, chunks[0].length);

                if(chunks[wrappedX][wrappedY] == null){
                    // Generate new chunk because it didnt exist before
                    chunks[wrappedX][wrappedY] = new Chunk(generator, world, wrappedX, wrappedY);
                }

                // Only add if not visible already
                if(!visibleChunks.contains(chunks[wrappedX][wrappedY], true)){
                    chunks[wrappedX][wrappedY].setVisible(true);
                    visibleChunks.add(chunks[wrappedX][wrappedY]);
                    this.addActor(chunks[wrappedX][wrappedY]);
                }
            }
        }
    
        // Now that everything's loaded or unloaded, run an algorithm to build the worldbody around the player
        // but only if theyve specifically moved locations to another chunk
        if(plyChunkX != lastPlayerX || plyChunkY != lastPlayerY || tileUpdate){
            lastPlayerX = plyChunkX;
            lastPlayerY = plyChunkY;
            tileUpdate = false;
            generateChunkHull();
        }
    }
}

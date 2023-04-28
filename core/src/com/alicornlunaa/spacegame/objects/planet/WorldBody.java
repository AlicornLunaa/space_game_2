package com.alicornlunaa.spacegame.objects.planet;

import com.alicornlunaa.spacegame.App;
import com.alicornlunaa.spacegame.engine.phys.PhysWorld;
import com.alicornlunaa.spacegame.objects.blocks.Tile;
import com.alicornlunaa.spacegame.objects.planet.terrain.Chunk;
import com.alicornlunaa.spacegame.util.Constants;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.utils.Array;

/**
 * Holds world chunks and manages which is loaded or not
 */
public class WorldBody extends Group {

    // Variables
    private final App game;
    private PhysWorld world;
    private Chunk[][] chunks;
    private Array<Chunk> loadedChunks = new Array<>();
    private Array<Fixture> activeFixtures = new Array<>();
    private Body worldBody;
    private int lastPlayerX = -1;
    private int lastPlayerY = -1;

    // Constructor
    public WorldBody(final App game, PhysWorld world, int width, int height){
        this.setTransform(false);
        this.game = game;
        this.world = world;
        chunks = new Chunk[width][height];

        BodyDef def = new BodyDef();
        def.type = BodyType.StaticBody;
        def.position.set(0, 0);
        worldBody = world.getBox2DWorld().createBody(def);

        // PolygonShape shape = new PolygonShape();
        // shape.setAsBox(10, Tile.TILE_SIZE / 2 / Constants.PPM);
        // worldBody.createFixture(shape, 0.f);
        // shape.dispose();
    }

    // Functions
    public void update(){
        // Update based on the player's positioning and time active
        Vector2 plyPos = game.player.getCenter();
        
        // Get chunk coordinates for the player
        int loadDist = Constants.CHUNK_LOAD_DISTANCE;
        int plyChunkX = (int)(plyPos.x / Chunk.CHUNK_SIZE / Tile.TILE_SIZE);
        int plyChunkY = (int)(plyPos.y / Chunk.CHUNK_SIZE / Tile.TILE_SIZE);

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
                int wrappedX = Math.floorMod(plyChunkX + x, chunks.length);
                int wrappedY = Math.floorMod(plyChunkY + y, chunks[0].length);
                Chunk chunk = chunks[wrappedX][wrappedY];

                if(chunk == null){
                    // Generate new chunk
                    chunks[wrappedX][wrappedY] = new Chunk(game, world, wrappedX, wrappedY);
                    this.addActor(chunks[wrappedX][wrappedY]);
                } else if(!chunk.isLoaded()){
                    // Load if not loaded
                    loadedChunks.add(chunk);
                    chunk.load();
                }
            }
        }
    
        // Now that everything's loaded or unloaded, run an algorithm to build the worldbody around the player
        // but only if theyve specifically moved locations to another chunk
        if(plyChunkX != lastPlayerX || plyChunkY != lastPlayerY){
            lastPlayerX = plyChunkX;
            lastPlayerY = plyChunkY;

            // Destroy all current fixtures
            while(activeFixtures.size > 0){
                worldBody.destroyFixture(activeFixtures.pop());
            }

            // Algorithm: Start top-left and move right. If there's a break in tiles, stop and create a new fixture
            PolygonShape shape = new PolygonShape();
            float offsetX = (plyChunkX - loadDist) * Tile.TILE_SIZE * Chunk.CHUNK_SIZE / Constants.PLANET_PPM;
            float offsetY = (plyChunkY - loadDist) * Tile.TILE_SIZE * Chunk.CHUNK_SIZE / Constants.PLANET_PPM;
            float physTileSize = Tile.TILE_SIZE / Constants.PLANET_PPM;
            int scanX = -1; // ScanX is the last X position to start a new tile bar
            int globalX = 0;
            int globalY = 0;

            for(int y = loadDist * -1; y < loadDist; y++){
                for(int ty = 0; ty < Chunk.CHUNK_SIZE; ty++){
                    for(int x = loadDist * -1; x < loadDist; x++){
                        int wrappedX = Math.floorMod(plyChunkX + x, chunks.length);
                        int wrappedY = Math.floorMod(plyChunkY + y, chunks[0].length);
                        Chunk chunk = chunks[wrappedX][wrappedY];
                        
                        for(int tx = 0; tx < Chunk.CHUNK_SIZE; tx++){
                            Tile tile = chunk.getTile(tx, ty);

                            if(tile != null && scanX == -1){
                                // No tile in progress, create a new one
                                scanX = globalX;
                            } else if(tile == null && scanX != -1) {
                                // A new tile was in creation, it ends here.
                                float width = (globalX - scanX) * physTileSize / 2;
                                shape.setAsBox(width, physTileSize / 2, new Vector2(width + (scanX * Tile.TILE_SIZE / Constants.PLANET_PPM) + offsetX, physTileSize / 2 + (globalY * Tile.TILE_SIZE / Constants.PLANET_PPM) + offsetY), 0.f);
                                activeFixtures.add(worldBody.createFixture(shape, 0.0f));
                                scanX = -1;
                            }

                            globalX++;
                        }
                    }

                    if(scanX != -1){
                        // Entire bar is one solid piece
                        float width = (globalX - scanX) * physTileSize / 2;
                        shape.setAsBox(width, physTileSize / 2, new Vector2(width + (scanX * Tile.TILE_SIZE / Constants.PLANET_PPM) + offsetX, physTileSize / 2 + (globalY * Tile.TILE_SIZE / Constants.PLANET_PPM) + offsetY), 0.f);
                        activeFixtures.add(worldBody.createFixture(shape, 0.0f));
                    }
                    
                    scanX = -1;
                    globalX = 0;
                    globalY++;
                }
            }

            shape.dispose();
        }
    }

    @Override
    public void draw(Batch batch, float a){
        Matrix4 trans = batch.getTransformMatrix().cpy();
        super.draw(batch, a);
        batch.setTransformMatrix(trans);
    }
    
}

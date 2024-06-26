package com.alicornlunaa.spacegame.objects.planet;

import com.alicornlunaa.spacegame.App;
import com.alicornlunaa.spacegame.engine.phys.PhysWorld;
import com.alicornlunaa.spacegame.objects.blocks.Tile;
import com.alicornlunaa.spacegame.util.Constants;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.EdgeShape;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Null;

/**
 * Holds world chunks and manages which is loaded or not
 */
public class WorldBody extends Group {

    // Static classes
    public static class Chunk extends Group {
        
        // Variables
        private final App game;
        private Tile[][] tiles = new Tile[Constants.CHUNK_SIZE][Constants.CHUNK_SIZE];
        private boolean active = false;
        private int chunkX;
        private int chunkY;

        public boolean chunkUpdate = false;
    
        // Private functions
        private void tempTileData(){
            // TODO: Remove
            if(chunkY < 14){
                for(int y = 0; y < Constants.CHUNK_SIZE; y++){
                    for(int x = 0; x < Constants.CHUNK_SIZE; x++){
                        final Tile tile = new Tile(game, x, y, "stone");
                        tiles[x][y] = tile;

                        tile.setBounds(
                            x * Tile.TILE_SIZE + chunkX * Constants.CHUNK_SIZE * Tile.TILE_SIZE,
                            y * Tile.TILE_SIZE + chunkY * Constants.CHUNK_SIZE * Tile.TILE_SIZE,
                            Tile.TILE_SIZE,
                            Tile.TILE_SIZE
                        );
                        this.addActor(tile);

                        tile.addListener(new ClickListener(){
                            @Override
                            public void enter(InputEvent event, float x, float y, int pointer, @Null Actor fromActor){
                                if(!Gdx.input.isTouched(0)) return;
                                removeActor(tile);
                                tiles[tile.getTileX()][tile.getTileY()] = null;
                                chunkUpdate = true;
                            }
                        });
                    }
                }
            }
        }
    
        // Constructor
        public Chunk(final App game, PhysWorld world, int chunkX, int chunkY){
            // Slight performance save
            this.setTransform(false);
            this.game = game;
            this.chunkX = chunkX;
            this.chunkY = chunkY;
    
            // Temp
            tempTileData();
        }
    
        // Functions
        public boolean isLoaded(){ return active; }
        public int getChunkX(){ return chunkX; }
        public int getChunkY(){ return chunkY; }
        public Tile getTile(int x, int y){
            if(x < 0 || x >= tiles.length) return null;
            if(y < 0 || y >= tiles[x].length) return null;
            return tiles[x][y];
        }
        
        @Override
        public void draw(Batch batch, float a){
            batch.setTransformMatrix(batch.getTransformMatrix().cpy().translate(chunkX * Constants.CHUNK_SIZE * Tile.TILE_SIZE, chunkY * Constants.CHUNK_SIZE * Tile.TILE_SIZE, 0));
            super.draw(batch, a);
            batch.setTransformMatrix(batch.getTransformMatrix().cpy().translate(-chunkX * Constants.CHUNK_SIZE * Tile.TILE_SIZE, -chunkY * Constants.CHUNK_SIZE * Tile.TILE_SIZE, 0));
        }
    
    }

    
    // Variables
    private final App game;
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
    private Tile getTileFromGlobal(int x, int y){
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

    private void generateChunkHull(){
        // Update based on the player's positioning and time active
        Vector2 plyPos = game.player.getCenter();
        
        // Get chunk coordinates for the player
        int loadDist = Constants.CHUNK_LOAD_DISTANCE;
        int plyTileX = (int)(plyPos.x / Tile.TILE_SIZE);
        int plyTileY = (int)(plyPos.y / Tile.TILE_SIZE);
        
        // Destroy all current fixtures
        while(activeFixtures.size > 0){
            worldBody.destroyFixture(activeFixtures.pop());
        }

        // Algorithm: Go from start to end of the entire load distance,
        // Keep track of the start of an edge and create the edge when it ends.
        // Repeat until finished. Run again but for Y.
        EdgeShape hullShape = new EdgeShape();
        float physTileSize = Tile.TILE_SIZE / Constants.PLANET_PPM;
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
    public WorldBody(final App game, PhysWorld world, int width, int height){
        this.setTransform(false);
        this.game = game;
        this.world = world;
        chunks = new Chunk[width][height];

        BodyDef def = new BodyDef();
        def.type = BodyType.StaticBody;
        def.position.set(0, 0);
        worldBody = world.getBox2DWorld().createBody(def);
    }

    // Functions
    public void loadChunk(int x, int y){
        if(chunks[x][y] == null){
            // Generate new chunk because it didnt exist before
            chunks[x][y] = new Chunk(game, world, x, y);
        }

        if(!chunks[x][y].active){
            // Load if not loaded
            loadedChunks.add(chunks[x][y]);
            chunks[x][y].active = true;
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
        chunks[x][y].active = false;

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
        Vector2 plyPos = game.player.getCenter();
        
        // Get chunk coordinates for the player
        int loadDist = Constants.CHUNK_LOAD_DISTANCE;
        int viewDist = (int)(game.activeCamera.viewportWidth / Tile.TILE_SIZE / Constants.CHUNK_SIZE / 2 + 1);
        int plyChunkX = (int)(plyPos.x / Constants.CHUNK_SIZE / Tile.TILE_SIZE);
        int plyChunkY = (int)(plyPos.y / Constants.CHUNK_SIZE / Tile.TILE_SIZE);
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
                    chunks[wrappedX][wrappedY] = new Chunk(game, world, wrappedX, wrappedY);
                }

                chunks[wrappedX][wrappedY].setVisible(true);
                visibleChunks.add(chunks[wrappedX][wrappedY]);
                this.addActor(chunks[wrappedX][wrappedY]);
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

    @Override
    public void draw(Batch batch, float a){
        Matrix4 trans = batch.getTransformMatrix().cpy();
        super.draw(batch, a);
        batch.setTransformMatrix(trans);
    }
    
}

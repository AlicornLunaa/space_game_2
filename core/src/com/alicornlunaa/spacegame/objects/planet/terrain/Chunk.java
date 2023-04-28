package com.alicornlunaa.spacegame.objects.planet.terrain;

import com.alicornlunaa.spacegame.App;
import com.alicornlunaa.spacegame.engine.phys.PhysWorld;
import com.alicornlunaa.spacegame.objects.blocks.Tile;
import com.alicornlunaa.spacegame.util.Constants;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.scenes.scene2d.Group;

/**
 * One chunk of the planet's terrain. The chunks start at 0, 0
 */
public class Chunk extends Group {

    // Static vars
    public static final int CHUNK_SIZE = 16; // 16x16 tiles
    
    // Variables
    private final App game;
    private Tile[][] tiles = new Tile[CHUNK_SIZE][CHUNK_SIZE];
    private Body chunkBody;
    private int chunkX;
    private int chunkY;

    // Private functions
    private void tempTileData(PolygonShape tempShape){
        // TODO: Remove
        if(chunkY < 1){
            for(int y = 0; y < CHUNK_SIZE; y++){
                for(int x = 0; x < CHUNK_SIZE; x++){
                    float halfExtents = Tile.TILE_SIZE / Constants.PLANET_PPM / 2;
                    tempShape.setAsBox(halfExtents, halfExtents, new Vector2(x, y).scl(halfExtents * 2).add(halfExtents, halfExtents), 0.f);
                    chunkBody.createFixture(tempShape, 0.f);

                    tiles[x][y] = new Tile(game, x, y, "stone");
                    this.addActor(tiles[x][y]);
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

        // Generate body for the chunk
        PolygonShape shape = new PolygonShape();
        BodyDef chunkBodyDef = new BodyDef();
        chunkBodyDef.type = BodyType.StaticBody;
        chunkBodyDef.position.set(chunkX * CHUNK_SIZE * Tile.TILE_SIZE / world.getPhysScale(), chunkY * CHUNK_SIZE * Tile.TILE_SIZE / world.getPhysScale());
        chunkBody = world.getBox2DWorld().createBody(chunkBodyDef);

        // Temp
        tempTileData(shape);

        // Cleanup
        shape.dispose();
    }

    // Functions
    public boolean isLoaded(){ return chunkBody.isActive(); }

    public void load(){
        chunkBody.setActive(true);
    }

    public void unload(){
        chunkBody.setActive(false);
    }

    public void setTile(int x, int y, Tile t){
        tiles[x][y] = t;
    }
    
    @Override
    public void draw(Batch batch, float a){
        batch.setTransformMatrix(batch.getTransformMatrix().cpy().translate(chunkX * CHUNK_SIZE * Tile.TILE_SIZE, chunkY * CHUNK_SIZE * Tile.TILE_SIZE, 0));
        super.draw(batch, a);
        batch.setTransformMatrix(batch.getTransformMatrix().cpy().translate(-chunkX * CHUNK_SIZE * Tile.TILE_SIZE, -chunkY * CHUNK_SIZE * Tile.TILE_SIZE, 0));
    }

}

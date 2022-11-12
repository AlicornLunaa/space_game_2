package com.alicornlunaa.spacegame.objects.Planet;

import java.util.HashMap;

import com.alicornlunaa.spacegame.App;
import com.alicornlunaa.spacegame.util.Constants;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Disposable;

public class Tile implements Disposable {

    // Enumerator
    public enum TileType { DIRT, STONE };

    // Static
    public static final float TILE_SIZE = 4;
    
    private static HashMap<TileType, TextureRegion> blockTextures = new HashMap<>();
    
    private static TextureRegion getTexture(TextureAtlas atlas, TileType type){
        if(blockTextures.containsKey(type)) return blockTextures.get(type);

        String texture = "error";

        switch(type){
            case DIRT:
                texture = "tiles/dirt";
                break;
            case STONE:
                texture = "tiles/stone";
                break;
        }

        TextureRegion region = atlas.findRegion(texture);
        blockTextures.put(type, region);
        return region;
    }

    // Variables
    private TextureRegionDrawable sprite;
    private int blockX;
    private int blockY;
    
    @SuppressWarnings("unused")
    private int chunkX;
    @SuppressWarnings("unused")
    private int chunkY;
    @SuppressWarnings("unused")
    private TileType type;

    private PolygonShape shape = new PolygonShape();

    // Constructor
    public Tile(final App game, final Body worldBody, int blockX, int blockY, int chunkX, int chunkY, TileType type){
        this.blockX = blockX;
        this.blockY = blockY;
        this.chunkX = chunkX;
        this.chunkY = chunkY;
        this.type = type;

        sprite = new TextureRegionDrawable(Tile.getTexture(game.atlas, type));
        
        shape.setAsBox(
            TILE_SIZE / 2 / Constants.PLANET_PPM,
            TILE_SIZE / 2 / Constants.PLANET_PPM,
            new Vector2(
                (blockX * TILE_SIZE + TILE_SIZE / 2) / Constants.PLANET_PPM,
                (blockY * TILE_SIZE + TILE_SIZE / 2) / Constants.PLANET_PPM
            ),
            0
        );
        worldBody.createFixture(shape, 0.0f);
    }

    // Functoins
    public float getX(){ return blockX; }
    public float getY(){ return blockY; }
    public TextureRegionDrawable getSprite(){ return sprite; }

    public void update(float delta){}

    public void draw(Batch batch){
        sprite.draw(
            batch,
            blockX * Tile.TILE_SIZE,
            blockY * Tile.TILE_SIZE,
            Tile.TILE_SIZE / 2,
            Tile.TILE_SIZE / 2,
            Tile.TILE_SIZE,
            Tile.TILE_SIZE,
            1,
            1,
            0
        );
    }

    @Override
    public void dispose(){

    }

}
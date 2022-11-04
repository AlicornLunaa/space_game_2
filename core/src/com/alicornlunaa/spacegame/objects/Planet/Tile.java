package com.alicornlunaa.spacegame.objects.Planet;

import java.util.HashMap;

import com.alicornlunaa.spacegame.App;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Disposable;

public class Tile implements Disposable {

    // Enumerator
    enum TileType { DIRT, STONE };

    // Static
    public static final float TILE_SIZE = 16;
    
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
    private int chunkX;
    private int chunkY;
    private TileType type;

    // Constructor
    public Tile(App game, int blockX, int blockY, int chunkX, int chunkY, TileType type){
        this.blockX = blockX;
        this.blockY = blockY;
        this.chunkX = chunkX;
        this.chunkY = chunkY;
        this.type = type;

        sprite = new TextureRegionDrawable(Tile.getTexture(game.atlas, type));
    }

    // Functoins
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
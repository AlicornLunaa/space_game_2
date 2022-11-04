package com.alicornlunaa.spacegame.objects.Planet;

import java.util.HashMap;

import com.alicornlunaa.spacegame.App;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;

public class Tile {

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
    TextureRegionDrawable sprite;
    int x;
    int y;
    TileType type;

    // Constructor
    public Tile(App game, int x, int y, TileType type){
        this.x = x;
        this.y = y;
        this.type = type;

        sprite = new TextureRegionDrawable(Tile.getTexture(game.atlas, type));
    }

    // Functoins
    public void draw(Batch batch){
        sprite.draw(
            batch,
            x * Tile.TILE_SIZE,
            y * Tile.TILE_SIZE,
            Tile.TILE_SIZE / 2,
            Tile.TILE_SIZE / 2,
            Tile.TILE_SIZE,
            Tile.TILE_SIZE,
            1,
            1,
            0
        );
    }

}
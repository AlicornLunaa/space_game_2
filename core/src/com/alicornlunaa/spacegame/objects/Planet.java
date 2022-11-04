package com.alicornlunaa.spacegame.objects;

import java.util.HashMap;

import com.alicornlunaa.spacegame.App;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;

/**
 * The World object will hold the data for the world's tiles
 * as well as the how to render the circular planet in space.
 */

public class Planet extends Entity {

    // Internal classes
    enum TileType { DIRT, STONE };

    public static float TILE_SIZE = 16;

    public static class Tile {

        // Static
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
                x * TILE_SIZE,
                y * TILE_SIZE,
                TILE_SIZE / 2,
                TILE_SIZE / 2,
                TILE_SIZE,
                TILE_SIZE,
                1,
                1,
                0
            );
        }

    }

    // Variables
    private HashMap<Vector2, Tile> map = new HashMap<>();
    private Vector2 cursor = new Vector2();

    // Constructor
    public Planet(final App game){
        // Initialize a cube for testing
        int initialRad = 15;
        for(int x = -initialRad; x <= initialRad; x++){
            for(int y = -initialRad; y <= initialRad; y++){
                map.put(new Vector2(x, y), new Tile(game, x, y, TileType.STONE));
            }
        }
    }

    // Functions
    public Tile getTile(int x, int y){
        cursor.set(x, y);
        return map.get(cursor);
    }

    public HashMap<Vector2, Tile> getMap(){
        return map;
    }
    
}

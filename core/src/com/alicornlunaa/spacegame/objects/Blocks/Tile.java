package com.alicornlunaa.spacegame.objects.Blocks;

import java.util.HashMap;

import com.alicornlunaa.spacegame.App;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;

public class Tile extends Actor {

    // Static vars
    public static final float TILE_SIZE = 8;
    public static HashMap<String, TextureRegion> tileTextures = new HashMap<>();
    
    private static TextureRegion getTexture(TextureAtlas atlas, String id){
        if(tileTextures.containsKey(id)) return tileTextures.get(id);

        String texture = "tiles/" + id;
        TextureRegion region = atlas.findRegion(texture);
        tileTextures.put(id, region);
        return region;
    }
    
    // Variables
    private int x;
    private int y;
    private String id;
    private TextureRegion region;

    // Constructor
    public Tile(final App game, int x, int y, String id){
        super();
        this.x = x;
        this.y = y;
        this.id = id;
        region = getTexture(game.atlas, id);

        setSize(TILE_SIZE, TILE_SIZE);
        setPosition(x, y);
    }

    // Functions
    public String getID(){ return id; }

    @Override
    public void draw(Batch b, float a){
        b.draw(
            region,
            x * TILE_SIZE, y * TILE_SIZE,
            0, 0,
            TILE_SIZE, TILE_SIZE,
            1, 1,
            0
        );
    }

}

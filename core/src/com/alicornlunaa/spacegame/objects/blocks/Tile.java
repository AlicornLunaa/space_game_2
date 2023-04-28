package com.alicornlunaa.spacegame.objects.blocks;

import java.util.HashMap;

import com.alicornlunaa.spacegame.App;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Fixture;
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
    private Fixture fixture;
    private TextureRegion region;

    // Constructor
    public Tile(final App game, int x, int y, String id, Fixture fixture){
        super();
        this.x = x;
        this.y = y;
        this.id = id;
        this.fixture = fixture;
        region = getTexture(game.atlas, id);
        setBounds(x, y, TILE_SIZE, TILE_SIZE);
    }

    // Functions
    public String getID(){ return id; }

    public void delete(Body body){
        body.destroyFixture(fixture);
    }

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

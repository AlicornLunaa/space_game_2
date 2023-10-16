package com.alicornlunaa.spacegame.objects.blocks;

import com.alicornlunaa.spacegame.App;
import com.alicornlunaa.spacegame.util.Constants;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;

public class Tile extends Actor {
    // Variables
    private int x;
    private int y;
    private String id;
    private TextureRegion region;

    // Constructor
    public Tile(int x, int y, String id){
        super();
        this.x = x;
        this.y = y;
        this.id = id;
        region = BaseTile.getTexture(App.instance.atlas, id);
        setBounds(x * Constants.TILE_SIZE, y * Constants.TILE_SIZE, Constants.TILE_SIZE, Constants.TILE_SIZE);
    }

    // Functions
    public String getID(){ return id; }
    public int getTileX(){ return x; }
    public int getTileY(){ return y; }

    @Override
    public void draw(Batch b, float a){
        b.draw(
            region,
            x * Constants.TILE_SIZE, y * Constants.TILE_SIZE,
            0, 0,
            Constants.TILE_SIZE, Constants.TILE_SIZE,
            1, 1,
            0
        );
    }
}

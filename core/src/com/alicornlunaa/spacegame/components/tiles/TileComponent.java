package com.alicornlunaa.spacegame.components.tiles;

import com.alicornlunaa.selene_engine.components.TextureComponent;
import com.alicornlunaa.selene_engine.ecs.IComponent;
import com.badlogic.gdx.graphics.g2d.Batch;

public abstract class TileComponent implements IComponent {
    // Variables
    protected TextureComponent textureComponent;
    public String tileID;

    // Constructor
    public TileComponent(TextureComponent textureComponent, String tileID){
        this.textureComponent = textureComponent;
        this.tileID = tileID;
    }

    // Functions
    public abstract void draw(Batch batch);
}

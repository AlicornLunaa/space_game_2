package com.alicornlunaa.selene_engine.ecs;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Null;
import com.badlogic.ashley.core.Component;

public class SpriteComponent implements Component {
    // Variables
    public enum AnchorPoint { CENTER, BOTTOM_LEFT };
    public Vector2 size = new Vector2(64, 64);
    public AnchorPoint anchor = AnchorPoint.CENTER;
    public @Null TextureRegion texture = null;

    // Constructors
    public SpriteComponent(){}

    public SpriteComponent(float width, float height){
        this(null, width, height);
    }

    public SpriteComponent(float width, float height, AnchorPoint anchor){
        this(null, width, height, anchor);
    }

    public SpriteComponent(TextureRegion texture){
        this.texture = texture;
    }

    public SpriteComponent(TextureRegion texture, float width, float height){
        this(texture);
        size.set(width, height);
    }

    public SpriteComponent(TextureRegion texture, float width, float height, AnchorPoint anchor){
        this(texture, width, height);
        this.anchor = anchor;
    }
}

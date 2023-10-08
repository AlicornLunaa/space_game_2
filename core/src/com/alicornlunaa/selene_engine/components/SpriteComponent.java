package com.alicornlunaa.selene_engine.components;

import com.alicornlunaa.selene_engine.ecs.IComponent;
import com.badlogic.gdx.math.Vector2;

public class SpriteComponent implements IComponent {
    public enum AnchorPoint { CENTER, BOTTOM_LEFT };

    public Vector2 size = new Vector2(64, 64);
    public AnchorPoint anchor = AnchorPoint.CENTER;

    public SpriteComponent(){}

    public SpriteComponent(float width, float height){
        size.set(width, height);
    }

    public SpriteComponent(float width, float height, AnchorPoint anchor){
        size.set(width, height);
        this.anchor = anchor;
    }
}

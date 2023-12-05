package com.alicornlunaa.spacegame.components;

import com.alicornlunaa.selene_engine.ecs.IComponent;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Vector2;

public abstract class CustomSpriteComponent implements IComponent {
    // Enums
    public static enum AnchorPoint { CENTER, BOTTOM_LEFT };

    // Variables
    public Vector2 size = new Vector2(64, 64);
    public AnchorPoint anchor = AnchorPoint.CENTER;

    // Functions
    public abstract void render(Batch batch);
}

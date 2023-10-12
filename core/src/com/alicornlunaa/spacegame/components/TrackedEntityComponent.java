package com.alicornlunaa.spacegame.components;

import com.alicornlunaa.selene_engine.ecs.IComponent;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;

public class TrackedEntityComponent implements IComponent {
    public Color color = Color.WHITE;
    public int pathTracingLength = 20000;
    public boolean predictFuture = false;
    
    public Array<Vector2> futurePoints = new Array<>();
    public Array<Vector2> pastPoints = new Array<>();

    public TrackedEntityComponent(Color color) {
        this.color = color;
    }
}

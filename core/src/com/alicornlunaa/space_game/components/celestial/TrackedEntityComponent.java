package com.alicornlunaa.space_game.components.celestial;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;

public class TrackedEntityComponent implements Component {
    // Variables
    public Color color = Color.WHITE;
    public int pathTracingLength = 20000;
    public boolean predictFuture = true;
    
    public Array<Vector2> futurePoints = new Array<>();
    public Array<Vector2> pastPoints = new Array<>();

    // Constructor
    public TrackedEntityComponent(Color color) {
        this.color = color;
    }
}

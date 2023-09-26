package com.alicornlunaa.spacegame.scenes.testing_scene;

import com.alicornlunaa.selene_engine.ecs.IComponent;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;

public class TrailComponent implements IComponent {
    public Color color;
    public int length = 20000;
    public Array<Vector2> points = new Array<>();

    public TrailComponent(Color color) {
        this.color = color;
    }
}

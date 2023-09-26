package com.alicornlunaa.spacegame.scenes.testing_scene;

import com.alicornlunaa.selene_engine.ecs.IComponent;
import com.badlogic.gdx.graphics.Color;

public class TrackedEntityComponent implements IComponent {
    public Color color;

    public TrackedEntityComponent(Color color) {
        this.color = color;
    }
}

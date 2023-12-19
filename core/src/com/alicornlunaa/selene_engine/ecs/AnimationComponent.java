package com.alicornlunaa.selene_engine.ecs;

import java.util.Stack;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;

public class AnimationComponent implements Component {
    // Variables
    public Stack<Animation<AtlasRegion>> animations = new Stack<>();
    public float stateTime = 0.f;
}

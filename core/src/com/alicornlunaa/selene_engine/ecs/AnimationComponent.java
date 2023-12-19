package com.alicornlunaa.selene_engine.ecs;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.utils.Array;

public class AnimationComponent implements Component {
    // Variables
    public Array<Animation<AtlasRegion>> animations = new Array<>();
    public float stateTime = 0.f;
    public int activeAnimation = 0;
}

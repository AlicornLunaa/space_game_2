package com.alicornlunaa.selene_engine.ecs;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Animation.PlayMode;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.utils.Array;

public class AnimationComponent implements Component {
    // Variables
    public Animation<AtlasRegion> animation;
    public float stateTime = 0.f;

    // Constructor
    public AnimationComponent(Array<AtlasRegion> textures, float frameTime, PlayMode mode){
        animation = new Animation<>(frameTime, textures, mode);
    }
}

package com.alicornlunaa.selene_engine.ecs;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.utils.ImmutableArray;

public class AnimationSystem extends EntitySystem {
    // Variables
    private ImmutableArray<Entity> entities;
    private ComponentMapper<AnimationComponent> am = ComponentMapper.getFor(AnimationComponent.class);
    private ComponentMapper<SpriteComponent> sm = ComponentMapper.getFor(SpriteComponent.class);

    // Constructor
    public AnimationSystem(){}

    // Functions
    @Override
    public void addedToEngine(Engine engine){
        entities = engine.getEntitiesFor(Family.all(AnimationComponent.class, SpriteComponent.class).get());
    }

    @Override
    public void update(float deltaTime){
		// Update every entity
        for(int i = 0; i < entities.size(); i++){
            // Get entity info
            Entity entity = entities.get(i);
            AnimationComponent animationComp = am.get(entity);
            SpriteComponent spriteComp = sm.get(entity);
            
            animationComp.stateTime += deltaTime;
            spriteComp.texture = animationComp.animation.getKeyFrame(animationComp.stateTime);
        }
    }
}

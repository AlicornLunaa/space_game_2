package com.alicornlunaa.spacegame.engine.phys;

import com.alicornlunaa.spacegame.engine.core.BaseEntity;
import com.alicornlunaa.spacegame.util.Constants;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Array;

/** Contains Box2D world and fixed timestep accumulator. Each world has unique scale */
public class PhysWorld {

    // Variables
    private World box2DWorld;
    private float accumulator;
    private float physScale;
    private Array<BaseEntity> entities = new Array<>();

    // Constructor
    public PhysWorld(float physScale){
        box2DWorld = new World(new Vector2(), true);
        accumulator = 0;
        this.physScale = physScale;
    }

    // Functions
    public float getPhysScale(){ return physScale; }
    public Array<BaseEntity> getEntities(){ return entities; }
    public World getBox2DWorld(){ return box2DWorld; }

    public void onEntityUpdate(BaseEntity e){}
    public void onUpdate(){}
    public void onAfterUpdate(){}

    public void update(){
        // Step the physics on the world
        accumulator += Math.min(Gdx.graphics.getDeltaTime(), 0.25f);
        while(accumulator >= Constants.TIME_STEP){
            box2DWorld.step(Constants.TIME_STEP, Constants.VELOCITY_ITERATIONS, Constants.POSITION_ITERATIONS);
            accumulator -= Constants.TIME_STEP;
            
            for(BaseEntity e : entities){
                e.update();
                onEntityUpdate(e);
            }

            onUpdate();
        }

        onAfterUpdate();
    }
    
}

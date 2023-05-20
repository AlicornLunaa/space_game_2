package com.alicornlunaa.selene_engine.phys;

import com.alicornlunaa.selene_engine.core.IEntity;
import com.alicornlunaa.spacegame.util.Constants;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Array;

/** Contains Box2D world and fixed timestep accumulator. Each world has unique scale */
public class PhysWorld {

    // Variables
    private World box2DWorld;
    private float physScale;
    private Array<IEntity> entities = new Array<>();

    // Constructor
    public PhysWorld(float physScale){
        box2DWorld = new World(new Vector2(), true);
        this.physScale = physScale;
    }

    // Functions
    public float getPhysScale(){ return physScale; }
    public Array<IEntity> getEntities(){ return entities; }
    public World getBox2DWorld(){ return box2DWorld; }

    public void onEntityUpdate(IEntity e){}
    public void onUpdate(){}
    public void onAfterUpdate(){}

    public void update(){
        // Step the physics on the world
        box2DWorld.step(Constants.TIME_STEP, Constants.VELOCITY_ITERATIONS, Constants.POSITION_ITERATIONS);
        
        for(IEntity e : entities){
            e.update();
            onEntityUpdate(e);
        }

        onUpdate();
    }
    
}

package com.alicornlunaa.selene_engine.phys;

import com.alicornlunaa.spacegame.util.Constants;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;

/** Contains Box2D world and fixed timestep accumulator. Each world has unique scale */
public class PhysWorld {
    // Variables
    private World box2DWorld;
    private float physScale;
    private Vector2 offset = new Vector2();

    // Constructor
    public PhysWorld(float physScale){
        box2DWorld = new World(new Vector2(), true);
        this.physScale = physScale;
    }

    // Functions
    public float getPhysScale(){ return physScale; }
    public World getBox2DWorld(){ return box2DWorld; }
    public Vector2 getOffset(){ return offset; }

    public void update(){
        // Step the physics on the world
        box2DWorld.step(Constants.TIME_STEP, Constants.VELOCITY_ITERATIONS, Constants.POSITION_ITERATIONS);
    }
}

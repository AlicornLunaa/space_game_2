package com.alicornlunaa.selene_engine.ecs;

import com.alicornlunaa.selene_engine.phys.Collider;
import com.alicornlunaa.selene_engine.phys.PhysWorld;
import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Null;

public class BodyComponent implements Component {
    // Variables
    public @Null Body body = null;
    public @Null PhysWorld world = null;

    public BodyDef bodyDef = new BodyDef();
    public Array<Collider> colliders = new Array<>();

    // Constructor
    public BodyComponent(){
        bodyDef.type = BodyType.DynamicBody;
    }
    
    public BodyComponent(Collider collider){
        this();
        colliders.add(collider);
    }

    // Functions
    public void addCollider(Collider collider){
        // Keep track of active colliders
        colliders.add(collider);

        if(body != null){
            // Attach immediately
            collider.attach(body);
        }
    }
}
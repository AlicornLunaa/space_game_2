package com.alicornlunaa.selene_engine.components;

import com.alicornlunaa.selene_engine.ecs.IComponent;
import com.alicornlunaa.selene_engine.phys.PhysWorld;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;

public class BodyComponent implements IComponent {

    public Body body;
    public PhysWorld world;

    public BodyComponent(PhysWorld world, BodyDef def){
        body = world.getBox2DWorld().createBody(def);
        this.world = world;
    }

}

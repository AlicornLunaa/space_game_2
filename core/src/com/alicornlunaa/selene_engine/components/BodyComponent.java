package com.alicornlunaa.selene_engine.components;

import com.alicornlunaa.selene_engine.ecs.IComponent;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.World;

public class BodyComponent implements IComponent {

    public Body body;

    public BodyComponent(World world, BodyDef def){
        body = world.createBody(def);
    }

}

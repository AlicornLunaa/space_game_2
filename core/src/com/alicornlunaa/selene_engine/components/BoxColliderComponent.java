package com.alicornlunaa.selene_engine.components;

import com.alicornlunaa.selene_engine.ecs.IComponent;
import com.alicornlunaa.spacegame.util.Constants;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.utils.Disposable;

public class BoxColliderComponent implements IComponent, Disposable {
    // Variables
    public PolygonShape shape = new PolygonShape();
    public Fixture fixture;
    
    // Cosntructors
    public BoxColliderComponent(BodyComponent body, float hx, float hy, float cx, float cy, float ang, float density){
        shape.setAsBox(hx, hy, new Vector2(cx, cy), ang);
        fixture = body.body.createFixture(shape, density);
    }
    
    public BoxColliderComponent(BodyComponent body, float hx, float hy, float density){
        this(body, hx - Constants.HITBOX_LINEUP_FACTOR, hy - Constants.HITBOX_LINEUP_FACTOR, 0.f, 0.f, 0.f, density);
    }

    // Functions
    @Override
    public void dispose() {
        shape.dispose();
    }
}

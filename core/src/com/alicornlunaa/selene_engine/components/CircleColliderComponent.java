package com.alicornlunaa.selene_engine.components;

import com.alicornlunaa.selene_engine.ecs.IComponent;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.utils.Disposable;

public class CircleColliderComponent implements IComponent, Disposable {
    public CircleShape shape = new CircleShape();
    public Fixture fixture;
    
    public CircleColliderComponent(BodyComponent body, float radius, float cx, float cy, float density){
        shape.setPosition(new Vector2(cx, cy));
        shape.setRadius(radius);
        fixture = body.body.createFixture(shape, density);
    }
    
    public CircleColliderComponent(BodyComponent body, float radius, float density){
        this(body, radius, 0.f, 0.f, density);
    }

    @Override
    public void dispose() {
        shape.dispose();
    }
}

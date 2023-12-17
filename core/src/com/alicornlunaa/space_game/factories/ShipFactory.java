package com.alicornlunaa.space_game.factories;

import com.alicornlunaa.selene_engine.ecs.ActorComponent;
import com.alicornlunaa.selene_engine.ecs.BodyComponent;
import com.alicornlunaa.selene_engine.ecs.TransformComponent;
import com.alicornlunaa.selene_engine.phys.PhysWorld;
import com.alicornlunaa.space_game.components.ship.InteriorComponent;
import com.alicornlunaa.space_game.components.ship.ShipComponent;
import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;

public class ShipFactory {
    private static void attachBox(Body body){
        PolygonShape shape = new PolygonShape();
        shape.setAsBox(1, 1);

        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = shape;
        fixtureDef.density = 1;

        body.createFixture(fixtureDef);
        shape.dispose();
    }

    public static Entity createShip(PhysWorld world, float x, float y, float rotation){
        TransformComponent transformComponent = new TransformComponent();
        transformComponent.position.set(x, y);
        transformComponent.rotation = rotation;

        BodyComponent bodyComponent = new BodyComponent(world);
        attachBox(bodyComponent.body);

        Entity entity = new Entity();
        entity.add(transformComponent);
        entity.add(bodyComponent);
        entity.add(new ActorComponent());
        entity.add(new InteriorComponent());
        entity.add(new ShipComponent());
        return entity;
    }
}

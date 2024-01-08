package com.alicornlunaa.space_game.factories;

import com.alicornlunaa.selene_engine.ecs.BodyComponent;
import com.alicornlunaa.selene_engine.ecs.SpriteComponent;
import com.alicornlunaa.selene_engine.ecs.TransformComponent;
import com.alicornlunaa.selene_engine.phys.Collider;
import com.alicornlunaa.space_game.App;
import com.badlogic.ashley.core.Entity;

public class BasicFactory {
    public static Entity createBox(float x, float y, float width, float height, float rotation){
        Entity entity = new Entity();
        entity.add(new TransformComponent(x, y, rotation));
        entity.add(new BodyComponent(Collider.box(0, 0, width / 2.f, height / 2.f, 0)));
        entity.add(new SpriteComponent(App.instance.atlas.findRegion("dev_texture"), width, height));
        return entity;
    }

    public static Entity createStaticBox(float x, float y, float width, float height, float rotation){
        Entity entity = new Entity();
        entity.add(new TransformComponent(x, y, rotation));
        entity.add(new BodyComponent(Collider.box(0, 0, width / 2.f, height / 2.f, 0)));
        entity.add(new SpriteComponent(App.instance.atlas.findRegion("dev_texture"), width, height));
        return entity;
    }
}

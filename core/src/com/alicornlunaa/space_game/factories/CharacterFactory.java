package com.alicornlunaa.space_game.factories;

import com.alicornlunaa.selene_engine.ecs.AnimationComponent;
import com.alicornlunaa.selene_engine.ecs.BodyComponent;
import com.alicornlunaa.selene_engine.ecs.SpriteComponent;
import com.alicornlunaa.selene_engine.ecs.TransformComponent;
import com.alicornlunaa.selene_engine.phys.Collider;
import com.alicornlunaa.space_game.App;
import com.alicornlunaa.space_game.components.player.ControlComponent;
import com.alicornlunaa.space_game.util.Constants;
import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.graphics.g2d.Animation.PlayMode;

public class CharacterFactory {
    public static Entity createCharacter(float x, float y, float rotation){
        // Create default character
        float radius = Constants.PLAYER_WIDTH / 2.f - Constants.HITBOX_LINEUP_FACTOR;

        BodyComponent bodyComp = new BodyComponent();
        bodyComp.addCollider(Collider.box(0, 0, radius, Constants.PLAYER_HEIGHT / 2.f - radius, 0).setFixture(0.1f, 0.05f, 1.4f, false));
        bodyComp.addCollider(Collider.circle(0, Constants.PLAYER_HEIGHT / 2.f - radius, radius).setFixture(0.1f, 0.05f, 1.4f, false));
        bodyComp.addCollider(Collider.circle(0, Constants.PLAYER_HEIGHT / -2.f + radius, radius).setFixture(0.1f, 0.05f, 1.4f, false));

        Entity entity = new Entity();
        entity.add(new TransformComponent(x, y, rotation));
        entity.add(bodyComp);
        entity.add(new SpriteComponent(Constants.PLAYER_WIDTH, Constants.PLAYER_HEIGHT));
        return entity;
    }

    public static Entity createPlayer(float x, float y, float rotation){
        Entity entity = createCharacter(x, y, rotation);
        entity.add(new ControlComponent());
        entity.add(new AnimationComponent(App.instance.atlas.findRegions("player/move_left"), 1 / 12.f, PlayMode.LOOP));
        return entity;
    }

    public static Entity createNPC(float x, float y, float rotation){
        Entity entity = createCharacter(x, y, rotation);
        return entity;
    }
}

package com.alicornlunaa.space_game.factories;

import com.alicornlunaa.selene_engine.ecs.AnimationComponent;
import com.alicornlunaa.selene_engine.ecs.BodyComponent;
import com.alicornlunaa.selene_engine.ecs.SpriteComponent;
import com.alicornlunaa.selene_engine.ecs.TransformComponent;
import com.alicornlunaa.selene_engine.phys.Collider;
import com.alicornlunaa.space_game.App;
import com.alicornlunaa.space_game.components.player.PlayerComponent;
import com.alicornlunaa.space_game.util.Constants;
import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Animation.PlayMode;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;

public class CharacterFactory {
    public static Entity createCharacter(float x, float y, float rotation){
        // Create default character
        float radius = Constants.PLAYER_WIDTH / 2.f - Constants.HITBOX_LINEUP_FACTOR;

        BodyComponent bodyComp = new BodyComponent();
        bodyComp.addCollider(Collider.box(0, 0, radius, Constants.PLAYER_HEIGHT / 2.f - radius, 0).setFixture(0.1f, 0.05f, 1.4f, false));
        bodyComp.addCollider(Collider.circle(0, Constants.PLAYER_HEIGHT / 2.f - radius, radius).setFixture(0.1f, 0.05f, 1.4f, false));
        bodyComp.addCollider(Collider.circle(0, Constants.PLAYER_HEIGHT / -2.f + radius, radius).setFixture(0.1f, 0.05f, 1.4f, false));

        Entity entity = new Entity();
        entity.add(new SpriteComponent(Constants.PLAYER_WIDTH, Constants.PLAYER_HEIGHT));
        entity.add(new TransformComponent(x, y, rotation));
        entity.add(bodyComp);
        return entity;
    }

    public static Entity createPlayer(float x, float y, float rotation){
        // Create player character
        AnimationComponent animationComp = new AnimationComponent();
        animationComp.animations.add(new Animation<AtlasRegion>(1 / 12.f, App.instance.atlas.findRegions("player/idle"), PlayMode.LOOP));
        
        Entity entity = createCharacter(x, y, rotation);
        entity.add(new PlayerComponent());
        entity.add(animationComp);
        return entity;
    }

    public static Entity createNPC(float x, float y, float rotation){
        Entity entity = createCharacter(x, y, rotation);
        return entity;
    }
}

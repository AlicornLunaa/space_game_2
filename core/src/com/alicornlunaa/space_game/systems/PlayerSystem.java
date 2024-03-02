package com.alicornlunaa.space_game.systems;

import com.alicornlunaa.selene_engine.ecs.AnimationComponent;
import com.alicornlunaa.selene_engine.ecs.BodyComponent;
import com.alicornlunaa.selene_engine.ecs.TransformComponent;
import com.alicornlunaa.space_game.components.player.PlayerComponent;
import com.alicornlunaa.space_game.components.player.PlayerComponent.PlayerState;
import com.alicornlunaa.space_game.util.Constants;
import com.alicornlunaa.space_game.util.ControlSchema;
import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;

public class PlayerSystem extends EntitySystem {
    // Variables
    private ImmutableArray<Entity> entities;
    private ComponentMapper<TransformComponent> tm = ComponentMapper.getFor(TransformComponent.class);
    private ComponentMapper<BodyComponent> bm = ComponentMapper.getFor(BodyComponent.class);
    private ComponentMapper<PlayerComponent> pm = ComponentMapper.getFor(PlayerComponent.class);
    private ComponentMapper<AnimationComponent> am = ComponentMapper.getFor(AnimationComponent.class);

    // Constructor
    public PlayerSystem(){
        super(1);
    }

    // Functions
    @Override
    public void addedToEngine(Engine engine){
        entities = engine.getEntitiesFor(Family.all(TransformComponent.class, BodyComponent.class, PlayerComponent.class, AnimationComponent.class).get());
    }

    @Override
    public void update(float deltaTime){
        // Update every entity
        for(int i = 0; i < entities.size(); i++){
            // Get entity info
            Entity entity = entities.get(i);
            TransformComponent transform = tm.get(entity);
            BodyComponent bodyComp = bm.get(entity);
            PlayerComponent plyComp = pm.get(entity);
            AnimationComponent animComp = am.get(entity);

            // Run raycasts
            plyComp.onGround = false;
            bodyComp.world.getBox2DWorld().rayCast(
                plyComp.jumpRaycast,
                bodyComp.body.getWorldPoint(new Vector2(0, Constants.PLAYER_HEIGHT / -2)).cpy(),
                bodyComp.body.getWorldPoint(new Vector2(0, Constants.PLAYER_HEIGHT / -2 - 0.1f)).cpy()
            );
            
            // Handle keyboard controls
            // TODO: Convert this to an input event system
            if(Gdx.input.isKeyPressed(ControlSchema.PLAYER_UP)) plyComp.vertical = 1;
            else if(Gdx.input.isKeyPressed(ControlSchema.PLAYER_DOWN)) plyComp.vertical = -1;
            else plyComp.vertical = 0;
            
            if(Gdx.input.isKeyPressed(ControlSchema.PLAYER_LEFT)) plyComp.horizontal = -1;
            else if(Gdx.input.isKeyPressed(ControlSchema.PLAYER_RIGHT)) plyComp.horizontal = 1;
            else plyComp.horizontal = 0;
            
            if(Gdx.input.isKeyPressed(ControlSchema.PLAYER_ROLL_LEFT)) plyComp.roll = 1;
            else if(Gdx.input.isKeyPressed(ControlSchema.PLAYER_ROLL_RIGHT)) plyComp.roll = -1;
            else plyComp.roll = 0;

            if(Gdx.input.isKeyJustPressed(ControlSchema.PLAYER_NOCLIP)) plyComp.isNoclipping = !plyComp.isNoclipping;

            // Set animation
            if(plyComp.state.animIndex != animComp.activeAnimation){
                animComp.activeAnimation = plyComp.state.animIndex;
                animComp.stateTime = 0.f;
            }

            // Skip forces if the component is disabled. This will be used for controlling other things
            if(!plyComp.enabled)
                continue;

            // Set their collisions based on noclip
            bodyComp.body.setActive(!plyComp.isNoclipping);

            // Apply forces
            if(plyComp.isNoclipping){
                // Noclip physics
                float speed = Gdx.input.isKeyPressed(ControlSchema.PLAYER_SPRINT) ? 1000 : 10; 

                bodyComp.body.setLinearVelocity(0, 0);
                bodyComp.body.setFixedRotation(true);

                transform.position.x += plyComp.horizontal * speed * deltaTime;
                transform.position.y += plyComp.vertical * speed * deltaTime;
                transform.rotation += plyComp.roll * 3 * deltaTime;

                plyComp.state = PlayerState.IDLE;
            } else if(plyComp.inSpace){
                // Space physics
                bodyComp.body.setFixedRotation(false);

                if(plyComp.vertical != 0 || plyComp.horizontal != 0){
                    Vector2 rightDirection = new Vector2((float)Math.cos(transform.rotation), (float)Math.sin(transform.rotation));
                    Vector2 upDirection = new Vector2(-(float)Math.sin(transform.rotation), (float)Math.cos(transform.rotation));
                    Vector2 totalForce = rightDirection.scl(plyComp.horizontal * Constants.PLAYER_MOVEMENT_SPEED).add(upDirection.scl(plyComp.vertical * Constants.PLAYER_MOVEMENT_SPEED));
                    bodyComp.body.applyLinearImpulse(totalForce.scl(deltaTime), bodyComp.body.getWorldCenter(), true);
                }

                if(plyComp.roll != 0)
                    bodyComp.body.applyTorque(plyComp.roll * Constants.PLAYER_ROLL_FORCE * deltaTime, true);

                // Get animation
                if(plyComp.horizontal < 0) plyComp.state = PlayerState.MOVE_LEFT_GROUND;
                else if(plyComp.horizontal > 0) plyComp.state = PlayerState.MOVE_RIGHT_GROUND;
                else plyComp.state = PlayerState.IDLE;
            } else {
                // Ground physics
                bodyComp.body.setFixedRotation(true);

                if(plyComp.vertical != 0 || plyComp.horizontal != 0 && bodyComp.body.getLinearVelocity().len() < 5){
                    bodyComp.body.applyLinearImpulse(new Vector2(plyComp.horizontal * Constants.PLAYER_MOVEMENT_SPEED, (plyComp.onGround ? plyComp.vertical : 0) * Constants.PLAYER_MOVEMENT_SPEED).scl(deltaTime), bodyComp.body.getWorldCenter(), true);
                }

                // Get animation
                if(plyComp.horizontal < 0) plyComp.state = PlayerState.MOVE_LEFT_GROUND;
                else if(plyComp.horizontal > 0) plyComp.state = PlayerState.MOVE_RIGHT_GROUND;
                else if(plyComp.vertical > 0) plyComp.state = PlayerState.JUMPING;
                else plyComp.state = PlayerState.IDLE;
            }
        }
    }
}

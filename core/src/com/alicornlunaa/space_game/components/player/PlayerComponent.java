package com.alicornlunaa.space_game.components.player;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.RayCastCallback;

public class PlayerComponent implements Component {
    // Static classes
    public static class JumpRaycast implements RayCastCallback {
        // Variables
        public PlayerComponent component;

        // Constructor
        public JumpRaycast(PlayerComponent component){
            this.component = component;
        }

        // Functions
        @Override
        public float reportRayFixture(Fixture fixture, Vector2 point, Vector2 normal, float fraction){
            component.onGround = true;
            return fraction;
        }
    }

    public static enum PlayerState {
        // Enumerations
        IDLE(0), MOVE_LEFT_GROUND(1), MOVE_RIGHT_GROUND(2), JUMPING(0);

        // Variables
        public final int animIndex;

        // Constructor
        private PlayerState(int index){
            animIndex = index;
        }
    };

    // Variables
    public boolean enabled = true;
    public boolean inSpace = true;
    public boolean onGround = false;
    public boolean isNoclipping = false;
    public PlayerState state = PlayerState.IDLE;

    public float vertical = 0.f;
    public float horizontal = 0.f;
    public float roll = 0.f;

    public JumpRaycast jumpRaycast;

    // Constructor
    public PlayerComponent(){
        jumpRaycast = new JumpRaycast(this);
    }
}

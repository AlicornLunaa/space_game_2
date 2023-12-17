package com.alicornlunaa.space_game.components.ship.parts;

import org.json.JSONObject;

import com.alicornlunaa.space_game.App;
import com.alicornlunaa.space_game.objects.ship.Ship;
import com.badlogic.gdx.graphics.g2d.ParticleEffectPool;
import com.badlogic.gdx.graphics.g2d.ParticleEffectPool.PooledEffect;
import com.badlogic.gdx.math.Vector2;

public class Thruster extends Part {
    // Variables
    private float power;
    private float coneAngle;
    private float coneSpeed;
    private float fuelUsage;

    private float currentAngle;

    private final float initial_scale = 0.2f;
    private float scale = 1;
    private PooledEffect effect;

    // Constructor
    public Thruster(final App game, final Ship ship, JSONObject obj){
        super(game, ship, obj);

        JSONObject metadata = obj.getJSONObject("metadata");
        power = metadata.getFloat("power");
        coneAngle = metadata.getFloat("coneAngle");
        coneSpeed = metadata.getFloat("coneSpeed");
        fuelUsage = metadata.getFloat("fuelUsage");

        currentAngle = 0.f;

        effect = game.manager.get(metadata.getString("effect"), ParticleEffectPool.class).obtain();
        effect.setPosition(0, 0);
        effect.scaleEffect(initial_scale);
        effect.start();
        
        effect.scaleEffect(1 / this.scale);
        this.scale = 0.001f;
        effect.scaleEffect(this.scale);
    }

    // Functions
    public void setTargetAngle(float angle){
        currentAngle += Math.min(Math.max((angle * coneAngle) - currentAngle, -coneSpeed), coneSpeed);
    }

    public void thrust(float delta, float throttle){
        if(throttle == 0) return;

        float physScale = parent.getBody().world.getPhysScale();
        Vector2 dir = new Vector2(0, 1
            // (float)Math.cos((currentAngle - 90) * (Math.PI / 180.f) + parent.getTransform().rotation),
            // (float)Math.sin((currentAngle - 90) * (Math.PI / 180.f) + parent.getTransform().rotation)
        );

        parent.getBody().body.applyForce(
            dir.scl(power * -throttle * delta / physScale),
            parent.getBody().body.getWorldPoint(getPosition().cpy().scl(1 / physScale)),
            true
        );
        
        parent.getState().liquidFuelStored -= (fuelUsage * power * -throttle * delta);
    }
    
    @Override
    public void tick(float delta){
        float compRoll = (parent.getState().roll == 0) ? parent.getState().artifRoll : parent.getState().roll;
        this.setTargetAngle(compRoll);
        this.thrust(delta, parent.getState().throttle);
        super.tick(delta);
    }

    @Override
    public void dispose(){
        effect.free();
    }
}

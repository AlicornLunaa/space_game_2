package com.alicornlunaa.space_game.components.ship.parts;

import org.json.JSONObject;

import com.alicornlunaa.selene_engine.ecs.BodyComponent;
import com.alicornlunaa.space_game.App;
import com.alicornlunaa.space_game.components.ship.ShipComponent;
import com.badlogic.gdx.graphics.g2d.ParticleEffectPool;
import com.badlogic.gdx.graphics.g2d.ParticleEffectPool.PooledEffect;
import com.badlogic.gdx.math.Vector2;

public class Thruster extends Part {
    // Variables
    public float power;
    public float coneAngle;
    public float coneSpeed;
    public float fuelUsage;

    private final float initial_scale = 0.2f;
    private float scale = 1;
    private float currentAngle;
    private PooledEffect effect;

    // Constructor
    public Thruster(ShipComponent shipComponent, JSONObject obj){
        super(shipComponent, obj);

        JSONObject metadata = obj.getJSONObject("metadata");
        power = metadata.getFloat("power");
        coneAngle = metadata.getFloat("coneAngle");
        coneSpeed = metadata.getFloat("coneSpeed");
        fuelUsage = metadata.getFloat("fuelUsage");

        currentAngle = 0.f;

        effect = App.instance.manager.get(metadata.getString("effect"), ParticleEffectPool.class).obtain();
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

    public void thrust(float delta, float throttle, BodyComponent bodyComp){
        if(throttle == 0) return;

        Vector2 dir = new Vector2(0, -1
            // (float)Math.cos((currentAngle - 90) * (Math.PI / 180.f) + parent.getTransform().rotation),
            // (float)Math.sin((currentAngle - 90) * (Math.PI / 180.f) + parent.getTransform().rotation)
        );

        bodyComp.body.applyForce(
            dir.scl(power * -throttle * delta),
            bodyComp.body.getWorldPoint(getPosition()),
            true
        );
        
        shipComponent.liquidFuelStored -= (fuelUsage * power * -throttle * delta);
    }
    
    @Override
    public void tick(float delta, ShipComponent shipComponent, BodyComponent bodyComponent){
        setTargetAngle((shipComponent.roll == 0) ? shipComponent.artifRoll : shipComponent.roll);
        thrust(delta, shipComponent.throttle, bodyComponent);
        super.tick(delta, shipComponent, bodyComponent);
    }

    @Override
    public void dispose(){
        effect.free();
    }
}

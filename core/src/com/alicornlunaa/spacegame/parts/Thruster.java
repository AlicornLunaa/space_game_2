package com.alicornlunaa.spacegame.parts;

import org.json.JSONObject;

import com.alicornlunaa.spacegame.App;
import com.alicornlunaa.spacegame.objects.Ship.Ship;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.ParticleEmitter;
import com.badlogic.gdx.graphics.g2d.ParticleEffectPool.PooledEffect;
import com.badlogic.gdx.graphics.g2d.ParticleEmitter.ScaledNumericValue;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;

public class Thruster extends Part {
    // Variables
    private float power;
    private float coneAngle;
    private float coneSpeed;
    private float fuelUsage;
    private float rotationOffset;

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
        rotationOffset = getRotation();

        currentAngle = 0.f;

        effect = game.manager.getEffect(metadata.getString("effect"));
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

        Vector2 dir = new Vector2(
            (float)Math.cos((currentAngle - 90) * (Math.PI / 180.f) + parent.getAngle()),
            (float)Math.sin((currentAngle - 90) * (Math.PI / 180.f) + parent.getAngle())
        );

        parent.applyForce(
            dir.scl(power * -throttle * delta / physScale),
            parent.getWorldPoint(
                new Vector2(
                    getX() / physScale,
                    getY() / physScale
                )
            ),
            true
        );
    }
    
    @Override
    protected void drawEffectsBelow(Batch batch, float deltaTime){
        // Reset angle to draw
        Matrix4 batchMatrix = new Matrix4(batch.getTransformMatrix());
        batch.setTransformMatrix(batch.getTransformMatrix().mul(batchMatrix.inv()));

        // Set emitter positions and angles
        for(ParticleEmitter emitter : effect.getEmitters()){
            ScaledNumericValue emitterAngle = emitter.getAngle();
            float a = (rotationOffset + currentAngle + (float)Math.toDegrees(parent.getAngle())) - 90;

            emitterAngle.setHigh(a - 15, a + 15);
            emitterAngle.setLow(a);
        }

        // Draw effects
        Vector3 pos = new Vector3(getX(), getY(), 0).mul(batchMatrix.inv());
        effect.setPosition(pos.x, pos.y);
        effect.update(deltaTime);
        effect.draw(batch, deltaTime);
        
        // Adjust thruster angle
        batch.setTransformMatrix(batchMatrix.rotate(0, 0, 1, currentAngle));
    }

    @Override
    protected void drawEffectsAbove(Batch batch, float deltaTime){
        // Reset matrix
        Matrix4 batchMatrix = new Matrix4(batch.getTransformMatrix());
        batch.setTransformMatrix(batchMatrix.rotate(0, 0, 1, -currentAngle));
    }

    @Override
    public void update(float delta){
        float compRoll = (stateRef.roll == 0) ? stateRef.artifRoll : stateRef.roll;

        this.setTargetAngle(compRoll);
        this.thrust(delta, stateRef.throttle);

        effect.scaleEffect(1 / scale);
        scale = (stateRef.throttle + 0.001f);
        effect.scaleEffect(scale);
    }

    @Override
    public void dispose(){
        effect.free();
    }

}

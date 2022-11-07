package com.alicornlunaa.spacegame.parts;

import java.util.ArrayList;

import com.alicornlunaa.spacegame.App;
import com.alicornlunaa.spacegame.states.ShipState;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.ParticleEmitter;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.ParticleEffectPool.PooledEffect;
import com.badlogic.gdx.graphics.g2d.ParticleEmitter.ScaledNumericValue;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.Body;

public class Thruster extends ShipPart {
    // Variables
    private String name;
    private String description;
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
    public Thruster(App game, Body parent, ShipState stateRef, TextureRegion region, float scale, Vector2 posOffset, float rotOffset, ArrayList<Vector2> attachmentPoints, String name, String description, float density, float power, float coneAngle, float fuelUsage, String effectName){
        super(parent, stateRef, region, scale, posOffset, rotOffset, attachmentPoints);

        this.name = name;
        this.description = description;
        this.power = power;
        this.coneAngle = coneAngle;
        this.coneSpeed = 1.5f;
        this.fuelUsage = fuelUsage;
        this.rotationOffset = rotOffset;

        currentAngle = 0.f;

        effect = game.manager.getEffect(effectName);
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

        parent.applyForce(dir.scl(power * -throttle * delta / getPhysScale()), parent.getWorldPoint(new Vector2(getX() / getPhysScale(), getY() / getPhysScale())), true);
    }
    
    @Override
    protected void drawEffectsUnder(Batch batch, float deltaTime){
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
        Vector3 pos = new Vector3(getOriginX(), getOriginY(), 0).mul(batchMatrix.inv());
        effect.setPosition(pos.x, pos.y);

        effect.update(deltaTime);
        effect.draw(batch, deltaTime);
        
        // Adjust thruster angle
        batch.setTransformMatrix(batchMatrix.rotate(0, 0, 1, currentAngle));
    }

    @Override
    public void act(float delta){
        super.act(delta);

        float compRoll = (stateRef.roll == 0) ? stateRef.artifRoll : stateRef.roll;

        this.setTargetAngle(compRoll);
        this.thrust(delta, stateRef.throttle);

        effect.scaleEffect(1 / scale);
        scale = (stateRef.throttle + 0.001f);
        effect.scaleEffect(scale);
    }

    @Override
    public boolean remove(){
        effect.free();
        return super.remove();
    }

    @Override
    public String toString(){
        String str = "Name: " + name;
        str += "\nDesc: " + description;
        str += "\nPower: " + String.valueOf(power);
        str += "\nConeAngle: " + String.valueOf(coneAngle);
        str += "\nFuelUsage: " + String.valueOf(fuelUsage);
        str += "\n";
        return str;
    }

}

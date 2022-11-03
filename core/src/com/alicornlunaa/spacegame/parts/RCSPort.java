package com.alicornlunaa.spacegame.parts;

import java.util.ArrayList;

import com.alicornlunaa.spacegame.App;
import com.alicornlunaa.spacegame.states.ShipState;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.ParticleEffectPool.PooledEffect;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;

/*
 * Mono propellant/RCS ports are decided to be fired depending on the button pressed
 * 
 * Roll can be calculated by getting the angle of the port to the ship's center of
 * mass and checking if its tangent value is positive or negative, just like on a
 * unit circle.
 */

public class RCSPort extends ShipPart {

    // Variables
    private String name;
    private String description;
    private float power;
    private float fuelUsage;

    private float scale = 0.2f;

    private PooledEffect effect;

    // Constructors
    public RCSPort(App game, Body parent, ShipState stateRef, TextureRegion region, float scale, Vector2 pos, float rot, ArrayList<Vector2> attachmentPoints, String name, String description, float denstiy, float power, float fuelUsage){
        super(parent, stateRef, region, scale, pos, rot, attachmentPoints);

        this.name = name;
        this.description = description;
        this.power = power;
        this.fuelUsage = fuelUsage;
        
        effect = game.manager.getEffect("effects/rcs");
        effect.setPosition(0, 0);
        effect.scaleEffect(0.1f);
        effect.start();
    }

    // Functions
    private int getSignAtPos(){
        Vector2 center = parent.getLocalCenter();
        Vector2 posOfCenter = new Vector2(getX() - center.x, getY() - center.y);
        float tanVal = (float)Math.atan(posOfCenter.y / posOfCenter.x);
        return Math.round(Math.abs(tanVal) / tanVal);
    }

    private float verticalThrust(float delta, float direction, Vector2 portPos, Vector2 portDir){
        Vector2 parentVert = new Vector2(0, direction).rotateRad(parent.getAngle());
        float adj = portDir.dot(parentVert);
        if(adj >= 0) return 0.0f;

        parent.applyForceToCenter(new Vector2(portDir).scl(power * 2.f * adj).add(parent.getPosition()), true);

        return adj;
    }

    private float horizontalThrust(float delta, float direction, Vector2 portPos, Vector2 portDir){
        Vector2 parentHori = new Vector2(direction, 0).rotateRad(parent.getAngle());
        float adj = portDir.dot(parentHori);
        if(adj >= 0) return 0.0f;

        parent.applyForceToCenter(new Vector2(portDir).scl(power * 2.f * adj).add(parent.getPosition()), true);

        return adj;
    }

    private float rollThrust(float delta, float direction, Vector2 portPos, Vector2 portDir){
        if(direction >= 0) return 0.0f; // Cant thrust negative

        Vector2 parentRight = new Vector2(1, 0).rotateRad(parent.getAngle());
        float adj = Math.abs(portDir.dot(parentRight)) * -1;

        parent.applyForce(new Vector2(portDir).scl(power * 2.f * adj).add(portPos), portPos, true);
        return adj;
    }
    
    @Override
    protected void drawEffects(Batch batch, float deltaTime){
        if(scale <= 0.01f) return;

        effect.update(deltaTime);
        effect.draw(batch, deltaTime);
    }

    @Override
    public void act(float delta){
        super.act(delta);

        if(stateRef.rcs){
            Vector2 portPos = new Vector2(getX(), getY()).rotateDeg((float)Math.toDegrees(parent.getAngle())).add(parent.getPosition());
            Vector2 portDir = new Vector2(1, 0).rotateDeg(((float)Math.toDegrees(parent.getAngle()) + getRotation()) * getScaleX()).scl(-getScaleX(), -getScaleY());

            float compRoll = (stateRef.roll == 0) ? stateRef.artifRoll : stateRef.roll;

            float thrust = 0.0f;
            thrust += rollThrust(delta, getSignAtPos() * compRoll, portPos, portDir);
            thrust += verticalThrust(delta, stateRef.vertical, portPos, portDir);
            thrust += horizontalThrust(delta, stateRef.horizontal, portPos, portDir);
            thrust = Math.min(Math.max(thrust, -1), 1);

            effect.scaleEffect(1 / scale);
            scale = -0.2f * (thrust + 0.01f);
            effect.scaleEffect(scale);
        } else {
            effect.scaleEffect(1 / scale);
            scale = 0.001f;
            effect.scaleEffect(scale);
        }
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
        str += "\nFuel usage: " + String.valueOf(fuelUsage);
        str += "\n";
        return str;
    }
    
}

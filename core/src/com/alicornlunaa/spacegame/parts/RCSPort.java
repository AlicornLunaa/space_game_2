package com.alicornlunaa.spacegame.parts;

import org.json.JSONObject;

import com.alicornlunaa.spacegame.App;
import com.alicornlunaa.spacegame.objects.Ship.Ship;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.ParticleEmitter;
import com.badlogic.gdx.graphics.g2d.ParticleEffectPool.PooledEffect;
import com.badlogic.gdx.graphics.g2d.ParticleEmitter.ScaledNumericValue;
import com.badlogic.gdx.math.Vector2;

/**
 * Mono propellant/RCS ports are decided to be fired depending on the button pressed
 * 
 * Roll can be calculated by getting the angle of the port to the ship's center of
 * mass and checking if its tangent value is positive or negative, just like on a
 * unit circle.
 */
public class RCSPort extends Part {

    // Variables
    private float power;
    private float fuelUsage;

    private final float initial_scale = 0.14f;
    private float scale = 0.2f;
    private PooledEffect effect;

    // Constructors
    public RCSPort(final App game, final Ship ship, JSONObject obj){
        super(game, ship, obj);

        JSONObject metadata = obj.getJSONObject("metadata");
        power = metadata.getFloat("power");
        fuelUsage = metadata.getFloat("fuelUsage");
        
        effect = game.manager.getEffect("effects/rcs");
        effect.setPosition(0, getHeight() / 2);
        effect.scaleEffect(initial_scale);
        effect.start();
        
        effect.scaleEffect(1 / this.scale);
        this.scale = 0.001f;
        effect.scaleEffect(this.scale);
    }

    // Functions
    private int getSignAtPos(){
        Vector2 center = parent.getLocalCenter();
        Vector2 posOfCenter = new Vector2(getX() / physScale - center.x, getY() / physScale - center.y);
        float tanVal = (float)Math.atan(posOfCenter.y / posOfCenter.x);
        return Math.round(Math.abs(tanVal) / tanVal);
    }

    private float verticalThrust(float delta, float direction, Vector2 portPos, Vector2 portDir){
        Vector2 parentVert = new Vector2(0, direction).rotateRad(parent.getAngle());
        float adj = portDir.dot(parentVert);
        if(adj >= 0) return 0.0f;

        parent.applyForceToCenter(portDir.cpy().scl(power * -adj * delta / physScale), true);

        return adj;
    }

    private float horizontalThrust(float delta, float direction, Vector2 portPos, Vector2 portDir){
        Vector2 parentHori = new Vector2(direction, 0).rotateRad(parent.getAngle());
        float adj = portDir.dot(parentHori);
        if(adj >= 0) return 0.0f;

        parent.applyForceToCenter(portDir.cpy().scl(power * -adj * delta / physScale), true);

        return adj;
    }

    private float rollThrust(float delta, float direction, Vector2 portPos, Vector2 portDir){
        if(direction >= 0) return 0.0f; // Cant thrust negative

        Vector2 parentRight = new Vector2(1, 0).rotateRad(parent.getAngle());
        float adj = Math.abs(portDir.dot(parentRight)) * -1;

        parent.applyForce(portDir.cpy().scl(power * -adj * delta / physScale), portPos, true);
        return adj;
    }
    
    @Override
    protected void drawEffectsBelow(Batch batch, float deltaTime){
        if(scale <= 0.01f) return;

        for(ParticleEmitter emitter : effect.getEmitters()){
            ScaledNumericValue emitterAngle = emitter.getAngle();
            emitterAngle.setHigh(getRotation() - 180);
            emitterAngle.setLow(getRotation() - 180);
        }

        effect.setPosition(getX(), getY());
        effect.setFlip(getFlipX(), getFlipY());
        effect.update(deltaTime);
        effect.draw(batch, deltaTime);
    }

    @Override
    public void update(float delta){
        if(stateRef.rcs){
            Vector2 portPos = new Vector2(getX() / physScale, getY() / physScale).rotateDeg((float)Math.toDegrees(parent.getAngle())).add(parent.getPosition());
            Vector2 portDir = new Vector2(1, 0).rotateDeg(((float)Math.toDegrees(parent.getAngle()) + getRotation()) * getWidth() / 2).scl(getFlipX() ? -1 : 1, getFlipY() ? -1 : 1);

            float compRoll = (stateRef.roll == 0) ? stateRef.artifRoll : stateRef.roll;

            float thrust = 0.0f;
            thrust += rollThrust(delta, getSignAtPos() * compRoll, portPos, portDir);
            thrust += verticalThrust(delta, -stateRef.vertical, portPos, portDir);
            thrust += horizontalThrust(delta, -stateRef.horizontal, portPos, portDir);
            thrust = Math.min(Math.max(thrust, -1), 1);

            effect.scaleEffect(1 / scale);
            scale = -initial_scale * (thrust + 0.001f);
            effect.scaleEffect(scale);
        } else {
            effect.scaleEffect(1 / scale);
            scale = 0.001f;
            effect.scaleEffect(scale);
        }
    }

    @Override
    public void dispose(){
        effect.free();
    }
    
}

package com.alicornlunaa.space_game.components.ship.parts;

import org.json.JSONObject;

import com.alicornlunaa.space_game.App;
import com.alicornlunaa.space_game.objects.ship.Ship;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.ParticleEffectPool;
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
        
        effect = game.manager.get("effects/rcs", ParticleEffectPool.class).obtain();
        effect.setPosition(0, 0);
        effect.scaleEffect(initial_scale);
        effect.start();
        
        effect.scaleEffect(1 / this.scale);
        this.scale = 0.001f;
        effect.scaleEffect(this.scale);
    }

    // Functions
    private int getSignAtPos(){
        Vector2 center = parent.getBody().body.getLocalCenter();
        Vector2 posOfCenter = new Vector2(getPosition().x / parent.getBody().world.getPhysScale() - center.x, getPosition().y / parent.getBody().world.getPhysScale() - center.y);
        float tanVal = (float)Math.atan(posOfCenter.y / posOfCenter.x);
        return Math.round(Math.abs(tanVal) / tanVal);
    }

    private float verticalThrust(float delta, float direction, Vector2 portPos, Vector2 portDir){
        Vector2 parentVert = new Vector2(0, direction).rotateRad(parent.getBody().body.getAngle());
        float adj = portDir.dot(parentVert);
        if(adj >= 0) return 0.0f;

        parent.getBody().body.applyForceToCenter(portDir.cpy().scl(power * -adj * delta / parent.getBody().world.getPhysScale()), true);
        parent.getState().rcsStored -= (fuelUsage * power * -adj * delta);

        return adj;
    }

    private float horizontalThrust(float delta, float direction, Vector2 portPos, Vector2 portDir){
        Vector2 parentHori = new Vector2(direction, 0).rotateRad(parent.getBody().body.getAngle());
        float adj = portDir.dot(parentHori);
        if(adj >= 0) return 0.0f;

        parent.getBody().body.applyForceToCenter(portDir.cpy().scl(power * -adj * delta / parent.getBody().world.getPhysScale()), true);
        parent.getState().rcsStored -= (fuelUsage * power * -adj * delta);

        return adj;
    }

    private float rollThrust(float delta, float direction, Vector2 portPos, Vector2 portDir){
        if(direction >= 0) return 0.0f; // Cant thrust negative

        Vector2 parentRight = new Vector2(1, 0).rotateRad(parent.getBody().body.getAngle());
        float adj = Math.abs(portDir.dot(parentRight)) * -1;

        parent.getBody().body.applyForce(portDir.cpy().scl(power * -adj * delta / parent.getBody().world.getPhysScale()), portPos, true);
        parent.getState().rcsStored -= (fuelUsage * power * -adj * delta);

        return adj;
    }
    
    @Override
    protected void drawEffectsBelow(Batch batch){
        if(scale <= 0.01f) return;

        for(ParticleEmitter emitter : effect.getEmitters()){
            ScaledNumericValue emitterAngle = emitter.getAngle();
            emitterAngle.setHigh(180);
            emitterAngle.setLow(180);
        }

        effect.setPosition(0, 0);
        effect.setFlip(getFlipX(), getFlipY());
        effect.update(Gdx.graphics.getDeltaTime());
        effect.draw(batch, Gdx.graphics.getDeltaTime());
    }

    @Override
    public void tick(float delta){
        if(parent.getState().rcs){
            float scl = parent.getBody().world.getPhysScale();
            Vector2 portPos = getPosition().cpy().scl(1 / scl).sub(parent.getBody().body.getLocalCenter()).rotateRad(parent.getBody().body.getAngle()).add(parent.getBody().body.getWorldCenter());
            Vector2 portDir = new Vector2(getFlipX() ? -1 : 1, 0).rotateRad(parent.getBody().body.getAngle() + (float)Math.toRadians(getRotation()));

            float compRoll = (parent.getState().roll == 0) ? parent.getState().artifRoll : parent.getState().roll;

            float thrust = 0.0f;
            thrust += rollThrust(delta, getSignAtPos() * compRoll, portPos, portDir);
            thrust += verticalThrust(delta, -parent.getState().vertical, portPos, portDir);
            thrust += horizontalThrust(delta, -parent.getState().horizontal, portPos, portDir);
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

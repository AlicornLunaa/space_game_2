package com.alicornlunaa.spacegame.scenes.testing_scene;

import com.alicornlunaa.selene_engine.components.BodyComponent;
import com.alicornlunaa.selene_engine.components.ScriptComponent;
import com.alicornlunaa.selene_engine.components.TransformComponent;
import com.alicornlunaa.selene_engine.core.BaseEntity;
import com.alicornlunaa.selene_engine.core.IEntity;
import com.alicornlunaa.spacegame.objects.simulation.orbits.EllipticalConic;
import com.alicornlunaa.spacegame.util.Constants;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.math.Vector2;

public class KeplerComponent extends ScriptComponent {
    public static final float GRAV_C = 0.0002f;

    private TransformComponent transform = getEntity().getComponent(TransformComponent.class);
    private BodyComponent bodyComponent = getEntity().getComponent(BodyComponent.class);

    public Vector2 velocity = new Vector2();
    public EllipticalConic conic;
    public float time = 0.0f;

    public KeplerComponent(BaseEntity entity, IEntity parent) {
        super(entity);
        conic = new EllipticalConic(parent, entity, 500 / 128.f, 0.0f, 0.0f, 0.0f, 0.0f);
        transform.position.set(conic.getPosition(conic.timeToMeanAnomaly(time - Constants.TIME_STEP)).scl(128));
        bodyComponent.body.setLinearVelocity(conic.getVelocity(conic.timeToMeanAnomaly(time - Constants.TIME_STEP)));
    }

    @Override
    public void start() {}

    @Override
    public void update() {
        Vector2 velocityDifference = bodyComponent.body.getLinearVelocity().cpy().sub(conic.getVelocity(conic.timeToMeanAnomaly(time - Constants.TIME_STEP)));

        if(velocityDifference.len() > 0){
            System.out.println(velocityDifference);
            conic = new EllipticalConic(conic.getParent(), getEntity(), transform.position.cpy().scl(1.f / 128.f), bodyComponent.body.getLinearVelocity().cpy().add(velocityDifference));
        }

        transform.position.set(conic.getPosition(conic.timeToMeanAnomaly(time)).scl(128));
        bodyComponent.body.setLinearVelocity(conic.getVelocity(conic.timeToMeanAnomaly(time)));
        
        time += Constants.TIME_STEP;

        if(Gdx.input.isKeyPressed(Keys.S)){
            bodyComponent.body.applyForceToCenter(0, -0.05f, true);
        } else if(Gdx.input.isKeyPressed(Keys.W)){
            bodyComponent.body.applyForceToCenter(0, 0.05f, true);
        } else if(Gdx.input.isKeyPressed(Keys.A)){
            bodyComponent.body.applyForceToCenter(-0.05f, 0, true);
        } else if(Gdx.input.isKeyPressed(Keys.D)){
            bodyComponent.body.applyForceToCenter(0.05f, 0, true);
        }
    }

    @Override
    public void render() {}
}

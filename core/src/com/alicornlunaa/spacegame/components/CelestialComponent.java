package com.alicornlunaa.spacegame.components;

import com.alicornlunaa.selene_engine.components.BodyComponent;
import com.alicornlunaa.selene_engine.components.ScriptComponent;
import com.alicornlunaa.selene_engine.components.TransformComponent;
import com.alicornlunaa.selene_engine.core.IEntity;
import com.alicornlunaa.selene_engine.phys.PhysWorld;
import com.alicornlunaa.selene_engine.systems.PhysicsSystem;
import com.alicornlunaa.spacegame.objects.simulation.Celestial;
import com.alicornlunaa.spacegame.objects.simulation.orbits.EllipticalConic;
import com.alicornlunaa.spacegame.phys.CelestialPhysWorld;
import com.alicornlunaa.spacegame.util.Constants;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Null;

public class CelestialComponent extends ScriptComponent {
    // Variables
    private TransformComponent transform = getEntity().getComponent(TransformComponent.class);
    private BodyComponent bodyComponent = getEntity().getComponent(BodyComponent.class);
    private Body influenceBody; // Static body

    public float radius = 100.f;
    public PhysWorld influenceWorld;
    
    public @Null Celestial parent = null;
    public Array<IEntity> children = new Array<>();
    public @Null EllipticalConic conic;
    public float elapsedTime = 0.0f;
    
    // Constructor
    public CelestialComponent(Celestial entity, PhysicsSystem phys, float radius) {
        super(entity);
        this.radius = radius;

        influenceWorld = phys.addWorld(new CelestialPhysWorld(entity, Constants.PPM));

        CircleShape shape = new CircleShape();
        shape.setRadius(radius / influenceWorld.getPhysScale());
        influenceBody = influenceWorld.getBox2DWorld().createBody(new BodyDef());
        influenceBody.createFixture(shape, 1.0f);
        shape.dispose();
    }

    // Functions
    public float getRadius(){
        return radius;
    }

    public float getSphereOfInfluence(){
        if(conic != null){
            return (float)((conic.getSemiMajorAxis() * bodyComponent.world.getPhysScale()) * Math.pow(bodyComponent.body.getMass() / conic.getParent().getComponent(BodyComponent.class).body.getMass(), 2.f/5.f));
        }

        return radius * 4;
    }
    
    @Override
    public void start() {
    }

    @Override
    public void update(){
        // Update offset
        influenceWorld.getOffset().set(transform.position);

        // Update rails
        if(conic != null){
            elapsedTime += Gdx.graphics.getDeltaTime();
            double anomaly = conic.timeToMeanAnomaly(elapsedTime);

            if(!Double.isNaN(anomaly)){
                transform.position.set(conic.getPosition(anomaly).scl(bodyComponent.world.getPhysScale()));
                transform.position.add(conic.getParent().getComponent(TransformComponent.class).position);
                bodyComponent.body.setLinearVelocity(conic.getVelocity(anomaly));
            }
        }
    }

    @Override
    public void render() {
    }
}

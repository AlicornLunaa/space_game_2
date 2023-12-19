package com.alicornlunaa.selene_engine.phys;

import com.alicornlunaa.selene_engine.ecs.BodyComponent;
import com.alicornlunaa.space_game.util.Constants;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.Shape;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Array;

/** Contains Box2D world and fixed timestep accumulator. Each world has unique scale */
public class PhysWorld {
    // Variables
    private World box2DWorld;
    private float physScale;

    // Constructor
    public PhysWorld(float physScale){
        box2DWorld = new World(new Vector2(0, 0), true);
        this.physScale = physScale;
    }

    // Functions
    public float getPhysScale(){ return physScale; }

    public World getBox2DWorld(){ return box2DWorld; }
    
    public void transferBody(BodyComponent bodyComp){
        // Save all data and prep to load to the new world
        Array<FixtureDef> fixtures = new Array<>(bodyComp.body.getFixtureList().size);
        Array<Shape> shapes = new Array<>(bodyComp.body.getFixtureList().size);
        float newPhysScale = getPhysScale();

        for(Fixture f : bodyComp.body.getFixtureList()){
            FixtureDef fixtureDef = new FixtureDef();
            fixtureDef.density = f.getDensity();
            fixtureDef.filter.set(f.getFilterData());
            fixtureDef.friction = f.getFriction();
            fixtureDef.isSensor = f.isSensor();
            fixtureDef.restitution = f.getRestitution();

            // Recreate the shape
            if(f.getShape().getType() == Shape.Type.Polygon){
                PolygonShape shape = (PolygonShape)f.getShape();
                PolygonShape copy = new PolygonShape();
                Vector2[] vertices = new Vector2[shape.getVertexCount()];

                for(int i = 0; i < shape.getVertexCount(); i++){
                    vertices[i] = new Vector2();
                    shape.getVertex(i, vertices[i]);

                    vertices[i].scl(bodyComp.world.getPhysScale());
                    vertices[i].scl(1 / newPhysScale);
                }
                
                copy.set(vertices);
                shapes.add(copy);
                fixtureDef.shape = copy;
            } else if(f.getShape().getType() == Shape.Type.Circle) {
                CircleShape shape = (CircleShape)f.getShape();
                CircleShape copy = new CircleShape();

                copy.setPosition(shape.getPosition().scl(bodyComp.world.getPhysScale()).scl(1 / newPhysScale));
                copy.setRadius(shape.getRadius() * bodyComp.world.getPhysScale() / newPhysScale);
                
                shapes.add(copy);
                fixtureDef.shape = copy;
            }

            fixtures.add(fixtureDef);
        }

        // Save body data
        BodyDef def = new BodyDef();
        def.active = bodyComp.body.isActive();
        def.allowSleep = bodyComp.body.isSleepingAllowed();
        def.angle = bodyComp.body.getAngle();
        def.angularDamping = bodyComp.body.getAngularDamping();
        def.angularVelocity = bodyComp.body.getAngularVelocity();
        def.awake = bodyComp.body.isAwake();
        def.bullet = bodyComp.body.isBullet();
        def.fixedRotation = bodyComp.body.isFixedRotation();
        def.gravityScale = bodyComp.body.getGravityScale();
        def.linearDamping = bodyComp.body.getLinearDamping();
        def.linearVelocity.set(bodyComp.body.getLinearVelocity().cpy().scl(bodyComp.world.getPhysScale() / newPhysScale));
        def.position.set(bodyComp.body.getPosition().cpy().scl(bodyComp.world.getPhysScale() / newPhysScale));
        def.type = bodyComp.body.getType();

        // Delete bodyComp.body from world and recreate it in the new one
        bodyComp.body.getWorld().destroyBody(bodyComp.body);
        bodyComp.body = getBox2DWorld().createBody(def);

        // Recreate the fixtures
        for(FixtureDef fixtureDef : fixtures){
            bodyComp.body.createFixture(fixtureDef);
        }

        // Remove shapes
        for(Shape s : shapes){
            s.dispose();
        }

        bodyComp.world = this;
    }

    public void update(){
        // Step the physics on the world
        box2DWorld.step(Constants.TIME_STEP, Constants.VELOCITY_ITERATIONS, Constants.POSITION_ITERATIONS);
    }
}

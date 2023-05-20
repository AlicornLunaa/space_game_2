package com.alicornlunaa.selene_engine.components;

import com.alicornlunaa.selene_engine.ecs.IComponent;
import com.alicornlunaa.selene_engine.phys.PhysWorld;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.Shape;
import com.badlogic.gdx.utils.Array;

public class BodyComponent implements IComponent {

    // Variables
    public Body body;
    public PhysWorld world;

    // Constructor
    public BodyComponent(PhysWorld world, BodyDef def){
        body = world.getBox2DWorld().createBody(def);
        this.world = world;
    }

    // Functions
    public void setWorld(PhysWorld world){
        // You can only load an entity to a world if it has a body
        if(world == null){ this.world = world; return; }

        // Save all data and prep to load to the new world
        Array<FixtureDef> fixtures = new Array<>(body.getFixtureList().size);
        Array<Shape> shapes = new Array<>(body.getFixtureList().size);
        float newPhysScale = world.getPhysScale();

        for(Fixture f : body.getFixtureList()){
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

                    vertices[i].scl(this.world.getPhysScale());
                    vertices[i].scl(1 / newPhysScale);
                }
                
                copy.set(vertices);
                shapes.add(copy);
                fixtureDef.shape = copy;
            } else if(f.getShape().getType() == Shape.Type.Circle) {
                CircleShape shape = (CircleShape)f.getShape();
                CircleShape copy = new CircleShape();

                copy.setPosition(shape.getPosition().scl(this.world.getPhysScale()).scl(1 / newPhysScale));
                copy.setRadius(shape.getRadius() * this.world.getPhysScale() / newPhysScale);
                
                shapes.add(copy);
                fixtureDef.shape = copy;
            }

            fixtures.add(fixtureDef);
        }

        // Save body data
        BodyDef def = new BodyDef();
        def.active = body.isActive();
        def.allowSleep = body.isSleepingAllowed();
        def.angle = body.getAngle();
        def.angularDamping = body.getAngularDamping();
        def.angularVelocity = body.getAngularVelocity();
        def.awake = body.isAwake();
        def.bullet = body.isBullet();
        def.fixedRotation = body.isFixedRotation();
        def.gravityScale = body.getGravityScale();
        def.linearDamping = body.getLinearDamping();
        def.linearVelocity.set(body.getLinearVelocity().cpy().scl(this.world.getPhysScale()).scl(1 / newPhysScale));
        def.position.set(body.getPosition().cpy().scl(this.world.getPhysScale()).scl(1 / newPhysScale));
        def.type = body.getType();

        // Delete body from world and recreate it in the new one
        body.getWorld().destroyBody(body);
        body = world.getBox2DWorld().createBody(def);

        // Recreate the fixtures
        for(FixtureDef fixtureDef : fixtures){
            body.createFixture(fixtureDef);
        }

        // Remove shapes
        for(Shape s : shapes){
            s.dispose();
        }

        this.world = world;
        afterWorldChange(world);
    }
    
    public void afterWorldChange(PhysWorld world){}

}

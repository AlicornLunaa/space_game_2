package com.alicornlunaa.spacegame.objects;

import org.json.JSONArray;

import com.alicornlunaa.spacegame.scenes.dev.physics_editor.Collider;
import com.badlogic.gdx.math.Matrix3;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.utils.Array;

/**
 * This class will hold vertex points, a position, scale, and rotation
 * in order to be able to create a physics collider on a body
 */
public class PhysicsCollider {

    // Variables
    private Collider collider = new Collider();
    private Vector2 position = new Vector2();
    private Vector2 scale = new Vector2(1, 1);
    private float rotation = 0.0f;

    private Body bodyRef;
    private Array<Fixture> fixtures = new Array<>();

    // Constructor
    public PhysicsCollider(){}

    public PhysicsCollider(JSONArray obj){
        collider = Collider.unserialize(obj);
    }

    public PhysicsCollider(PhysicsCollider pc){
        collider = new Collider(pc.collider);
        position = pc.position.cpy();
        scale = pc.scale.cpy();
        rotation = pc.rotation;
    }

    // Functions
    public Body attachCollider(Body b){
        // Remove existing body
        if(bodyRef != null){
            for(Fixture f : fixtures){
                if(f == null) continue;
                bodyRef.destroyFixture(f);
            }

            fixtures.clear();
        }

        // Attach to new body
        Matrix3 trans = new Matrix3().translate(position).rotate(rotation).scale(scale);
        bodyRef = b;

        for(int i = 0; i < collider.getShapeCount(); i++){
            Vector2[] vertexData = new Vector2[collider.getIndexCount(i)];

            for(int j = 0; j < collider.getIndexCount(i); j++){
                Vector2 v = collider.getVertex(i, collider.getIndex(i, j));
                vertexData[j] = v.cpy().mul(trans);
            }

            PolygonShape physShape = new PolygonShape();
            physShape.set(vertexData);
            
            FixtureDef def = new FixtureDef();
            def.shape = physShape;
            def.friction = collider.getFriction(i);
            def.restitution = collider.getDensity(i);
            def.density = collider.getDensity(i);
            def.isSensor = collider.getSensor(i);
            b.createFixture(def);

            physShape.dispose();
        }

        return b;
    }

    public void detachCollider(){
        // Remove existing body
        if(bodyRef != null){
            for(Fixture f : fixtures){
                if(f == null) continue;
                bodyRef.destroyFixture(f);
            }

            fixtures.clear();
        }
    }

    public void reattach(){ attachCollider(bodyRef); }

    // Getters & setters
    public Vector2 getPosition() { return position; }
    public void setPosition(Vector2 position) { this.position = position; }
    public Vector2 getScale() { return scale; }
    public void setScale(Vector2 scale) { this.scale = scale; }
    public void setScale(float scale) { this.scale.set(scale, scale); }
    public float getRotation() { return rotation; }
    public void setRotation(float rotation) { this.rotation = rotation; }

    // Static functions
    public static PhysicsCollider box(Vector2 pos, Vector2 size, float rotation){
        PhysicsCollider c = new PhysicsCollider();
        c.collider.addShape();
        c.collider.addVertex(0, new Vector2(-1, -1));
        c.collider.addVertex(0, new Vector2(1, -1));
        c.collider.addVertex(0, new Vector2(1, 1));
        c.collider.addVertex(0, new Vector2(-1, 1));
        return c;
    }
    
}

package com.alicornlunaa.spacegame.objects;

import org.json.JSONArray;

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
    private Array<Array<Vector2>> vertices = new Array<>();
    private Vector2 position = new Vector2();
    private Vector2 scale = new Vector2(1, 1);
    private float rotation = 0.0f;

    private Body bodyRef;
    private Array<Fixture> fixtures = new Array<>();

    // Constructor
    public PhysicsCollider(){}
    
    public PhysicsCollider(Vector2[] vertices){
        this.vertices.add(new Array<Vector2>());
        for(int i = 0; i < vertices.length; i++){
            this.vertices.get(this.vertices.size - 1).add(vertices[i]);
        }
    }

    public PhysicsCollider(JSONArray object){
        /** JSONObject format is just an array containing arrays of floats */
        for(int i = 0; i < object.length(); i++){
            Array<Vector2> arr = new Array<>();

            for(int j = 0; j < object.getJSONArray(i).length(); j += 2){
                arr.add(new Vector2(object.getJSONArray(i).getFloat(j), object.getJSONArray(i).getFloat(j + 1)));
            }

            vertices.add(arr);
        }
    }

    public PhysicsCollider(PhysicsCollider pc){
        for(Array<Vector2> shape : pc.vertices){
            vertices.add(new Array<Vector2>());

            for(Vector2 vertex : shape){
                vertices.get(vertices.size - 1).add(vertex.cpy());
            }
        }

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

        for(Array<Vector2> shape : vertices){
            Vector2[] verticesTransformed = new Vector2[shape.size];
            for(int i = 0; i < shape.size; i++){
                verticesTransformed[i] = shape.get(i).cpy().mul(trans);
            }

            PolygonShape physShape = new PolygonShape();
            physShape.set(verticesTransformed);
            
            FixtureDef def = new FixtureDef();
            def.shape = physShape;
            def.density = 0.1f;
            b.createFixture(def);

            physShape.dispose();
        }

        return b;
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
        Vector2[] vertices = { new Vector2(-1, -1), new Vector2(1, -1), new Vector2(1, 1), new Vector2(-1, 1) };
        return new PhysicsCollider(vertices);
    }
    
}

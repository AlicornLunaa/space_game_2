package com.alicornlunaa.spacegame.objects;

import org.json.JSONObject;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.utils.Array;

/**
 * This class will hold vertex points, a position, scale, and rotation
 * in order to be able to create a physics collider on a body
 */
public class PhysicsCollider {

    // Variables
    private Array<Vector2> vertices = new Array<>();
    private Vector2 position = new Vector2();
    private Vector2 scale = new Vector2(1, 1);
    private float rotation = 0.0f;

    private Body bodyRef;
    private Fixture fixture;

    // Constructor
    public PhysicsCollider(){}
    
    public PhysicsCollider(Array<Vector2> vertices){
        this.vertices = vertices;
    }

    public PhysicsCollider(JSONObject object){
        // TODO: Implement JSONObject constructor
    }

    public PhysicsCollider(PhysicsCollider pc){
        for(Vector2 v : pc.vertices){
            vertices.add(v.cpy());
        }

        position = pc.position.cpy();
        scale = pc.scale.cpy();
        rotation = pc.rotation;
    }

    // Functions
    public Body attachCollider(Body b){
        // TODO: Implement method to attach vertices with transform
        bodyRef = b;
        return b;
    }
    
}

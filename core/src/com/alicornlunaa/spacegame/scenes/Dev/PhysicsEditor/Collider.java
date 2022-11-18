package com.alicornlunaa.spacegame.scenes.Dev.PhysicsEditor;

import org.json.JSONArray;

import com.badlogic.gdx.math.DelaunayTriangulator;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ShortArray;

class Collider {

    // Variables
    protected Array<Vector2> vertices = new Array<>();
    protected Array<Vector2> triangles = new Array<>();

    // Constructors
    protected Collider(){}

    // Functions
    protected Array<Vector2> triangulate(){
        float[] rawVertices = new float[vertices.size * 2];
        for(int i = 0; i < vertices.size; i++){
            rawVertices[i * 2] = vertices.get(i).x;
            rawVertices[i * 2 + 1] = vertices.get(i).y;
        }

        DelaunayTriangulator triangulator = new DelaunayTriangulator();
        ShortArray res = triangulator.computeTriangles(rawVertices, false);

        triangles.clear();
        for(int i = 0; i < res.size; i++){
            triangles.add(vertices.get(res.get(i)).cpy());
        }

        return triangles;
    }

    protected JSONArray serialize(){
        JSONArray arr = new JSONArray();
        
        for(Vector2 v : triangulate()){
            arr.put(v.x);
            arr.put(v.y);
        }

        return arr;
    }

    protected static Collider unserialize(JSONArray arr){
        Collider c = new Collider();

        for(int i = 0; i < arr.length(); i += 2){
            Vector2 p = new Vector2(arr.getFloat(i), arr.getFloat(i + 1));
            
            if(!c.vertices.contains(p, false)){
                c.vertices.add(new Vector2(p));
            }
        }

        return c;
    }
    
}

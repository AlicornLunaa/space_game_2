package com.alicornlunaa.spacegame.scenes.dev.part_editor;

import org.json.JSONArray;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;

/** This class draws a polygon */
public class PhysShape {

    // Variables
    private final ShapeRenderer render;
    
    public Array<Vector2> vertices = new Array<>();
    public Array<Vector2> hull = new Array<>();
    public int disableWhen = -1; // The index of the attachment point to disable this collider when attached
    public float friction = 1.0f;
    public float restitution = 0.0f;
    public float density = 0.0f;

    // Constructors
    public PhysShape(ShapeRenderer render){
        this.render = render;
    }

    public PhysShape(ShapeRenderer render, PhysShape ps){
        this(render);

        for(Vector2 v : ps.vertices){
            vertices.add(v.cpy());
        }

        disableWhen = ps.disableWhen;
    }

    // Functions
    public void draw(){
        render.circle(0, 0, 20.0f);
    }

    public JSONArray serialize(){
        JSONArray arr = new JSONArray();
        
        for(Vector2 v : calculateHull()){
            arr.put(v.x);
            arr.put(v.y);
        }

        return arr;
    }

    private int orientation(Vector2 p, Vector2 q, Vector2 r){
        float val = (int)((q.y - p.y) * (r.x - q.x) - (q.x - p.x) * (r.y - q.y));
        if(val == 0) return 0;
        return (val > 0) ? 1 : 2;
    }

    public Array<Vector2> calculateHull(){
        if(vertices.size == 0) return new Array<>();

        // Start with left-most point
        int min = 0;
        float minX = vertices.get(min).x;
        for(int i = 1; i < vertices.size; i++){
            if(vertices.get(i).x < minX){
                min = i;
                minX = vertices.get(i).x;
            }
        }

        // Find next point
        hull.clear();
        
        if(vertices.size >= 3){
            int p = min;
            int q;

            do {
                hull.add(vertices.get(p));
                q = (p + 1) % vertices.size;
                
                for(int i = 0; i < vertices.size; i++){
                    if(orientation(vertices.get(p), vertices.get(i), vertices.get(q)) == 2){
                        q = i;
                    }
                }

                p = q;
            } while(p != min);
        }

        return hull;
    }

    public static PhysShape unserialize(ShapeRenderer render, JSONArray arr){
        PhysShape s = new PhysShape(render);

        for(int i = 0; i < arr.length(); i += 2){
            s.vertices.add(new Vector2(arr.getFloat(i), arr.getFloat(i + 1)));
        }

        return s;
    }
}
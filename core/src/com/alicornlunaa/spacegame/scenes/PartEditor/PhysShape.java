package com.alicornlunaa.spacegame.scenes.PartEditor;

import org.json.JSONArray;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;

/** This class draws a polygon */
class PhysShape {

    // Variables
    private final ShapeRenderer render;
    
    Array<Vector2> vertices = new Array<>();
    int disableWhen = -1; // The index of the attachment point to disable this collider when attached

    // Constructors
    PhysShape(ShapeRenderer render){
        this.render = render;
    }

    PhysShape(ShapeRenderer render, PhysShape ps){
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

    JSONArray serialize(){
        JSONArray arr = new JSONArray();
        
        for(Vector2 v : vertices){
            arr.put(v.x);
            arr.put(v.y);
        }

        return arr;
    }

    public static PhysShape unserialize(ShapeRenderer render, JSONArray arr){
        PhysShape s = new PhysShape(render);

        for(int i = 0; i < arr.length(); i += 2){
            s.vertices.add(new Vector2(arr.getFloat(i), arr.getFloat(i + 1)));
        }

        return s;
    }
}
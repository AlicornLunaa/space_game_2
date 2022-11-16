package com.alicornlunaa.spacegame.scenes.PartEditor;

import org.json.JSONArray;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.utils.Array;

/** This class draws a polygon */
class PhysShape extends Actor {

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
    @Override
    public void draw(Batch batch, float parentAlpha){
        render.circle(0, 0, 2.0f);
    }

    JSONArray serialize(){
        JSONArray arr = new JSONArray();
        
        for(Vector2 v : vertices){
            arr.put(v.x);
            arr.put(v.y);
        }

        return arr;
    }
}
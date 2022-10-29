package com.alicornlunaa.spacegame.panels;

import com.alicornlunaa.spacegame.objects.Ship;
import com.alicornlunaa.spacegame.util.Assets;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;

/*
 * This class holds the ship view and sprites
 */
public class EditorPane extends Actor {
    // Variables
    private Ship shipRef;
    
    // Constructor
    public EditorPane(final Assets manager, Skin skin, Ship shipRef){
        this.shipRef = shipRef;
    }

    // Methods
    @Override
    public void draw(Batch batch, float parentAlpha){
        super.draw(batch, parentAlpha);
        
        Color c = getColor();
        batch.setColor(c.r, c.g, c.b, c.a * parentAlpha);
        // batch.draw(
        //     texture,
        //     getX(),
        //     getY(),
        //     getOriginX(),
        //     getOriginY(),
        //     getWidth(),
        //     getHeight(),
        //     getScaleX(),
        //     getScaleY(),
        //     getRotation()
        // );

        batch.setTransformMatrix(new Matrix4(new Vector3(getX(), getY(), 0), new Quaternion(), new Vector3(1, 1, 1)));
        shipRef.draw(batch, parentAlpha);
    }
    
    @Override
    public boolean remove(){
        return super.remove();
    }
}

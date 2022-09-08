package com.alicornlunaa.spacegame.panels;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;

/*
 * This class holds the ship view and sprites
 */
public class EditorPane extends Actor {
    // Variables
    private TextureRegion texture;
    
    // Constructor
    public EditorPane(Skin skin){
        texture = new TextureRegion(new Texture(Gdx.files.internal("editor_ui_mockup.png")));
    }

    // Methods
    @Override
    public void draw(Batch batch, float parentAlpha){
        super.draw(batch, parentAlpha);
        
        Color c = getColor();
        batch.setColor(c.r, c.g, c.b, c.a * parentAlpha);
        batch.draw(
            texture,
            getX(),
            getY(),
            getOriginX(),
            getOriginY(),
            getWidth(),
            getHeight(),
            getScaleX(),
            getScaleY(),
            getRotation()
        );
    }
    
    @Override
    public boolean remove(){
        return super.remove();
    }
}

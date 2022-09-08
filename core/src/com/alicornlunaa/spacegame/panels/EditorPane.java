package com.alicornlunaa.spacegame.panels;

import com.alicornlunaa.spacegame.util.Assets;
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
    public EditorPane(final Assets manager, Skin skin){
        texture = new TextureRegion(manager.get("textures/editor_ui_mockup.png", Texture.class));
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
    }
    
    @Override
    public boolean remove(){
        return super.remove();
    }
}

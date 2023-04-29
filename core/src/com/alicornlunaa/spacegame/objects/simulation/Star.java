package com.alicornlunaa.spacegame.objects.simulation;

import com.alicornlunaa.spacegame.App;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.utils.Disposable;

public class Star extends Celestial implements Disposable {

    // Variables
    // private final App game;

    // private float temperature = 0.0f;
    private Pixmap pixmap;
    private Texture starTexture;
    private ShaderProgram shader;

    // Private functions
    public void generateSprite(){
        pixmap = new Pixmap(2000, 2000, Format.RGBA8888);
        pixmap.setColor(Color.WHITE);
        pixmap.fill();
        starTexture = new Texture(pixmap);
        pixmap.dispose();
    }

    // Constructor
    public Star(App game, float x, float y, float radius){
        super(game, radius);
        generateSprite();
        setPosition(x, y);

        shader = game.manager.get("shaders/star", ShaderProgram.class);
    }

    // Functions
    @Override
    public void render(Batch batch){
        batch.setShader(shader);
        batch.draw(starTexture, radius * -1, radius * -1, radius * 2, radius * 2);
        batch.setShader(null);
    }

    @Override
    public void dispose(){
        starTexture.dispose();
        shader.dispose();
    }
    
}

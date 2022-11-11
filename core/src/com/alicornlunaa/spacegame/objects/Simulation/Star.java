package com.alicornlunaa.spacegame.objects.Simulation;

import com.alicornlunaa.spacegame.App;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.physics.box2d.World;

public class Star extends Celestial {

    // Variables
    // private final App game;

    // private float temperature = 0.0f;
    private Pixmap pixmap;
    private Texture starTexture;
    private ShaderProgram shader;

    // Private functions
    public void generateSprite(){
        pixmap = new Pixmap(Math.min((int)radius * 2, 2000), Math.min((int)radius * 2, 2000), Format.RGBA8888);
        pixmap.setColor(Color.WHITE);
        pixmap.fill();

        // for(int x = 0; x < pixmap.getWidth(); x++){
        //     for(int y = 0; y < pixmap.getHeight(); y++){
        //         int mX = x - pixmap.getWidth() / 2;
        //         int mY = y - pixmap.getHeight() / 2;
        //         int radSqr = mX * mX + mY * mY;
        //         Color c = new Color(1, 1, 1, 1);

        //         if(radSqr > (float)Math.pow(pixmap.getWidth() / 2, 2)) c.a = 0;

        //         pixmap.setColor(c);
        //         pixmap.drawPixel(x, y);
        //     }
        // }

        starTexture = new Texture(pixmap);
        pixmap.dispose();
    }

    // Constructor
    public Star(final App game, final World world, float x, float y, float radius){
        super(game, world, radius);
        generateSprite();
        setPosition(x, y);

        shader = new ShaderProgram(Gdx.files.internal("shaders/star/vertex.glsl"), Gdx.files.internal("shaders/star/fragment.glsl"));
    }

    // Functions
    public void reloadShaders(){
        shader.dispose();
        shader = new ShaderProgram(Gdx.files.internal("shaders/star/vertex.glsl"), Gdx.files.internal("shaders/star/fragment.glsl"));
    }

    @Override
    public void draw(Batch batch, float parentAlpha){
        super.draw(batch, parentAlpha);
        batch.setShader(shader);
        batch.draw(starTexture, radius * -1, radius * -1, radius * 2, radius * 2);
        batch.setShader(null);
    }

    @Override
    public boolean remove(){
        starTexture.dispose();
        shader.dispose();
        return super.remove();
    }
    
}

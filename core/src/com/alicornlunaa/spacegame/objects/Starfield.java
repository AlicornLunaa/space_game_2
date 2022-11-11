package com.alicornlunaa.spacegame.objects;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture.TextureWrap;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;

/**
 * Starfield is a special drawable where the texture
 * is just a background of stars
 */
public class Starfield implements Drawable {

    // Variables
    private Texture texture;
    private ShaderProgram shader;
    private float x = 0.0f;
    private float y = 0.0f;

    // Constructor
    public Starfield(int width, int height){
        Pixmap map = new Pixmap(width, height, Format.RGBA8888);
        map.setColor(new Color(0.1f, 0.1f, 0.1f, 1.0f));
        map.fill();
        map.setColor(Color.WHITE);
        for(int x = 0; x < width; x++) for(int y = 0; y < height; y++){
            if(Math.random() < 0.0005){
                map.drawPixel(x, y);
            }
        }
        texture = new Texture(map);
        texture.setWrap(TextureWrap.Repeat, TextureWrap.Repeat);
        map.dispose();

        shader = new ShaderProgram(
            Gdx.files.internal("shaders/starfield/vertex.glsl"),
            Gdx.files.internal("shaders/starfield/fragment.glsl")
        );
    }

    // Functions
    public void setOffset(float x, float y){
        this.x = x;
        this.y = y;
    }

    @Override
    public void draw(Batch batch, float x, float y, float width, float height) {
        batch.setShader(shader);
        shader.setUniformf("x", this.x);
        shader.setUniformf("y", this.y);
        batch.draw(texture, x, y, width, height);
        batch.setShader(null);
    }

    @Override
    public float getLeftWidth() { return 10; }

    @Override
    public void setLeftWidth(float leftWidth) {}

    @Override
    public float getRightWidth() { return 10; }

    @Override
    public void setRightWidth(float rightWidth) {}

    @Override
    public float getTopHeight() { return 10; }

    @Override
    public void setTopHeight(float topHeight) {}

    @Override
    public float getBottomHeight() { return 10; }

    @Override
    public void setBottomHeight(float bottomHeight) {}

    @Override
    public float getMinWidth() { return 128; }

    @Override
    public void setMinWidth(float minWidth) {}

    @Override
    public float getMinHeight() { return 128; }

    @Override
    public void setMinHeight(float minHeight) {}

}

package com.alicornlunaa.spacegame.objects.simulation;

import com.alicornlunaa.selene_engine.phys.PhysWorld;
import com.alicornlunaa.spacegame.App;
import com.alicornlunaa.spacegame.components.CustomSpriteComponent;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;

public class Star extends Celestial {

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
    public Star(App game, PhysWorld world, float x, float y, float radius){
        super(game, world, radius);
        generateSprite();

        float ppm = bodyComponent.world.getPhysScale();
        bodyComponent.body.setTransform(x / ppm, y / ppm, bodyComponent.body.getAngle());
        transform.position.set(x, y);
        transform.dp.set(x, y);

        addComponent(new CustomSpriteComponent() {
            @Override
            public void render(Batch batch) {
                batch.setShader(shader);
                batch.draw(starTexture, getRadius() * -1, getRadius() * -1, getRadius() * 2, getRadius() * 2);
                batch.setShader(null);
            }
        });

        shader = game.manager.get("shaders/star", ShaderProgram.class);
    }

    // Functions
    @Override
    public void dispose(){
        starTexture.dispose();
        shader.dispose();
    }
    
}

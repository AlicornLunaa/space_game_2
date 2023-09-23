package com.alicornlunaa.spacegame.objects.simulation;

import com.alicornlunaa.selene_engine.phys.PhysWorld;
import com.alicornlunaa.spacegame.App;
import com.alicornlunaa.spacegame.components.CelestialComponent;
import com.alicornlunaa.spacegame.components.CustomSpriteComponent;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;

public class Star extends Celestial {
    // Variables
    // private float temperature = 0.0f;
    private Pixmap pixmap;
    private Texture starTexture;
    private ShaderProgram shader;
    private CelestialComponent celestialComponent = getComponent(CelestialComponent.class);

    // Private functions
    public void generateSprite(){
        pixmap = new Pixmap(2000, 2000, Format.RGBA8888);
        pixmap.setColor(Color.WHITE);
        pixmap.fill();
        starTexture = new Texture(pixmap);
        pixmap.dispose();
    }

    // Constructor
    public Star(PhysWorld world, float radius, float x, float y){
        super(world, radius, x, y);
        generateSprite();

        addComponent(new CustomSpriteComponent() {
            @Override
            public void render(Batch batch) {
                batch.setShader(shader);
                batch.draw(starTexture, celestialComponent.radius * -1, celestialComponent.radius * -1, celestialComponent.radius * 2, celestialComponent.radius * 2);
                batch.setShader(null);
            }
        });

        shader = App.instance.manager.get("shaders/star", ShaderProgram.class);
    }

    // Functions
    @Override
    public void dispose(){
        starTexture.dispose();
    }
}

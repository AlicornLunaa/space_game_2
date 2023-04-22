package com.alicornlunaa.spacegame.engine.lighting;

import com.alicornlunaa.spacegame.App;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.ScreenUtils;

/** Point light class to allow shadows and lighting */
public class PointLight {

    // Variables
    private ShaderProgram shadowShader;
    private ShaderProgram lightShader;
        
    private Vector2 position;
    private int resolution = 512;

    private OrthographicCamera lightCamera;
    private FrameBuffer occlusionFBO;
    private FrameBuffer shadowMapFBO;

    // Constructor
    public PointLight(final App game, Vector2 position){
        shadowShader = game.manager.get("shaders/shadow_map", ShaderProgram.class);
        lightShader = game.manager.get("shaders/light", ShaderProgram.class);

        this.position = position;

        lightCamera = new OrthographicCamera();
        occlusionFBO = new FrameBuffer(Format.RGBA8888, resolution, resolution, false);
        shadowMapFBO = new FrameBuffer(Format.RGBA8888, resolution, 1, false);
    }

    // Getters & setters
    public void setPosition(Vector2 position){ this.position.set(position); }
    public void setResolution(int resolution){ this.resolution = resolution; }
    public Vector2 getPosition(){ return position; }
    public int getResolution(){ return resolution; }

    // Functions
    public void beginOcclusion(Batch batch){
        if(batch.isDrawing()) batch.end();

        occlusionFBO.begin();
        ScreenUtils.clear(0, 0, 0, 1);
        lightCamera.setToOrtho(false, occlusionFBO.getWidth(), occlusionFBO.getHeight());
        lightCamera.translate(position.x - resolution / 2.f, position.y - resolution / 2.f);
        lightCamera.update();
        batch.setProjectionMatrix(lightCamera.combined);
        batch.setShader(null);
        batch.begin();
    }

    public void endOcclusion(Batch batch){
        batch.end();
        occlusionFBO.end();

        shadowMapFBO.begin();
        ScreenUtils.clear(0, 0, 0, 1);
        batch.setShader(shadowShader);
        batch.begin();
        shadowShader.setUniformf("u_resolution", resolution, resolution);
        lightCamera.setToOrtho(false, shadowMapFBO.getWidth(), shadowMapFBO.getHeight());
        batch.setProjectionMatrix(lightCamera.combined);
        batch.draw(occlusionFBO.getColorBufferTexture(), 0, 0, resolution, shadowMapFBO.getHeight());
        batch.end();
        shadowMapFBO.end();
    }

    public void drawLight(Batch batch){
        batch.setShader(lightShader);
        lightShader.setUniformf("u_resolution", resolution, resolution);
        batch.draw(shadowMapFBO.getColorBufferTexture(), position.x - resolution / 2.f, position.y - resolution / 2.f, resolution, resolution);
    }
    
}

package com.alicornlunaa.spacegame.scenes.planet_scene;

import com.alicornlunaa.spacegame.App;
import com.alicornlunaa.spacegame.engine.core.BaseEntity;
import com.alicornlunaa.spacegame.objects.blocks.Tile;
import com.alicornlunaa.spacegame.objects.planet.Planet;
import com.alicornlunaa.spacegame.objects.planet.WorldBody;
import com.alicornlunaa.spacegame.util.Constants;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.viewport.FillViewport;

public class PlanetPanel extends Stage {
    
    // Variables
    private final App game;
    private Planet planet;

    private Texture texture;
    private ShaderProgram cartesianAtmosShader;

    // Functions
    private void generateTexture(){
        Pixmap p = new Pixmap(1, 1, Format.RGBA8888);
        p.setColor(Color.WHITE);
        p.fill();
        texture = new Texture(p);
        p.dispose();
    }

    // Constructor
    public PlanetPanel(final App game, final Planet planet){
        super(new FillViewport(1280, 720));
        this.game = game;
        this.planet = planet;

        cartesianAtmosShader = game.manager.get("shaders/cartesian_atmosphere", ShaderProgram.class);
        generateTexture();
        getViewport().setCamera(game.activeCamera);

        // Controls
        this.addListener(new InputListener(){
            @Override
            public boolean keyDown(InputEvent event, int keycode){
                return false;
            }

            @Override
            public boolean scrolled(InputEvent event, float x, float y, float amountX, float amountY){
                OrthographicCamera cam = (OrthographicCamera)getCamera();
                cam.zoom = Math.min(Math.max(cam.zoom + (amountY / 50), 0.05f), 10.5f);
                return true;
            }
        });
    }

    // Functions
    public Planet getPlanet(){ return planet; }

    @Override
    public void act(float delta){
        super.act(delta);
        planet.getWorldBody().act(delta);
        planet.getWorldBody().update();
        game.universe.update(delta);

        if(!game.player.isDriving())
            game.player.setRotation(0);
    }

    @Override
    public void draw(){
        // Draw every map tile
        Batch batch = getBatch();
        batch.setProjectionMatrix(getCamera().combined);
        batch.setTransformMatrix(new Matrix4());
        
        // Save rendering state
        Matrix4 proj = batch.getProjectionMatrix().cpy();
        Matrix4 invProj = proj.cpy().inv();

        // Skybox rendering
        OrthographicCamera cam = (OrthographicCamera)getCamera();
        
        batch.begin();
        batch.setProjectionMatrix(new Matrix4());
        batch.setTransformMatrix(new Matrix4());
        game.spaceScene.getContent().getStarfield().setOffset(cam.position.x / 10000000, cam.position.y / 10000000);
        game.spaceScene.getContent().getStarfield().draw(batch, -1, -1, 2, 2);

        batch.setShader(cartesianAtmosShader);
        batch.setProjectionMatrix(new Matrix4().setToOrtho2D(0, 0, 1280, 720));
        batch.setTransformMatrix(new Matrix4());
        cartesianAtmosShader.setUniformMatrix("u_invCamTrans", invProj);
        cartesianAtmosShader.setUniformf("u_starDirection", planet.getStarDirection());
        cartesianAtmosShader.setUniformf("u_planetRadius", planet.getTerrestrialHeight() * Constants.CHUNK_SIZE * Tile.TILE_SIZE);
        cartesianAtmosShader.setUniformf("u_planetCircumference", planet.getTerrestrialWidth() * Constants.CHUNK_SIZE * Tile.TILE_SIZE);
        cartesianAtmosShader.setUniformf("u_atmosRadius", planet.getAtmosphereRadius());
        cartesianAtmosShader.setUniformf("u_atmosColor", planet.getAtmosphereColor());
        batch.draw(texture, 0, 0, 1280, 720);
        batch.setShader(null);

        // World rendering
        WorldBody worldBody = planet.getWorldBody();

        batch.setProjectionMatrix(proj);
        batch.setTransformMatrix(new Matrix4().translate(planet.getTerrestrialWidth() * Constants.CHUNK_SIZE * Tile.TILE_SIZE * -1.00f, 0, 0));
        worldBody.draw(batch, batch.getColor().a);
        batch.setTransformMatrix(new Matrix4().translate(planet.getTerrestrialWidth() * Constants.CHUNK_SIZE * Tile.TILE_SIZE * 1.00f, 0, 0));
        worldBody.draw(batch, batch.getColor().a);
        batch.setTransformMatrix(new Matrix4().translate(0, 0, 0));
        worldBody.draw(batch, batch.getColor().a);

        for(BaseEntity e : planet.getPlanetEntities()){
            e.render(batch);
        }

        batch.end();
        super.draw();

        // Debug rendering
        if(this.isDebugAll()){
            game.debug.render(planet.getInternalPhysWorld().getBox2DWorld(), game.activeCamera.combined.cpy().scl(Constants.PLANET_PPM));
        }
    }

    @Override
    public void dispose(){
        texture.dispose();
        super.dispose();
    }

}

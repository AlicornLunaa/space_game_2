package com.alicornlunaa.spacegame.scenes.planet_scene;

import com.alicornlunaa.selene_engine.components.ShaderComponent;
import com.alicornlunaa.spacegame.App;
import com.alicornlunaa.spacegame.components.PlanetComponent;
import com.alicornlunaa.spacegame.objects.blocks.Tile;
import com.alicornlunaa.spacegame.objects.planet.Planet;
import com.alicornlunaa.spacegame.objects.world.ChunkManager;
import com.alicornlunaa.spacegame.phys.PlanetaryPhysWorld;
import com.alicornlunaa.spacegame.util.Constants;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.viewport.FillViewport;

public class PlanetPanel extends Stage {
    // Variables
    private final App game;
    private Planet planet;
    private PlanetComponent planetComponent;
    private Texture texture;
    private ShaderComponent cartesianAtmosShader;

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
        planetComponent = planet.getComponent(PlanetComponent.class);

        cartesianAtmosShader = planetComponent.getCartesianShaderComponent();
        generateTexture();
        getViewport().setCamera(game.camera);
        addActor(planetComponent.chunkManager);

        // Controls
        this.addListener(new InputListener(){
            @Override
            public boolean keyDown(InputEvent event, int keycode){
                if(keycode == Keys.F5){
                    App.instance.manager.reload();
                }

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

        if(!game.gameScene.player.isDriving() && game.gameScene.player.bodyComponent.world instanceof PlanetaryPhysWorld)
            game.gameScene.player.transform.rotation = 0;
    }

    @Override
    public void draw(){
        // Draw every map tile
        Batch batch = getBatch();
        batch.setProjectionMatrix(game.camera.combined);
        batch.setTransformMatrix(new Matrix4());
        
        // Save rendering state
        Matrix4 proj = batch.getProjectionMatrix().cpy();
        Matrix4 invProj = proj.cpy().inv();

        // Skybox rendering
        Vector2 globalPos = game.gameScene.player.transform.position;
        
        batch.begin();
        batch.setProjectionMatrix(new Matrix4());
        batch.setTransformMatrix(new Matrix4());
        game.gameScene.spacePanel.getStarfield().setOffset(globalPos.x / 10000000, globalPos.y / 10000000);
        game.gameScene.spacePanel.getStarfield().draw(batch, -1, -1, 2, 2);

        batch.setShader(cartesianAtmosShader.program);
        batch.setProjectionMatrix(new Matrix4().setToOrtho2D(0, 0, 1280, 720));
        batch.setTransformMatrix(new Matrix4());
        cartesianAtmosShader.program.setUniformMatrix("u_invCamTrans", invProj);
        cartesianAtmosShader.program.setUniformf("u_starDirection", planetComponent.starDirection);
        cartesianAtmosShader.program.setUniformf("u_planetRadius", planetComponent.chunkHeight * Constants.CHUNK_SIZE * Tile.TILE_SIZE);
        cartesianAtmosShader.program.setUniformf("u_planetCircumference", planetComponent.chunkWidth * Constants.CHUNK_SIZE * Tile.TILE_SIZE);
        cartesianAtmosShader.program.setUniformf("u_atmosRadius", planetComponent.atmosphereRadius);
        cartesianAtmosShader.program.setUniformf("u_atmosColor", planetComponent.getAtmosphereColor());
        batch.draw(texture, 0, 0, 1280, 720);
        batch.setShader(null);
        
        App.instance.gameScene.registry.render();

        // World rendering
        ChunkManager worldBody = planetComponent.chunkManager;
        batch.setProjectionMatrix(game.camera.combined);
        batch.setTransformMatrix(new Matrix4().translate(planetComponent.chunkWidth * Constants.CHUNK_SIZE * Tile.TILE_SIZE * -1.00f, 0, 0));
        worldBody.draw(batch, batch.getColor().a);
        batch.setTransformMatrix(new Matrix4().translate(planetComponent.chunkWidth * Constants.CHUNK_SIZE * Tile.TILE_SIZE * 1.00f, 0, 0));
        worldBody.draw(batch, batch.getColor().a);
        batch.setTransformMatrix(new Matrix4());

        batch.end();
        super.draw();

        // Debug rendering
        if(Constants.DEBUG){
            game.debug.render(planetComponent.physWorld.getBox2DWorld(), game.camera.combined);
        }
    }

    @Override
    public void dispose(){
        texture.dispose();
        super.dispose();
    }
}

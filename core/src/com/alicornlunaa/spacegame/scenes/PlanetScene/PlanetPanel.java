package com.alicornlunaa.spacegame.scenes.PlanetScene;

import com.alicornlunaa.spacegame.App;
import com.alicornlunaa.spacegame.objects.Entity;
import com.alicornlunaa.spacegame.objects.Blocks.Tile;
import com.alicornlunaa.spacegame.objects.Planet2.Planet;
import com.alicornlunaa.spacegame.objects.Planet2.WorldBody;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.viewport.FillViewport;

public class PlanetPanel extends Stage {
    
    // Variables
    private final App game;
    private Planet planet;
    private OrthographicCamera cam;

    // Constructor
    public PlanetPanel(final App game, final Planet planet){
        super(new FillViewport(1280, 720));
        this.game = game;
        this.planet = planet;
        this.cam = (OrthographicCamera)getCamera();

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

        // Physics updates
        planet.updateWorld(delta);

        // Parent camera to player
        game.player.updateCamera(cam);
    }

    @Override
    public void draw(){
        super.draw();

        // Draw every map tile
        Batch batch = getBatch();
        batch.setProjectionMatrix(getCamera().combined);
        batch.setTransformMatrix(new Matrix4());

        WorldBody worldBody = planet.getWorldBody();

        batch.begin();
        batch.setTransformMatrix(new Matrix4().translate(planet.getTerrestrialWidth() * Tile.TILE_SIZE * -1.01f, 0, 0));
        // worldBody.draw(batch, batch.getColor().a);
        batch.setTransformMatrix(new Matrix4().translate(planet.getTerrestrialWidth() * Tile.TILE_SIZE * 1.01f, 0, 0));
        // worldBody.draw(batch, batch.getColor().a);
        batch.setTransformMatrix(new Matrix4().translate(0, 0, 0));
        worldBody.draw(batch, batch.getColor().a);

        for(Entity e : planet.getPlanetEntities()){
            e.draw(batch, 1.f);
        }

        batch.end();

        // Debug rendering
        if(this.isDebugAll()){
            // Draw chunk borders
            game.shapeRenderer.begin(ShapeType.Line);
            game.shapeRenderer.setProjectionMatrix(batch.getProjectionMatrix());
            game.shapeRenderer.setTransformMatrix(batch.getTransformMatrix());
            // for(Chunk chunk : planet.getMap().values()){
            //     if(!isChunkVisible(chunk)) continue;

            //     Vector2 pos = chunk.getChunkPos();
            //     float size = Chunk.CHUNK_SIZE * Tile.TILE_SIZE;

            //     game.shapeRenderer.setColor(chunk.isActive() ? Color.GREEN : Color.GRAY);
            //     game.shapeRenderer.rect(pos.x * size, pos.y * size, size, size);
            // }
            game.shapeRenderer.end();

            // game.debug.render(planet.getPhysWorld(), batch.getProjectionMatrix().cpy().scl(Constants.PLANET_PPM));
        }
    }

    @Override
    public void dispose(){
        super.dispose();
    }

}

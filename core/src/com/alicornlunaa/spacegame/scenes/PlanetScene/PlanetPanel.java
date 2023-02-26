package com.alicornlunaa.spacegame.scenes.PlanetScene;

import com.alicornlunaa.spacegame.App;
import com.alicornlunaa.spacegame.objects.Planet_old.Chunk;
import com.alicornlunaa.spacegame.objects.Planet_old.Planet;
import com.alicornlunaa.spacegame.objects.Planet_old.Tile;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.viewport.FillViewport;

public class PlanetPanel extends Stage {
    
    // Variables
    private final App game;

    public Planet planet;
    private float worldWidthPixels;

    // Constructor
    public PlanetPanel(final App game, final Planet planet){
        super(new FillViewport(1280, 720));
        this.game = game;

        this.planet = planet;
        worldWidthPixels = planet.getGenerator().getWidth() * Chunk.CHUNK_SIZE * Tile.TILE_SIZE;

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

    // Private functions
    private boolean isChunkVisible(Chunk chunk){
        // Used to cull the chunks not on screen
        OrthographicCamera cam = (OrthographicCamera)getCamera();
        Vector2 chunkPosition = chunk.getChunkPos();
        float chunkWorldSize = (Chunk.CHUNK_SIZE * Tile.TILE_SIZE);

        int chunkX = (int)Math.ceil(cam.position.x / chunkWorldSize) - 1;
        int chunkY = (int)Math.ceil(cam.position.y / chunkWorldSize) - 1;
        int chunksVisible = ((int)Math.ceil((cam.viewportWidth * cam.zoom) / chunkWorldSize) + 1) / 2;

        boolean xMatch = chunkPosition.x > chunkX - chunksVisible - 1 && chunkPosition.x < chunkX + chunksVisible + 1;
        boolean yMatch = chunkPosition.y > chunkY - chunksVisible - 1 && chunkPosition.y < chunkY + chunksVisible + 1;

        return xMatch && yMatch;
    }

    private void setActiveChunks(){
        // Get player position, convert it to chunk coordinates
        OrthographicCamera cam = (OrthographicCamera)getCamera();
        Vector2 playerPos = game.player.getPosition().cpy();
        float chunkWorldSize = (Chunk.CHUNK_SIZE * Tile.TILE_SIZE);

        int chunkX = (int)Math.ceil(playerPos.x / chunkWorldSize) - 1;
        int chunkY = (int)Math.ceil(playerPos.y / chunkWorldSize) - 1;
        int chunksVisible = (int)Math.ceil((cam.viewportWidth * cam.zoom) / chunkWorldSize) + 1;
        int renderRange = Math.max(chunksVisible, Chunk.RENDER_DISTANCE);

        for(int x = -renderRange; x <= renderRange; x++){
            for(int y = -renderRange; y <= renderRange; y++){
                int chunkXWrapped = Math.floorMod((chunkX + x), planet.getGenerator().getWidth());
                int chunkYWrapped = Math.floorMod((chunkY + y), planet.getGenerator().getHeight());
                Chunk chunk = planet.getChunk(chunkXWrapped, chunkYWrapped);

                if(chunk == null){
                    // Make a set chunk here
                    chunk = planet.createChunk(chunkXWrapped, chunkYWrapped);
                }
                
                // chunk.setVisible(isChunkVisible(chunk));
                chunk.setVisible(true);
                chunk.setActive(Math.abs(x) <= Chunk.ACTIVE_DISTANCE && Math.abs(y) <= Chunk.ACTIVE_DISTANCE);
            }
        }
    }

    // Functions
    public Planet getPlanet(){ return planet; }

    @Override
    public void act(float delta){
        super.act(delta);

        // Physics updates
        setActiveChunks();
        planet.updateWorld(delta);

        // Parent camera to player
        game.player.updateCamera((OrthographicCamera)getCamera());
    }

    @Override
    public void draw(){
        super.draw();

        // Draw every map tile
        Batch batch = getBatch();
        batch.setProjectionMatrix(getCamera().combined);
        batch.setTransformMatrix(new Matrix4());

        batch.begin();
        batch.setTransformMatrix(new Matrix4().translate(-worldWidthPixels, 0, 0));
        planet.drawWorld(batch, batch.getColor().a);
        batch.setTransformMatrix(new Matrix4().translate(worldWidthPixels, 0, 0));
        planet.drawWorld(batch, batch.getColor().a);
        batch.setTransformMatrix(new Matrix4().translate(0, 0, 0));
        planet.drawWorld(batch, batch.getColor().a);
        batch.end();

        // Debug rendering
        if(this.isDebugAll()){
            // Draw chunk borders
            game.shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
            game.shapeRenderer.setProjectionMatrix(batch.getProjectionMatrix());
            game.shapeRenderer.setTransformMatrix(batch.getTransformMatrix());
            for(Chunk chunk : planet.getMap().values()){
                if(!isChunkVisible(chunk)) continue;

                Vector2 pos = chunk.getChunkPos();
                float size = Chunk.CHUNK_SIZE * Tile.TILE_SIZE;

                game.shapeRenderer.setColor(chunk.isActive() ? Color.GREEN : Color.GRAY);
                game.shapeRenderer.rect(pos.x * size, pos.y * size, size, size);
            }
            game.shapeRenderer.end();
        }
    }

    @Override
    public void dispose(){
        super.dispose();
    }

}

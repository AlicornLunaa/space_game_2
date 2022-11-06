package com.alicornlunaa.spacegame.panels;

import com.alicornlunaa.spacegame.App;
import com.alicornlunaa.spacegame.objects.Player;
import com.alicornlunaa.spacegame.objects.Planet.Chunk;
import com.alicornlunaa.spacegame.objects.Planet.Planet;
import com.alicornlunaa.spacegame.objects.Planet.Tile;
import com.alicornlunaa.spacegame.util.Constants;
import com.alicornlunaa.spacegame.util.ControlSchema;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.viewport.FillViewport;

public class PlanetPanel extends Stage {
    
    // Variables
    private final App game;

    public Planet planet;
    public Player player;
    private float worldWidthPixels;

    // Constructor
    public PlanetPanel(final App game, final Planet planet){
        super(new FillViewport(1280, 720));
        this.game = game;

        player = new Player(game, planet.getPlanetWorld(), 30, (30 + planet.getRadius()) / Constants.PLANET_PPM, Constants.PLANET_PPM);
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
                cam.zoom = Math.min(Math.max(cam.zoom + (amountY / 50), 0.05f), 1.5f);

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
        Vector2 playerPos = new Vector2(player.getPosition());
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
                
                chunk.setVisible(isChunkVisible(chunk));
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
        player.act(delta);

        // Constrain player to world bounds
        if(player.getX() > worldWidthPixels){
            player.setX(player.getX() - worldWidthPixels);
        } else if(player.getX() < 0){
            player.setX(player.getX() + worldWidthPixels);
        }

        // Parent camera to player
        OrthographicCamera cam = (OrthographicCamera)getCamera();
        cam.position.set(planet.getEntities().size() > 0 ? planet.getEntities().get(0).getPosition() : player.getPosition(), 0);
        cam.update();

        // Controls for player
        if(Gdx.input.isKeyPressed(ControlSchema.PLAYER_UP)){
            player.state.vertical = 1;
        } else if(Gdx.input.isKeyPressed(ControlSchema.PLAYER_DOWN)){
            player.state.vertical = -1;
        } else {
            player.state.vertical = 0;
        }
        
        if(Gdx.input.isKeyPressed(ControlSchema.PLAYER_RIGHT)){
            player.state.horizontal = 1;
        } else if(Gdx.input.isKeyPressed(ControlSchema.PLAYER_LEFT)){
            player.state.horizontal = -1;
        } else {
            player.state.horizontal = 0;
        }
    }

    @Override
    public void draw(){
        super.draw();

        // Draw every map tile
        Batch batch = getBatch();
        batch.begin();

        batch.setProjectionMatrix(getCamera().combined);
        player.draw(batch, batch.getColor().a);
        planet.drawWorld(batch, batch.getColor().a);

        // Wrapping effect
        batch.setProjectionMatrix(getCamera().combined.cpy().translate(worldWidthPixels, 0, 0).scl(1, 1, 1));
        planet.drawWorld(batch, batch.getColor().a);
        batch.setProjectionMatrix(getCamera().combined.cpy().translate(-worldWidthPixels, 0, 0).scl(1, 1, 1));
        planet.drawWorld(batch, batch.getColor().a);
        batch.setProjectionMatrix(getCamera().combined);
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

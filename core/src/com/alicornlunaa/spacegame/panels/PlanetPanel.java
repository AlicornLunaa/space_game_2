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
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

public class PlanetPanel extends Stage {
    
    // Variables
    private final App game;

    private World world;
    private float physAccumulator;

    private Planet planet;
    private Player player;
    
    private Box2DDebugRenderer debug = new Box2DDebugRenderer();

    // Constructor
    public PlanetPanel(final App game){
        super(new ScreenViewport());
        this.game = game;

        world = new World(new Vector2(0, -600), true);

        planet = new Planet(game, world, 1000.0f);
        player = new Player(game, world, 0, 150);

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
        Vector2 playerPos = new Vector2(player.getBody().getWorldCenter());
        float chunkWorldSize = (Chunk.CHUNK_SIZE * Tile.TILE_SIZE);

        int chunkX = (int)Math.ceil(playerPos.x / chunkWorldSize) - 1;
        int chunkY = (int)Math.ceil(playerPos.y / chunkWorldSize) - 1;
        int chunksVisible = (int)Math.ceil((cam.viewportWidth * cam.zoom) / chunkWorldSize) + 1;
        int renderRange = Math.max(chunksVisible, Chunk.RENDER_DISTANCE);

        for(int x = -renderRange; x <= renderRange; x++){
            for(int y = -renderRange; y <= renderRange; y++){
                Chunk chunk = planet.getChunk(chunkX + x, chunkY + y);

                if(chunk == null){
                    // Make a set chunk here
                    chunk = planet.createChunk(chunkX + x, chunkY + y);
                }
                
                chunk.setActive(Math.abs(x) <= Chunk.ACTIVE_DISTANCE && Math.abs(y) <= Chunk.ACTIVE_DISTANCE);
            }
        }
    }

    // Functions
    public World getWorld(){ return world; }

    @Override
    public void act(float delta){
        super.act(delta);

        // Physics updates
        physAccumulator += Math.min(delta, 0.25f);
        while(physAccumulator >= Constants.TIME_STEP){
            world.step(Constants.TIME_STEP, Constants.VELOCITY_ITERATIONS, Constants.POSITION_ITERATIONS);
            physAccumulator -= Constants.TIME_STEP;
        }

        setActiveChunks();
        for(Chunk chunk : planet.getMap().values()){
            chunk.update(delta);
        }

        player.act(delta);
        float coreAngle = new Vector2(0, 0).sub(player.getBody().getPosition()).nor().angleRad() + (float)Math.toRadians(90);
        player.getBody().setTransform(player.getBody().getPosition(), coreAngle);
        Vector2 playerUp = new Vector2(0, 1).rotateRad(player.getBody().getAngle());

        // Parent camera to player
        OrthographicCamera cam = (OrthographicCamera)getCamera();
        cam.position.set(player.getBody().getWorldCenter(), 0);
        cam.up.set(playerUp, 0);
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

        for(Chunk chunk : planet.getMap().values()){
            if(!isChunkVisible(chunk)) continue;
            chunk.draw(batch);
        }

        player.draw(batch, batch.getColor().a);

        batch.end();

        // Debug rendering
        if(this.isDebugAll()){
            debug.render(world, this.getCamera().combined);

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

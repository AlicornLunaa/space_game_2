package com.alicornlunaa.spacegame.panels;

import com.alicornlunaa.spacegame.App;
import com.alicornlunaa.spacegame.objects.Player;
import com.alicornlunaa.spacegame.objects.Planet.Chunk;
import com.alicornlunaa.spacegame.objects.Planet.Planet;
import com.alicornlunaa.spacegame.util.Constants;
import com.alicornlunaa.spacegame.util.ControlSchema;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

public class PlanetPanel extends Stage {
    
    // Variables
    private final Planet planetRef;

    private World world;
    private float physAccumulator;

    private Player player;
    
    private Box2DDebugRenderer debug = new Box2DDebugRenderer();

    // Constructor
    public PlanetPanel(final App game, final Planet planetRef){
        super(new ScreenViewport());
        this.planetRef = planetRef;

        world = new World(new Vector2(), true);

        player = new Player(game, world, getWidth() / 2, getHeight() / 2);
        this.addActor(player);
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

        for(Chunk chunk : planetRef.getMap().values()){
            chunk.update(delta);
        }

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
        batch.setTransformMatrix(new Matrix4().translate(getWidth() / 2, getHeight() / 2, 0));

        for(Chunk chunk : planetRef.getMap().values()){
            chunk.draw(batch);
        }

        batch.end();

        // Debug rendering
        if(this.isDebugAll()){
            debug.render(world, this.getCamera().combined);
        }
    }

    @Override
    public void dispose(){
        super.dispose();
    }

}

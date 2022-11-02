package com.alicornlunaa.spacegame.panels;

import com.alicornlunaa.spacegame.App;
import com.alicornlunaa.spacegame.objects.Ground;
import com.alicornlunaa.spacegame.objects.Ship;
import com.alicornlunaa.spacegame.util.Constants;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

public class GamePanel extends Stage {

    // Variables
    final App game;

    private World world;
    private float physAccumulator = 0.0f;
    
    private Box2DDebugRenderer debug = new Box2DDebugRenderer();

	public boolean rcs = false;
	public boolean sas = false;

    // Constructor
    public GamePanel(final App game){
        super(new ScreenViewport());
        this.game = game;

        world = new World(new Vector2(), true);

        Ship ship = new Ship(game.manager, game.partManager, world, 640/2, 250, 0);
        ship.load("./saves/ships/null.ship");
		this.addActor(ship);

		this.addActor(new Ground(game, world, getWidth() / 2, 30, getWidth() - 30, 25));
    }

    // Functions
    public World getWorld(){ return world; }

    @Override
    public void act(float delta){
        super.act(delta);

        // Physics updates
        physAccumulator += Math.min(delta, 0.25f);;
        while(physAccumulator >= Constants.TIME_STEP){
            world.step(Constants.TIME_STEP, Constants.VELOCITY_ITERATIONS, Constants.POSITION_ITERATIONS);
            physAccumulator -= Constants.TIME_STEP;
        }
    }

    @Override
    public void draw(){
        super.draw();

        if(this.isDebugAll()){
            debug.render(world, this.getCamera().combined);
        }
    }
    
    @Override
    public void dispose(){
        super.dispose();
    }

}

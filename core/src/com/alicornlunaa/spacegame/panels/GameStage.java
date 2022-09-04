package com.alicornlunaa.spacegame.panels;

import java.util.ArrayList;

import com.alicornlunaa.spacegame.objects.Ground;
import com.alicornlunaa.spacegame.objects.Ship;
import com.alicornlunaa.spacegame.util.Constants;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

/*
 * GameStage is the view in space
 */
public class GameStage extends Stage {
    // Variables
    private World world;
    private Box2DDebugRenderer debugRenderer;
    private float accumulator;
    private InputProcessor oldProcessor;

    private boolean debug;
    private boolean paused;
	public boolean rcs;
	public boolean sas;
    
    private Skin skin;
	private Table ui;
	private TextButton rcsButton;
	private TextButton sasButton;
    private TextButton editorButton;

    // Constructor
    public GameStage(final ArrayList<Stage> stages, final InputMultiplexer inputs, Skin skin){
        super(new ScreenViewport());

        world = new World(new Vector2(), true);
        debugRenderer = new Box2DDebugRenderer();
		accumulator = 0.f;
        oldProcessor = Gdx.input.getInputProcessor();
        debug = false;
        paused = false;
		rcs = false;
		sas = false;

        Gdx.input.setInputProcessor(this);

        this.skin = skin;
        ui = new Table();
        ui.setFillParent(true);
		ui.setDebug(true);
		this.addActor(ui);
		
		rcsButton = new TextButton("RCS", skin);
		rcsButton.setPosition(640 - 128, 480 - 32);
		rcsButton.setSize(64, 32);
		rcsButton.setColor(Color.RED);
		this.addActor(rcsButton);
		sasButton = new TextButton("SAS", skin);
		sasButton.setPosition(640 - 64, 480 - 32);
		sasButton.setSize(64, 32);
		sasButton.setColor(Color.RED);
		this.addActor(sasButton);
		editorButton = new TextButton("Editor", skin);
		editorButton.setPosition(640 - 192, 480 - 32);
		editorButton.setSize(64, 32);
		this.addActor(editorButton);

        editorButton.addListener(new ChangeListener(){
            @Override
            public void changed(ChangeEvent event, Actor actor){
                stages.add(new ShipEditor());
            }
        });

		this.addActor(new Ship(world, 640/2, 250, 0));
		this.addActor(new Ground(world, 640/2, 20, 500, 15));
    }

    // Functions
    public boolean getPaused(){ return paused; }
    public World getWorld(){ return world; }

    public void setPaused(boolean paused){ this.paused = paused; }

    @Override
    public void setDebugAll(boolean debugAll){
        super.setDebugAll(debugAll);
        debug = debugAll;
    }

    @Override
    public void act(float delta){
        super.act(delta);

        // Physics updates
        if(!paused){
            accumulator += Math.min(delta, 0.25f);;
			while(accumulator >= Constants.TIME_STEP){
				world.step(Constants.TIME_STEP, Constants.VELOCITY_ITERATIONS, Constants.POSITION_ITERATIONS);
				accumulator -= Constants.TIME_STEP;
			}
        }

        rcsButton.setColor(rcs ? Color.GREEN : Color.RED);
        sasButton.setColor(sas ? Color.GREEN : Color.RED);
    }

    @Override
    public void draw(){
        super.draw();

        if(debug){
            debugRenderer.render(world, this.getCamera().combined);
        }
    }
    
    @Override
    public void dispose(){
        super.dispose();
        Gdx.input.setInputProcessor(oldProcessor);
    }
}

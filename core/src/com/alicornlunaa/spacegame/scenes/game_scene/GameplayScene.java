package com.alicornlunaa.spacegame.scenes.game_scene;

import com.alicornlunaa.selene_engine.scenes.BaseScene;
import com.alicornlunaa.spacegame.App;
import com.alicornlunaa.spacegame.objects.Player;
import com.alicornlunaa.spacegame.objects.planet.Planet;
import com.alicornlunaa.spacegame.objects.simulation.Star;
import com.alicornlunaa.spacegame.objects.simulation.Universe;
import com.alicornlunaa.spacegame.objects.simulation.orbits.OrbitUtils;
import com.alicornlunaa.spacegame.util.Constants;
import com.badlogic.gdx.graphics.OrthographicCamera;

/**
 * Handles all normal gameplay in the application,
 * this includes the space scene, planet scene,
 * inside ship view, and map view.
 */
public class GameplayScene extends BaseScene {

    // Variables
    public SpacePanel spacePanel;
    public SpaceInterface spaceInterface;
    
	public Universe universe;
	public OrthographicCamera activeCamera;
	public Player player;
    
    // Private functions
	private void initializeUniverse(){
		universe = new Universe(game);

        universe.addCelestial(new Star(game, 1000000, 0, 695700 * Constants.CONVERSION_FACTOR));
        universe.addCelestial(new Planet(game, 1000000 - 5632704 * Constants.CONVERSION_FACTOR, 0, 24390 * Constants.CONVERSION_FACTOR, 29400 * Constants.CONVERSION_FACTOR, 1)); // Mercury
        OrbitUtils.createOrbit(universe, universe.getCelestial(1));
        universe.addCelestial(new Planet(game, 1000000 - 10782604 * Constants.CONVERSION_FACTOR, 0, 60518 * Constants.CONVERSION_FACTOR, 62700 * Constants.CONVERSION_FACTOR, 1)); // Venus
        OrbitUtils.createOrbit(universe, universe.getCelestial(2));
        universe.addCelestial(new Planet(game, 1000000 - 14966899 * Constants.CONVERSION_FACTOR, 0, 63780 * Constants.CONVERSION_FACTOR, 68000 * Constants.CONVERSION_FACTOR, 1)); // Earth
        OrbitUtils.createOrbit(universe, universe.getCelestial(3));
        universe.addCelestial(new Planet(game, 1000000 - 22852684 * Constants.CONVERSION_FACTOR, 0, 33890 * Constants.CONVERSION_FACTOR, 36890 * Constants.CONVERSION_FACTOR, 1)); // Mars
        OrbitUtils.createOrbit(universe, universe.getCelestial(4));
        universe.addCelestial(new Planet(game, 1000000 - 14966899 * Constants.CONVERSION_FACTOR + 405400 * Constants.CONVERSION_FACTOR, 0, 17374 * Constants.CONVERSION_FACTOR, 0, 0)); // Moon
        OrbitUtils.createOrbit(universe, universe.getCelestial(5));

        // universe.addCelestial(new Star(this, 1400, 0, 500));
        // universe.addCelestial(new Planet(this, 6400, 0, 500, 800, 1.0f));
        // OrbitUtils.createOrbit(universe, universe.getCelestial(1));

		player = new Player(game, -50, 0);
		universe.addEntity(player);
		OrbitUtils.createOrbit(universe, player);
	}

    private void initializeSpaceScene(){
        spacePanel = new SpacePanel(game);
        spacePanel.setDebugAll(Constants.DEBUG);
        spaceInterface = new SpaceInterface(game);
        spaceInterface.setDebugAll(Constants.DEBUG);

        inputs.addProcessor(spaceInterface);
        inputs.addProcessor(spacePanel);

        spacePanel.ship.drive(player);
        OrbitUtils.createOrbit(universe, spacePanel.ship);
    }

    // Constructors
    public GameplayScene(App game) { super(game); }
    
    // Functions
    // public U getUI(){ return ui; }
    public SpacePanel getContent(){ return spacePanel; }

    public void init(){
        initializeUniverse();
        initializeSpaceScene();
    }

    @Override
    public void render(float delta) {
        super.render(delta);

        spaceInterface.act(delta);
        spacePanel.act(delta);

        spacePanel.draw();
        spaceInterface.draw();
    }

    @Override
    public void resize(int width, int height) {
        spacePanel.getViewport().update(width, height, true);
        spaceInterface.getViewport().update(width, height, true);
    }

    @Override
    public void dispose(){
        spacePanel.dispose();
        spaceInterface.dispose();
    }

}

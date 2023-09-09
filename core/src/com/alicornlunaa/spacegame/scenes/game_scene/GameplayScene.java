package com.alicornlunaa.spacegame.scenes.game_scene;

import com.alicornlunaa.selene_engine.components.BodyComponent;
import com.alicornlunaa.selene_engine.ecs.Registry;
import com.alicornlunaa.selene_engine.scenes.BaseScene;
import com.alicornlunaa.selene_engine.systems.CameraSystem;
import com.alicornlunaa.selene_engine.systems.PhysicsSystem;
import com.alicornlunaa.selene_engine.systems.ScriptSystem;
import com.alicornlunaa.spacegame.App;
import com.alicornlunaa.spacegame.objects.Player;
import com.alicornlunaa.spacegame.objects.planet.Planet;
import com.alicornlunaa.spacegame.objects.ship.Ship;
import com.alicornlunaa.spacegame.objects.simulation.Star;
import com.alicornlunaa.spacegame.objects.simulation.Universe;
import com.alicornlunaa.spacegame.objects.simulation.orbits.OrbitUtils;
import com.alicornlunaa.spacegame.systems.OrbitSystem;
import com.alicornlunaa.spacegame.systems.SpaceRenderSystem;
import com.alicornlunaa.spacegame.util.Constants;
import com.badlogic.gdx.utils.ScreenUtils;

/**
 * Handles all normal gameplay in the application,
 * this includes the space scene, planet scene,
 * inside ship view, and map view.
 */
public class GameplayScene extends BaseScene {

    // Variables
    private GameplayState state = GameplayState.SPACE;

	public Registry registry;
	public PhysicsSystem simulation;
	public OrbitSystem orbitSystem;

    public SpacePanel spacePanel;
    public SpaceInterface spaceInterface;
    public ShipViewPanel shipViewPanel;
    public ShipViewInterface shipViewInteface;
    public MapPanel mapPanel;
    
	public Universe universe;
	public Player player;
    
    // Private functions
	private void initializeUniverse(){
		universe = new Universe(game);
        registry.registerSystem(new SpaceRenderSystem(game, universe));

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
    public GameplayScene(App game) {
        // Initialize base scene
        super(game);

        // Start new engine registry
        registry = new Registry();
        registry.registerSystem(new CameraSystem(game));
        simulation = registry.registerSystem(new PhysicsSystem());
        registry.registerSystem(new ScriptSystem());
        orbitSystem = registry.registerSystem(new OrbitSystem(game));
    }
    
    // Functions
    public void init(){
        initializeUniverse();
        initializeSpaceScene();
    }

    public void openMap(){
        mapPanel = new MapPanel(game, spacePanel);
        inputs.addProcessor(0, mapPanel);
        state = GameplayState.MAP;
    }

    public void closeMap(){
        inputs.removeProcessor(mapPanel);
        state = GameplayState.SPACE;
        mapPanel.dispose();
        mapPanel = null;
    }

    public void openShipView(Ship ship){
        ship.stopDriving();
        game.gameScene.player.bodyComponent.setWorld(ship.getInterior().getWorld());
        game.gameScene.player.transform.position.set(0, 0);
        game.gameScene.player.transform.rotation = 0;
        game.gameScene.player.getComponent(BodyComponent.class).body.setLinearVelocity(0, 0);

        state = GameplayState.SHIP;

        shipViewPanel = new ShipViewPanel(game, ship);
        shipViewInteface = new ShipViewInterface(game);

        inputs.clear();
        inputs.addProcessor(shipViewInteface);
        inputs.addProcessor(shipViewPanel);
    }

    public void closeShipView(){
        game.gameScene.player.bodyComponent.setWorld(shipViewPanel.ship.bodyComponent.world);
        shipViewPanel.ship.drive(game.gameScene.player);

        state = GameplayState.SPACE;
        shipViewPanel.dispose();
        shipViewPanel = null;
        shipViewInteface.dispose();
        shipViewInteface = null;

        inputs.clear();
        inputs.addProcessor(spaceInterface);
        inputs.addProcessor(spacePanel);
    }

    @Override
    public void render(float delta) {
        super.render(delta);

        switch(state){
            case SPACE:
                spaceInterface.act(delta);
                spacePanel.act(delta);
                spacePanel.draw();
                spaceInterface.draw();
                break;
                
            case MAP:
                spaceInterface.act(delta);
                mapPanel.act(delta);
                mapPanel.draw();
                spaceInterface.draw();
                break;

            case SHIP:
                ScreenUtils.clear(0.1f, 0.1f, 0.1f, 1.0f);
                shipViewInteface.act(delta);
                shipViewPanel.act(delta);
                shipViewPanel.draw();
                shipViewInteface.draw();
                break;

            case PLANET:
                break;
        }
    }

    @Override
    public void resize(int width, int height) {
        if(spacePanel != null) spacePanel.getViewport().update(width, height, true);
        if(spaceInterface != null) spaceInterface.getViewport().update(width, height, true);
        if(shipViewPanel != null) shipViewPanel.getViewport().update(width, height, true);
        if(shipViewInteface != null) shipViewInteface.getViewport().update(width, height, true);
        if(mapPanel != null) mapPanel.getViewport().update(width, height, true);
    }

    @Override
    public void dispose(){
        spacePanel.dispose();
        spaceInterface.dispose();
    }

}

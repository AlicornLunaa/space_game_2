package com.alicornlunaa.spacegame.scenes.game_scene;

import com.alicornlunaa.selene_engine.components.BodyComponent;
import com.alicornlunaa.selene_engine.components.CameraComponent;
import com.alicornlunaa.selene_engine.ecs.Registry;
import com.alicornlunaa.selene_engine.scenes.BaseScene;
import com.alicornlunaa.selene_engine.systems.CameraSystem;
import com.alicornlunaa.selene_engine.systems.PhysicsSystem;
import com.alicornlunaa.selene_engine.systems.ScriptSystem;
import com.alicornlunaa.spacegame.App;
import com.alicornlunaa.spacegame.components.PlanetComponent;
import com.alicornlunaa.spacegame.objects.Player;
import com.alicornlunaa.spacegame.objects.planet.Planet;
import com.alicornlunaa.spacegame.objects.ship.Ship;
import com.alicornlunaa.spacegame.objects.simulation.Celestial;
import com.alicornlunaa.spacegame.objects.simulation.Star;
import com.alicornlunaa.spacegame.objects.simulation.Universe;
import com.alicornlunaa.spacegame.objects.simulation.orbits.OrbitUtils;
import com.alicornlunaa.spacegame.scenes.planet_scene.PlanetPanel;
import com.alicornlunaa.spacegame.scenes.planet_scene.PlanetUIPanel;
import com.alicornlunaa.spacegame.scripts.GravityScript;
import com.alicornlunaa.spacegame.systems.SpaceRenderSystem;
import com.alicornlunaa.spacegame.systems.CelestialSystem;
import com.alicornlunaa.spacegame.systems.OrbitSystem;
import com.alicornlunaa.spacegame.systems.PlanetRenderSystem;
import com.alicornlunaa.spacegame.systems.SimulatedPathSystem;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Color;
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
    public SpaceRenderSystem spaceRenderSystem;
    public PlanetRenderSystem planetRenderSystem;
	public PhysicsSystem simulation;
	public OrbitSystem orbitSystem;

    public SpacePanel spacePanel;
    public SpaceInterface spaceInterface;
    public ShipViewPanel shipViewPanel;
    public ShipViewInterface shipViewInteface;
    public PlanetPanel planetViewPanel;
    public PlanetUIPanel planetViewInteface;
    public MapPanel mapPanel;
    
	public Universe universe;
	public Player player;
    public Ship ship;
    public Planet testPlanet;
    
    // Private functions
    private Celestial newCelestial(Celestial c){
        registry.addEntity(c);
        universe.addCelestial(c);
        return c;
    }

	private void initializeUniverse(){
		universe = new Universe(registry);
        simulation.addWorld(universe.getUniversalWorld());
        // registry.registerSystem(new CelestialSystem(universe));
        spaceRenderSystem = registry.registerSystem(new SpaceRenderSystem(universe));

        // Celestial star = newCelestial(new Star(simulation, universe.getUniversalWorld(), 800000, 1000000, 0));
        Celestial star = newCelestial(new Star(simulation, universe.getUniversalWorld(), 80000, 300000, 0));
        newCelestial(new Celestial(simulation, universe.getUniversalWorld(), 8000, -400000, 0));
        newCelestial(new Celestial(simulation, universe.getUniversalWorld(), star, 100000, 10000, 0.001f, 0.0f, 0.0f, 0.0f)); 
        newCelestial(new Celestial(simulation, universe.getUniversalWorld(), star, 10000, 1000, 0.001f, 0.0f, 0.0f, 0.0f)); 
        // newCelestial(new Celestial(simulation, universe.getUniversalWorld(), star, 11000, 17500, 0.001f, 0.0f, 0.0f, 0.0f));
        // newCelestial(new Celestial(simulation, universe.getUniversalWorld(), star, 18000, 21500, 0.001f, 0.0f, 0.0f, 0.0f));
        // newCelestial(new Celestial(simulation, universe.getUniversalWorld(), star, 18000, 31500, 0.001f, 0.0f, 0.0f, 0.0f));
        // newCelestial(new Celestial(simulation, universe.getUniversalWorld(), star, 18000, 41500, 0.001f, 0.0f, 0.0f, 0.0f));
        // newCelestial(new Celestial(simulation, universe.getUniversalWorld(), star, 18000, 51500, 0.001f, 0.0f, 0.0f, 0.0f));
        // newCelestial(new Celestial(simulation, universe.getUniversalWorld(), star, 18000, 61500, 0.001f, 0.0f, 0.0f, 0.0f));
        // newCelestial(new Celestial(simulation, universe.getUniversalWorld(), star, 480000, 71500, 0.001f, 0.0f, 0.0f, 0.0f));
        
        // testPlanet = new Planet(simulation, universe.getUniversalWorld(), -1000, 0, 500, 560, 1);
        // registry.addEntity(testPlanet);

		player = new Player(game, universe.getUniversalWorld(), -50, 0);
        player.addComponent(new GravityScript(universe, player));
        player.getComponent(CameraComponent.class).active = true;
        game.camera = player.getComponent(CameraComponent.class).camera;
		registry.addEntity(player);
        OrbitUtils.createOrbit(universe, player);

        ship = new Ship(game, game.gameScene.universe.getUniversalWorld(), 100, 0, 0);
        ship.addComponent(new GravityScript(universe, ship));
        ship.load("./saves/ships/test.ship");
        registry.addEntity(ship);
        OrbitUtils.createOrbit(universe, ship);
        // ship.drive(player);
	}

    private void initializeSpaceScene(){
        spacePanel = new SpacePanel(game);
        spaceInterface = new SpaceInterface(game);
        spacePanel.setDebugAll(true);
        spaceInterface.setDebugAll(false);

        inputs.addProcessor(spaceInterface);
        inputs.addProcessor(spacePanel);
    }

    // Constructors
    public GameplayScene(App game) {
        // Initialize base scene
        super(game);
        backgroundColor = new Color(0.04f, 0.04f, 0.04f, 1.0f);

        // Start new engine registry
        registry = new Registry();
        registry.registerSystem(new CameraSystem(game));
        simulation = registry.registerSystem(new PhysicsSystem());
        planetRenderSystem = registry.registerSystem(new PlanetRenderSystem());
        orbitSystem = registry.registerSystem(new OrbitSystem(game));
        registry.registerSystem(new ScriptSystem());
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
        player.getComponent(CameraComponent.class).active = false;
    }

    public void closeMap(){
        inputs.removeProcessor(mapPanel);
        state = GameplayState.SPACE;
        mapPanel.dispose();
        mapPanel = null;
        player.getComponent(CameraComponent.class).active = true;
        spacePanel.addActor(universe);
    }

    public void openShipView(Ship ship){
        ship.stopDriving();
        player.bodyComponent.setWorld(ship.getInterior().getWorld());
        player.transform.position.set(0, 0);
        player.transform.rotation = 0;
        player.getComponent(BodyComponent.class).body.setLinearVelocity(0, 0);

        state = GameplayState.SHIP;

        shipViewPanel = new ShipViewPanel(game, ship);
        shipViewInteface = new ShipViewInterface(game);

        inputs.clear();
        inputs.addProcessor(shipViewInteface);
        inputs.addProcessor(shipViewPanel);
    }

    public void closeShipView(){
        player.bodyComponent.setWorld(shipViewPanel.ship.getBody().world);
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

    public void openPlanetView(Planet planet){
        player.bodyComponent.setWorld(planet.getComponent(PlanetComponent.class).physWorld);
        player.transform.position.set(0, 800);
        player.transform.rotation = 0;
        player.getComponent(BodyComponent.class).body.setLinearVelocity(0, 0);

        state = GameplayState.PLANET;

        planet.getComponent(PlanetComponent.class).addEntityWorld(player);
        planetViewPanel = new PlanetPanel(game, planet);
        planetViewInteface = new PlanetUIPanel(game);
        planetViewPanel.getViewport().setCamera(player.getComponent(CameraComponent.class).camera);

        spaceRenderSystem.setActive(false);
        planetRenderSystem.setPlanet(planet);

        inputs.clear();
        inputs.addProcessor(planetViewInteface);
        inputs.addProcessor(planetViewPanel);
    }

    public void closePlanetView(){
        player.bodyComponent.setWorld(universe.getUniversalWorld());

        state = GameplayState.SPACE;
        planetViewPanel.dispose();
        planetViewPanel = null;
        planetViewInteface.dispose();
        planetViewInteface = null;

        spaceRenderSystem.setActive(true);
        planetRenderSystem.setPlanet(null);

        inputs.clear();
        inputs.addProcessor(spaceInterface);
        inputs.addProcessor(spacePanel);
    }

    @Override
    public void render(float delta) {
        super.render(delta);
        universe.update(delta);

        if(Gdx.input.isKeyJustPressed(Keys.F4)){
            if(planetViewPanel == null){
                openPlanetView(testPlanet);
            } else {
                closePlanetView();
            }
        }

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
                ScreenUtils.clear(0.1f, 0.1f, 0.1f, 1.0f);
                planetViewInteface.act(delta);
                planetViewPanel.act(delta);
                planetViewPanel.draw();
                spaceInterface.draw();
                planetViewInteface.draw();
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
        shipViewPanel.dispose();
        shipViewInteface.dispose();
        mapPanel.dispose();
    }
}

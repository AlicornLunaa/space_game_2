package com.alicornlunaa.spacegame.scenes.game_scene;

import com.alicornlunaa.selene_engine.components.ActorComponent;
import com.alicornlunaa.selene_engine.components.BodyComponent;
import com.alicornlunaa.selene_engine.components.CameraComponent;
import com.alicornlunaa.selene_engine.ecs.Registry;
import com.alicornlunaa.selene_engine.scenes.BaseScene;
import com.alicornlunaa.selene_engine.systems.CameraSystem;
import com.alicornlunaa.selene_engine.systems.PhysicsSystem;
import com.alicornlunaa.selene_engine.systems.ScriptSystem;
import com.alicornlunaa.selene_engine.systems.ShapeRenderSystem;
import com.alicornlunaa.spacegame.App;
import com.alicornlunaa.spacegame.components.GravityComponent;
import com.alicornlunaa.spacegame.components.PlanetComponent;
import com.alicornlunaa.spacegame.objects.ItemEntity;
import com.alicornlunaa.spacegame.objects.Player;
import com.alicornlunaa.spacegame.objects.blocks.BaseTile;
import com.alicornlunaa.spacegame.objects.ship.Ship;
import com.alicornlunaa.spacegame.objects.simulation.Celestial;
import com.alicornlunaa.spacegame.objects.simulation.Planet;
import com.alicornlunaa.spacegame.objects.simulation.Universe;
import com.alicornlunaa.spacegame.scenes.planet_scene.PlanetPanel;
import com.alicornlunaa.spacegame.scenes.planet_scene.PlanetUIPanel;
import com.alicornlunaa.spacegame.systems.SpaceRenderSystem;
import com.alicornlunaa.spacegame.systems.TrackingSystem;
import com.alicornlunaa.spacegame.systems.GravitySystem;
import com.alicornlunaa.spacegame.systems.PlanetRenderSystem;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Null;
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
    public TrackingSystem trackingSystem;

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

        // Celestial c1 = newCelestial(new Star(simulation, universe.getUniversalWorld(), 3000, 4000, 0));
        // Celestial c2 = newCelestial(new Planet(simulation, universe.getUniversalWorld(), 15000, 0.f, 300, 400, 1.f));
        // Celestial c3 = newCelestial(new Celestial(simulation, universe.getUniversalWorld(), 50, 15575, 0.f));
        // Celestial c4 = newCelestial(new Celestial(simulation, universe.getUniversalWorld(), 100, 21000, 0.f));
        // Celestial c5 = newCelestial(new Celestial(simulation, universe.getUniversalWorld(), 200, 24000, 0.f));
        // Celestial c6 = newCelestial(new Celestial(simulation, universe.getUniversalWorld(), 400, 30000, 0.f));
        testPlanet = new Planet(registry, simulation, universe.getUniversalWorld(), 800, 0, 300, 400, 1.f);
        newCelestial(testPlanet);
        
		player = new Player(universe.getUniversalWorld(), 500, 0);
        // player.addComponent(new TrackedEntityComponent(Color.CYAN)).predictFuture = true;
        App.instance.camera = player.getComponent(CameraComponent.class).camera;
		registry.addEntity(player);
        player.toggleNoclip();

        PlanetComponent pc = testPlanet.getComponent(PlanetComponent.class);
        BaseTile testDynamicTile = new BaseTile(pc.physWorld, "stone", 96, 26, 0);
        testDynamicTile.getComponent(ActorComponent.class).addListener(new ClickListener(){
            @Override
            public void enter(InputEvent event, float x, float y, int pointer, @Null Actor fromActor){
                if(!Gdx.input.isTouched(0)) return;
                Gdx.app.log("Tile Checker", "Clicked!");
            }
        });
        registry.addEntity(testDynamicTile);
        pc.chunkManager.addActor(testDynamicTile.getComponent(ActorComponent.class));

        ItemEntity testItemEntity = new ItemEntity(pc.physWorld, 94, 26, "stone_square", 1, 64);
        registry.addEntity(testItemEntity);

        ship = new Ship(game, game.gameScene.universe.getUniversalWorld(), 100, 0, 0);
        // ship.addComponent(new TrackedEntityComponent(Color.CORAL)).predictFuture = true;
        ship.addComponent(new GravityComponent(ship));
        ship.load("./saves/ships/test.ship");
        registry.addEntity(ship);
        // ship.drive(player);
        
        // OrbitUtils.createOrbit(registry, c2);
        // OrbitUtils.createOrbit(registry, c3);
        // OrbitUtils.createOrbit(registry, c4);
        // OrbitUtils.createOrbit(registry, c5);
        // OrbitUtils.createOrbit(registry, c6);
        // OrbitUtils.createOrbit(registry, ship);
        // OrbitUtils.createOrbit(registry, player);

        openPlanetView(testPlanet);
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
        spaceRenderSystem = registry.registerSystem(new SpaceRenderSystem());
        planetRenderSystem = registry.registerSystem(new PlanetRenderSystem());
        trackingSystem = registry.registerSystem(new TrackingSystem(registry));
        registry.registerSystem(new ShapeRenderSystem());
        registry.registerSystem(new GravitySystem(registry));
        registry.registerSystem(new ScriptSystem());
    }
    
    // Functions
    public void init(){
        initializeUniverse();
        initializeSpaceScene();
    }

    public void openMap(){
        mapPanel = new MapPanel(game, spacePanel, trackingSystem);
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
        planetViewPanel.getPlanet().getComponent(PlanetComponent.class).delEntityWorld(player);

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

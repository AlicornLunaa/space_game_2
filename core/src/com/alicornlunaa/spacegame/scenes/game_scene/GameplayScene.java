package com.alicornlunaa.spacegame.scenes.game_scene;

import com.alicornlunaa.selene_engine.components.BodyComponent;
import com.alicornlunaa.selene_engine.components.CameraComponent;
import com.alicornlunaa.selene_engine.ecs.Registry;
import com.alicornlunaa.selene_engine.scenes.BaseScene;
import com.alicornlunaa.selene_engine.systems.CameraSystem;
import com.alicornlunaa.selene_engine.systems.PhysicsSystem;
import com.alicornlunaa.selene_engine.systems.RenderSystem;
import com.alicornlunaa.selene_engine.systems.ScriptSystem;
import com.alicornlunaa.spacegame.App;
import com.alicornlunaa.spacegame.objects.Player;
import com.alicornlunaa.spacegame.objects.ship.Ship;
import com.alicornlunaa.spacegame.objects.simulation.Universe;
import com.alicornlunaa.spacegame.scripts.GravityScript;
import com.alicornlunaa.spacegame.systems.CustomRenderSystem;
import com.alicornlunaa.spacegame.systems.OrbitSystem;
import com.alicornlunaa.spacegame.systems.SimulatedPathSystem;
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
		universe = new Universe(registry);

        // universe.addCelestial(new Star(game, universe.getUniversalWorld(), 1000000, 0, 80000));
        // universe.addCelestial(new Planet(game, universe.getUniversalWorld(), 1000000 - 1500000, 0, 7000, 6000, 1));
        // universe.addCelestial(new Planet(game, universe.getUniversalWorld(), 1000000 - 1700000, 0, 5000, 10000, 1));

		player = new Player(game, universe.getUniversalWorld(), -50, 0);
		universe.addEntity(player);

        // universe.getCelestial(0).getComponent(GravityScript.class).start();
        // universe.getCelestial(1).getComponent(GravityScript.class).start();
        player.getComponent(GravityScript.class).start();
	}

    private void initializeSpaceScene(){
        spacePanel = new SpacePanel(game);
        spaceInterface = new SpaceInterface(game);
        spacePanel.setDebugAll(true);
        spaceInterface.setDebugAll(false);

        inputs.addProcessor(spaceInterface);
        inputs.addProcessor(spacePanel);

        // spacePanel.ship.drive(player);
        // spacePanel.ship.getComponent(GravityScript.class).start();
    }

    // Constructors
    public GameplayScene(App game) {
        // Initialize base scene
        super(game);

        // Start new engine registry
        registry = new Registry();
        registry.registerSystem(new CameraSystem(game));
        simulation = registry.registerSystem(new PhysicsSystem());
        registry.registerSystem(new RenderSystem(game));
        registry.registerSystem(new CustomRenderSystem(game));
        registry.registerSystem(new ScriptSystem());
        orbitSystem = registry.registerSystem(new OrbitSystem(game));
        registry.registerSystem(new SimulatedPathSystem(game));
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
        game.gameScene.player.bodyComponent.setWorld(shipViewPanel.ship.getBody().world);
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

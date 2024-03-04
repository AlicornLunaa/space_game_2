package com.alicornlunaa.space_game.scenes.space_scene;

import com.alicornlunaa.selene_engine.ecs.AnimationSystem;
import com.alicornlunaa.selene_engine.ecs.BodyComponent;
import com.alicornlunaa.selene_engine.ecs.CameraSystem;
import com.alicornlunaa.selene_engine.ecs.PhysicsSystem;
import com.alicornlunaa.selene_engine.ecs.RenderSystem;
import com.alicornlunaa.selene_engine.ecs.TransformComponent;
import com.alicornlunaa.selene_engine.scenes.GameScene;
import com.alicornlunaa.space_game.App;
import com.alicornlunaa.space_game.components.celestial.GravityComponent;
import com.alicornlunaa.space_game.components.celestial.TrackedEntityComponent;
import com.alicornlunaa.space_game.factories.BasicFactory;
import com.alicornlunaa.space_game.factories.CelestialFactory;
import com.alicornlunaa.space_game.factories.CharacterFactory;
import com.alicornlunaa.space_game.factories.GridFactory;
import com.alicornlunaa.space_game.systems.CelestialRenderSystem;
import com.alicornlunaa.space_game.systems.GravitySystem;
import com.alicornlunaa.space_game.systems.GridBottomRenderSystem;
import com.alicornlunaa.space_game.systems.GridPhysicsSystem;
import com.alicornlunaa.space_game.systems.GridRenderSystem;
import com.alicornlunaa.space_game.systems.PlayerSystem;
import com.alicornlunaa.space_game.systems.ShipSystem;
import com.alicornlunaa.space_game.systems.TrackingSystem;
import com.alicornlunaa.space_game.util.Constants;
import com.alicornlunaa.space_game.widgets.ConsoleWidget;
import com.badlogic.ashley.core.Engine;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;

public class SpaceScene extends GameScene {
    // Variables
    private Label infoLbl;
    private TransformComponent plyTransComp;
    private BodyComponent plyBodyComp;
    private ConsoleWidget console = new ConsoleWidget();

    // Constructor
    public SpaceScene() {
        // Initialize ashley
        Engine engine = getEngine();
        engine.addSystem(new PhysicsSystem(Constants.PPM));
        engine.addSystem(new RenderSystem());
        engine.addSystem(new CameraSystem());
        engine.addSystem(new AnimationSystem());
        engine.addSystem(new PlayerSystem());
        engine.addSystem(new GridRenderSystem());
        engine.addSystem(new GridBottomRenderSystem());
        engine.addSystem(new GridPhysicsSystem());
        engine.addSystem(new ShipSystem());
        engine.addSystem(new CelestialRenderSystem());
        engine.addSystem(new GravitySystem());
        engine.addSystem(new TrackingSystem());

        // Init camera
        App.instance.camera = new OrthographicCamera(1280 / Constants.PPM, 720 / Constants.PPM);
        App.instance.camera.position.set(1.5f, 2.5f, 0);
        App.instance.camera.update();

        // Init controls
        inputs.addProcessor(App.instance.inputs);

        // Test entities
        App.instance.playerEntity = CharacterFactory.createPlayer(4, 0, 0);
        engine.addEntity(App.instance.playerEntity);
        engine.addEntity(GridFactory.createGrid(2, 1, 0));
        engine.addEntity(BasicFactory.createBox(0.1f, 0.1f, 0.2f, 0.2f, 0));
        engine.addEntity(CelestialFactory.createCelestial(8, 0, 0, 1));

        // Gravity test
        App.instance.playerEntity.add(new TrackedEntityComponent(Color.CYAN));
        App.instance.playerEntity.add(new GravityComponent());
        engine.getEntities().get(3).add(new GravityComponent());

        // Create debug interface
        plyTransComp = App.instance.playerEntity.getComponent(TransformComponent.class);
        plyBodyComp = App.instance.playerEntity.getComponent(BodyComponent.class);

        Table debugInterface = new Table();
        debugInterface.row().expand();
        debugInterface.setFillParent(true);

        infoLbl = new Label("Null", App.instance.skin);
        debugInterface.add(infoLbl).pad(10).left().top();

        getInterface().addActor(debugInterface);
    }

    // Functions
    @Override
    public void render(float delta) {
        super.render(delta);

        if(App.instance.inputs.isKeyJustPressed("CONSOLE")){
            getInterface().addActor(console);
        }

        String txt = "Frame time: ";
        txt += delta;
        txt += "\nPosition: ";
        txt += plyTransComp.position.toString();
        txt += "\nVelocity: ";
        txt += plyBodyComp.body == null ? "(null, null)" : plyBodyComp.body.getLinearVelocity().toString();
        infoLbl.setText(txt);
    }
}

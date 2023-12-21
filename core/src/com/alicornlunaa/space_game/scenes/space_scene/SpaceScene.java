package com.alicornlunaa.space_game.scenes.space_scene;

import com.alicornlunaa.selene_engine.ecs.AnimationSystem;
import com.alicornlunaa.selene_engine.ecs.CameraSystem;
import com.alicornlunaa.selene_engine.ecs.PhysicsSystem;
import com.alicornlunaa.selene_engine.ecs.RenderSystem;
import com.alicornlunaa.selene_engine.scenes.BaseScene;
import com.alicornlunaa.space_game.App;
import com.alicornlunaa.space_game.factories.BasicFactory;
import com.alicornlunaa.space_game.factories.CharacterFactory;
import com.alicornlunaa.space_game.factories.ShipFactory;
import com.alicornlunaa.space_game.systems.PlayerSystem;
import com.alicornlunaa.space_game.systems.ShipPhysicsSystem;
import com.alicornlunaa.space_game.systems.ShipRenderSystem;
import com.alicornlunaa.space_game.util.Constants;
import com.badlogic.ashley.core.Engine;
import com.badlogic.gdx.graphics.OrthographicCamera;

public class SpaceScene extends BaseScene {
    // Variables
    private Engine engine = new Engine();

    // Constructor
    public SpaceScene() {
        // Initialize ashley
        engine.addSystem(new PhysicsSystem(Constants.PPM));
        engine.addSystem(new RenderSystem());
        engine.addSystem(new CameraSystem());
        engine.addSystem(new AnimationSystem());
        engine.addSystem(new PlayerSystem());
        engine.addSystem(new ShipRenderSystem());
        engine.addSystem(new ShipPhysicsSystem());

        // Init camera
        App.instance.camera = new OrthographicCamera(1280 / Constants.PPM, 720 / Constants.PPM);
        App.instance.camera.position.set(1.5f, 2.5f, 0);
        App.instance.camera.update();

        // Test entities
        engine.addEntity(ShipFactory.createShip(0, 1, 0));
        engine.addEntity(CharacterFactory.createPlayer(4, 0, 0));
        engine.addEntity(BasicFactory.createStaticBox(0.f, -2.5f, 8.f, 0.5f, (float)Math.toRadians(5)));
    }

    // Functions
    @Override
    public void render(float delta) {
        super.render(delta);
        engine.update(delta);
    }
}

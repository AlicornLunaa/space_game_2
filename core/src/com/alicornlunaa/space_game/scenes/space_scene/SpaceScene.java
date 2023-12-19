package com.alicornlunaa.space_game.scenes.space_scene;

import com.alicornlunaa.selene_engine.ecs.BodyComponent;
import com.alicornlunaa.selene_engine.ecs.PhysicsSystem;
import com.alicornlunaa.selene_engine.ecs.RenderSystem;
import com.alicornlunaa.selene_engine.ecs.SpriteComponent;
import com.alicornlunaa.selene_engine.ecs.TransformComponent;
import com.alicornlunaa.selene_engine.phys.Collider;
import com.alicornlunaa.selene_engine.scenes.BaseScene;
import com.alicornlunaa.space_game.App;
import com.alicornlunaa.space_game.factories.ShipFactory;
import com.alicornlunaa.space_game.systems.ShipSystem;
import com.alicornlunaa.space_game.util.Constants;
import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;

public class SpaceScene extends BaseScene {
    // Variables
    private Engine engine = new Engine();
    private PhysicsSystem physics = new PhysicsSystem(Constants.PPM);

    // Constructor
    public SpaceScene() {
        // Initialize ashley
        engine.addSystem(physics);
        engine.addSystem(new RenderSystem());
        // engine.addSystem(new CameraSystem());
        engine.addSystem(new ShipSystem());

        // Init camera
        App.instance.camera = new OrthographicCamera(1280 / Constants.PPM, 720 / Constants.PPM);
        App.instance.camera.position.set(0, 0, 0);
        App.instance.camera.update();

        // Test entity
        engine.addEntity(ShipFactory.createShip(0, 1, 0));
        
        // Ground entity
        TransformComponent transform = new TransformComponent();
        transform.position.set(0, -2.5f);
        transform.rotation = (float)Math.toRadians(5);

        BodyComponent bodyComponent = new BodyComponent(Collider.box(0, 0, 4.f, 0.5f, 0));
        bodyComponent.bodyDef.type = BodyType.StaticBody;

        Entity e = new Entity();
        e.add(transform);
        e.add(bodyComponent);
        e.add(new SpriteComponent(App.instance.atlas.findRegion("dev_texture"), 8, 1));
        engine.addEntity(e);
    }

    // Functions
    @Override
    public void render(float delta) {
        super.render(delta);
        engine.update(delta);
        App.instance.debug.render(physics.getWorld().getBox2DWorld(), App.instance.camera.combined);
    }
}

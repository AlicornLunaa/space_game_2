package com.alicornlunaa.spacegame.scenes.testing_scene;

import com.alicornlunaa.selene_engine.components.CameraComponent;
import com.alicornlunaa.selene_engine.ecs.Registry;
import com.alicornlunaa.selene_engine.phys.PhysWorld;
import com.alicornlunaa.selene_engine.systems.CameraSystem;
import com.alicornlunaa.selene_engine.systems.PhysicsSystem;
import com.alicornlunaa.selene_engine.systems.RenderSystem;
import com.alicornlunaa.selene_engine.systems.ScriptSystem;
import com.alicornlunaa.spacegame.App;
import com.alicornlunaa.spacegame.objects.Player;
import com.alicornlunaa.spacegame.objects.ship.Ship;
import com.alicornlunaa.spacegame.systems.CustomRenderSystem;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.utils.ScreenUtils;

public class TestScreen implements Screen {
    // Variables
    private Registry registry;
    private PhysicsSystem simulation;
    private PhysWorld world;
    
    // Constructor
    public TestScreen(){
        registry = new Registry();
        registry.registerSystem(new CameraSystem(App.instance));
        simulation = registry.registerSystem(new PhysicsSystem());
        registry.registerSystem(new RenderSystem(App.instance));
        registry.registerSystem(new CustomRenderSystem(App.instance));
        registry.registerSystem(new ScriptSystem());

        world = new PhysWorld(128.0f);
        simulation.addWorld(world);

        Ship ship = new Ship(App.instance, world, -64, 0, 0);
        ship.load("./saves/ships/test.ship");
        registry.addEntity(ship);

        final Player p = new Player(App.instance, world, -100, 0);
        p.getComponent(CameraComponent.class).active = true;
        registry.addEntity(p);
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(0.02f, 0.02f, 0.02f, 1);

        registry.update(delta);
        registry.render();
        App.instance.debug.render(world.getBox2DWorld(), App.instance.camera.combined.cpy().scl(world.getPhysScale()));
    }

    @Override
    public void show() {
        // Gdx.input.setInputProcessor(stage);
    }

    @Override
    public void resize(int width, int height) {}

    @Override
    public void pause() {}

    @Override
    public void resume() {}

    @Override
    public void hide() {}

    @Override
    public void dispose() {}
    
}
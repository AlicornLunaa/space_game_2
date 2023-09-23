package com.alicornlunaa.spacegame.scenes.testing_scene;

import com.alicornlunaa.selene_engine.components.CameraComponent;
import com.alicornlunaa.selene_engine.ecs.Registry;
import com.alicornlunaa.selene_engine.phys.PhysWorld;
import com.alicornlunaa.selene_engine.systems.CameraSystem;
import com.alicornlunaa.selene_engine.systems.PhysicsSystem;
import com.alicornlunaa.selene_engine.systems.RenderSystem;
import com.alicornlunaa.selene_engine.systems.ScriptSystem;
import com.alicornlunaa.spacegame.App;
import com.alicornlunaa.spacegame.components.CustomSpriteComponent;
import com.alicornlunaa.spacegame.objects.Player;
import com.alicornlunaa.spacegame.objects.planet.Planet;
import com.alicornlunaa.spacegame.objects.ship.Ship;
import com.alicornlunaa.spacegame.objects.simulation.Universe;
import com.alicornlunaa.spacegame.objects.simulation.orbits.Orbit;
import com.alicornlunaa.spacegame.phys.CelestialPhysWorld;
import com.alicornlunaa.spacegame.scripts.GravityScript;
import com.alicornlunaa.spacegame.systems.SpaceRenderSystem;
import com.alicornlunaa.spacegame.util.Constants;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.utils.ScreenUtils;

public class TestScreen implements Screen {
    // Variables
    private Registry registry;
    private PhysicsSystem simulation;
    private PhysWorld world;

    private Universe universe;
    private Orbit orbit;
    private InputMultiplexer inputs = new InputMultiplexer();
    
    // Constructor
    public TestScreen(){
        registry = new Registry();
        registry.registerSystem(new CameraSystem(App.instance));
        simulation = registry.registerSystem(new PhysicsSystem());
        registry.registerSystem(new RenderSystem(App.instance));
        registry.registerSystem(new SpaceRenderSystem(universe));
        registry.registerSystem(new ScriptSystem());

        universe = new Universe(registry);
        world = new CelestialPhysWorld(null, 128.0f);
        simulation.addWorld(world);

        float scale = 0.1f;
        float au = 9.296e4f / 2;
        // Star star = new Star(world, 432690 * scale, 432690 * scale, 0);
        // registry.addEntity(star);
        // universe.addCelestial(star);
        // Celestial2 mercury = new Celestial2(world, star, 1516 * scale, 0.3871f * au * scale, 0.20564f, 77, 0, 0);
        // registry.addEntity(mercury);
        // universe.addCelestial(mercury);
        // Celestial2 venus = new Celestial2(world, star, 3760 * scale, 0.7233f * au * scale, 0.00676f, 131, 0, 0);
        // registry.addEntity(venus);
        // universe.addCelestial(venus);
        // Celestial2 earth = new Celestial2(world, star, 3958 * scale, 1.0f * au * scale, 0.01673f, 102, 0, 0);
        // registry.addEntity(earth);
        // universe.addCelestial(earth);

        // Celestial2 testPlanet = new Celestial2(world, star, 50000 * scale, 600, 0.001f, 0, 0, 0);
        // registry.addEntity(testPlanet);
        // universe.addCelestial(testPlanet);
        
        Planet test = new Planet(simulation, world, -1000, 0, 500, 560, 1);
        registry.addEntity(test);

        Ship ship = new Ship(App.instance, world, -64, 0, 0);
        ship.load("./saves/ships/test.ship");
        ship.addComponent(new GravityScript(universe, ship));
        registry.addEntity(ship);

        Player p = new Player(App.instance, world, -100, 0);
        p.getComponent(CameraComponent.class).active = true;
        registry.addEntity(p);
        ship.drive(p);
        orbit = new Orbit(universe, ship);
        p.addComponent(new GravityScript(universe, p));
        // p.addComponent(new PlayerLocalizerScript(registry, p));
        p.addComponent(new CustomSpriteComponent() {
            @Override
            public void render(Batch batch) {
                batch.end();
                App.instance.shapeRenderer.setProjectionMatrix(batch.getProjectionMatrix());
                App.instance.shapeRenderer.setTransformMatrix(batch.getTransformMatrix());
                App.instance.shapeRenderer.begin(ShapeType.Filled);

                Constants.DEBUG = false;
                orbit.recalculate();
                orbit.draw(App.instance.shapeRenderer, 1000);
                Constants.DEBUG = true;

                App.instance.shapeRenderer.end();
                batch.begin();
            }
        });

        inputs.addProcessor(new InputAdapter(){
            @Override
            public boolean scrolled(float amountX, float amountY){
                float speed = Constants.MAP_VIEW_ZOOM_SENSITIVITY * App.instance.camera.zoom * amountY;
                App.instance.camera.zoom = Math.min(Math.max(App.instance.camera.zoom + speed, 0.01f), 300000.0f);
                return true;
            }
        });
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
        Gdx.input.setInputProcessor(inputs);
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
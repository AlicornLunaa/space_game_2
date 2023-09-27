package com.alicornlunaa.spacegame.scenes.testing_scene;

import com.alicornlunaa.selene_engine.components.BodyComponent;
import com.alicornlunaa.selene_engine.components.CameraComponent;
import com.alicornlunaa.selene_engine.components.ScriptComponent;
import com.alicornlunaa.selene_engine.components.SpriteComponent;
import com.alicornlunaa.selene_engine.components.TextureComponent;
import com.alicornlunaa.selene_engine.components.TransformComponent;
import com.alicornlunaa.selene_engine.core.BaseEntity;
import com.alicornlunaa.selene_engine.core.IEntity;
import com.alicornlunaa.selene_engine.ecs.Registry;
import com.alicornlunaa.selene_engine.phys.PhysWorld;
import com.alicornlunaa.selene_engine.systems.CameraSystem;
import com.alicornlunaa.selene_engine.systems.PhysicsSystem;
import com.alicornlunaa.selene_engine.systems.RenderSystem;
import com.alicornlunaa.selene_engine.systems.ScriptSystem;
import com.alicornlunaa.selene_engine.systems.ShapeRenderSystem;
import com.alicornlunaa.spacegame.App;
import com.alicornlunaa.spacegame.objects.simulation.Universe;
import com.alicornlunaa.spacegame.systems.SpaceRenderSystem;
import com.alicornlunaa.spacegame.util.Constants;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.utils.ScreenUtils;

public class TestScreen implements Screen {
    // Static classes
    private static class TestEntity extends BaseEntity {
        public TestEntity(float x, float y){
            addComponent(new TextureComponent(App.instance.manager, "textures/dev_texture.png"));
            addComponent(new SpriteComponent(32, 32));
            getComponent(TransformComponent.class).position.set(x, y);
        }
    };

    // Variables
    private Registry registry;
    private PhysicsSystem simulation;
    private PhysWorld world;

    private Universe universe;
    private InputMultiplexer inputs = new InputMultiplexer();
    private float totalEnergy = 0.0f;

    // Private functions
    private float orbitVelocity(IEntity parent, IEntity entity){
        TransformComponent trans1 = parent.getComponent(TransformComponent.class);
        TransformComponent trans2 = entity.getComponent(TransformComponent.class);
        GravityComponent grav1 = parent.getComponent(GravityComponent.class);

        float radius = trans1.position.dst(trans2.position);
        return (float)Math.sqrt(Constants.GRAVITY_CONSTANT * grav1.getMass() / radius);
    }
    
    private float energy(IEntity entity){
        GravityComponent gravityComponent = entity.getComponent(GravityComponent.class);
        BodyComponent bodyComponent = entity.getComponent(BodyComponent.class);
        return (1.f / 2.f) * (bodyComponent.body.getMass() * gravityComponent.velocity.len2());
    }

    // Constructor
    public TestScreen(){
        registry = new Registry();
        registry.registerSystem(new CameraSystem(App.instance));
        simulation = registry.registerSystem(new PhysicsSystem());
        registry.registerSystem(new RenderSystem(App.instance));
        registry.registerSystem(new ShapeRenderSystem());
        registry.registerSystem(new SpaceRenderSystem(universe));
        TrackingSystem trackingSystem = registry.registerSystem(new TrackingSystem(registry));
        TrailSystem trailSystem = registry.registerSystem(new TrailSystem());
        registry.registerSystem(new ScriptSystem());

        universe = new Universe(registry);
        world = new PhysWorld(128);
        simulation.addWorld(world);

        BodyDef def = new BodyDef();
        def.type = BodyType.DynamicBody;

        BaseEntity cameraEntity = new BaseEntity();
        // cameraEntity.addComponent(new CameraComponent(1280, 720));
        registry.addEntity(cameraEntity);

        TestEntity ent1 = new TestEntity(0, 0);
        ent1.addComponent(new BodyComponent(world, def));
        ent1.addComponent(new GravityComponent(ent1, registry, 0, 0, 100000));
        ent1.addComponent(new TrailComponent(Color.RED));
        registry.addEntity(ent1);

        TestEntity ent2 = new TestEntity(2000, 0);
        ent2.addComponent(new BodyComponent(world, def));
        ent2.addComponent(new GravityComponent(ent2, registry, 0, orbitVelocity(ent1, ent2), 15000));
        ent2.addComponent(new TrailComponent(Color.PINK));
        // ent2.addComponent(new CameraComponent(1280, 720));
        registry.addEntity(ent2);

        TestEntity ent3 = new TestEntity(2200, 0);
        ent3.addComponent(new GravityComponent(ent3, registry, 0, orbitVelocity(ent1, ent3) + orbitVelocity(ent2, ent3), 10));
        ent3.addComponent(new TrailComponent(Color.CYAN));
        registry.addEntity(ent3);

        TestEntity ent4 = new TestEntity(400, 0);
        ent4.addComponent(new GravityComponent(ent4, registry, 0, orbitVelocity(ent1, ent4), 1000));
        ent4.addComponent(new TrailComponent(Color.LIME));
        registry.addEntity(ent4);

        TestEntity ent5 = new TestEntity(750, 0);
        ent5.addComponent(new GravityComponent(ent5, registry, 0, orbitVelocity(ent1, ent5), 1000));
        ent5.addComponent(new TrailComponent(Color.YELLOW));
        registry.addEntity(ent5);

        trackingSystem.setReferenceEntity(ent2);
        trailSystem.setReferenceEntity(ent2);
        ent2.addComponent(new CameraComponent(1280, 720));

        TestEntity player = new TestEntity(1000, 0);
        player.addComponent(new BodyComponent(world, def));
        player.addComponent(new GravityComponent(player, registry, 0, orbitVelocity(ent1, player), 0.01f));
        player.addComponent(new TrackedEntityComponent(Color.CORAL));
        // player.addComponent(new CameraComponent(1280, 720));
        player.addComponent(new ScriptComponent(player) {
            private TransformComponent trans = getEntity().getComponent(TransformComponent.class);
            private GravityComponent gc = getEntity().getComponent(GravityComponent.class);

            @Override
            public void start() {}

            @Override
            public void update() {
                if(Gdx.input.isKeyPressed(Keys.W)){
                    gc.acceleration.y += 0.005f;
                }
                if(Gdx.input.isKeyPressed(Keys.S)){
                    gc.acceleration.y -= 0.005f;
                }
                if(Gdx.input.isKeyPressed(Keys.A)){
                    gc.acceleration.x -= 0.005f;
                }
                if(Gdx.input.isKeyPressed(Keys.D)){
                    gc.acceleration.x += 0.005f;
                }

                Vector2 offset = trans.position.cpy();
                if(trans.position.len() > 15000){
                    for(int i = 0; i < registry.getEntities().size; i++){
                        IEntity otherEntity = registry.getEntity(i);
                        TransformComponent otherTrans = otherEntity.getComponent(TransformComponent.class);
                        otherTrans.position.sub(offset);
                    }
                }
            }

            @Override
            public void render() {
            }
        });
        registry.addEntity(player);

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

        totalEnergy = 0;
        for(IEntity entity : registry.getEntities()){
            if(!entity.hasComponents(GravityComponent.class, BodyComponent.class)) continue;
            totalEnergy += energy(entity);
        }
        // System.out.println(totalEnergy);

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
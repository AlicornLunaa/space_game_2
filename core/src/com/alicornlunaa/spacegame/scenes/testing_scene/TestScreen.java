package com.alicornlunaa.spacegame.scenes.testing_scene;

import com.alicornlunaa.selene_engine.components.BodyComponent;
import com.alicornlunaa.selene_engine.components.CameraComponent;
import com.alicornlunaa.selene_engine.components.CircleColliderComponent;
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
import com.alicornlunaa.spacegame.util.Constants;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.utils.ScreenUtils;

public class TestScreen implements Screen {
    // Static classes
    static private class TestEntity extends BaseEntity {
        public TestEntity(float x, float y){
            addComponent(new TextureComponent(App.instance.manager, "textures/dev_texture.png"));
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
        BodyComponent bc = parent.getComponent(BodyComponent.class);

        float radius = trans1.position.dst(trans2.position);
        return (float)Math.sqrt(Constants.GRAVITY_CONSTANT * bc.body.getMass() / radius);
    }
    
    private float energy(IEntity entity){
        TransformComponent transform = entity.getComponent(TransformComponent.class);
        BodyComponent bodyComponent = entity.getComponent(BodyComponent.class);

        float kineticEnergy = (1.f / 2.f) * (bodyComponent.body.getMass() * bodyComponent.body.getLinearVelocity().len2());
        float potentialEnergy = 0.f;

        for(int i = 0; i < registry.getEntities().size; i++){
            // Calculate gravity for every n-body
            IEntity otherEntity = registry.getEntity(i);

            if(otherEntity == entity) continue; // Prevent infinite forces

            TransformComponent otherTransform = otherEntity.getComponent(TransformComponent.class);
            BodyComponent otherBodyComponent = otherEntity.getComponent(BodyComponent.class);
            GravityComponent otherGravity = otherEntity.getComponent(GravityComponent.class);

            // Only apply if they also have a gravity component
            if(otherGravity != null){
                // Get variables
                float radius = transform.position.dst(otherTransform.position);
                float soi = otherGravity.getSphereOfInfluence();
                
                // Prevent insignificant forces
                if(radius > soi)
                    continue;

                // Calculate gravitational force
                potentialEnergy += (Constants.GRAVITY_CONSTANT * otherBodyComponent.body.getMass() * bodyComponent.body.getMass() * -1) / radius;
            }
        }

        return kineticEnergy + potentialEnergy;
    }

    // Constructor
    public TestScreen(){
        registry = new Registry();
        registry.registerSystem(new CameraSystem(App.instance));
        simulation = registry.registerSystem(new PhysicsSystem());
        registry.registerSystem(new RenderSystem(App.instance));
        registry.registerSystem(new ShapeRenderSystem());
        final TrackingSystem trackingSystem = registry.registerSystem(new TrackingSystem(registry));
        final TrailSystem trailSystem = registry.registerSystem(new TrailSystem());
        registry.registerSystem(new ScriptSystem());

        universe = new Universe(registry);
        world = new PhysWorld(128);
        simulation.addWorld(world);

        BodyDef def = new BodyDef();
        def.type = BodyType.DynamicBody;

        BaseEntity cameraEntity = new BaseEntity();
        registry.addEntity(cameraEntity);

        final TestEntity ent1 = new TestEntity(0, 0);
        ent1.addComponent(new SpriteComponent(400, 400));
        ent1.addComponent(new BodyComponent(world, def));
        ent1.addComponent(new CircleColliderComponent(ent1.getComponent(BodyComponent.class), 200, 10));
        ent1.addComponent(new GravityComponent(ent1, registry));
        ent1.addComponent(new TrailComponent(Color.RED));
        ent1.addComponent(new CameraComponent(1280, 720)).active = true;
        registry.addEntity(ent1);

        //testicle
        TestEntity player = new TestEntity(400, 0);
        player.addComponent(new SpriteComponent(2, 2));
        player.addComponent(new BodyComponent(world, def));
        player.addComponent(new CircleColliderComponent(player.getComponent(BodyComponent.class), 1, 0.01f));
        player.addComponent(new GravityComponent(player, registry));
        player.addComponent(new TrailComponent(Color.CORAL));
        player.addComponent(new TrackedEntityComponent(Color.LIME));
        player.addComponent(new ScriptComponent(player) {
            private BodyComponent bc = getEntity().getComponent(BodyComponent.class);

            @Override
            public void start() {}

            @Override
            public void update() {
                if(Gdx.input.isKeyPressed(Keys.W)){
                    bc.body.applyLinearImpulse(0, 0.00155f, bc.body.getWorldCenter().x, bc.body.getWorldCenter().y, true);
                }
                if(Gdx.input.isKeyPressed(Keys.S)){
                    bc.body.applyLinearImpulse(0, -0.00155f, bc.body.getWorldCenter().x, bc.body.getWorldCenter().y, true);
                }
                if(Gdx.input.isKeyPressed(Keys.A)){
                    bc.body.applyLinearImpulse(-0.00155f, 0, bc.body.getWorldCenter().x, bc.body.getWorldCenter().y, true);
                }
                if(Gdx.input.isKeyPressed(Keys.D)){
                    bc.body.applyLinearImpulse(0.00155f, 0, bc.body.getWorldCenter().x, bc.body.getWorldCenter().y, true);
                }

                // Vector2 offset = trans.position.cpy();
                // if(trans.position.len() > 15000){
                //     for(int i = 0; i < registry.getEntities().size; i++){
                //         IEntity otherEntity = registry.getEntity(i);
                //         TransformComponent otherTrans = otherEntity.getComponent(TransformComponent.class);
                //         otherTrans.position.sub(offset);
                //     }
                // }
            }

            @Override
            public void render() {
            }
        });
        player.getComponent(BodyComponent.class).body.setLinearVelocity(0, orbitVelocity(ent1, player));
        player.addComponent(new CameraComponent(1280, 720)).active = false;
        registry.addEntity(player);

        TestEntity ent2 = new TestEntity(1900, 0);
        ent2.addComponent(new SpriteComponent(80, 80));
        ent2.addComponent(new BodyComponent(world, def));
        ent2.addComponent(new CircleColliderComponent(ent2.getComponent(BodyComponent.class), 40, 12));
        ent2.addComponent(new GravityComponent(ent2, registry));
        ent2.addComponent(new TrailComponent(Color.PINK));
        ent2.getComponent(BodyComponent.class).body.setLinearVelocity(0, orbitVelocity(ent1, ent2));
        ent2.addComponent(new CameraComponent(1280, 720)).active = false;
        registry.addEntity(ent2);

        TestEntity ent3 = new TestEntity(2400, 0);
        ent3.addComponent(new SpriteComponent(20, 20));
        ent3.addComponent(new BodyComponent(world, def));
        ent3.addComponent(new CircleColliderComponent(ent3.getComponent(BodyComponent.class), 10, 10));
        ent3.addComponent(new GravityComponent(ent3, registry));
        ent3.addComponent(new TrailComponent(Color.CYAN));
        ent3.addComponent(new TrackedEntityComponent(Color.LIME));
        ent3.getComponent(BodyComponent.class).body.setLinearVelocity(0, orbitVelocity(ent1, ent3) + orbitVelocity(ent2, ent3));
        ent3.addComponent(new CameraComponent(1280, 720)).active = false;
        ent3.addComponent(new ScriptComponent(ent3) {
            private BodyComponent bc = getEntity().getComponent(BodyComponent.class);

            @Override
            public void start() {}

            @Override
            public void update() {
                if(Gdx.input.isKeyPressed(Keys.I)){
                    bc.body.applyLinearImpulse(0, 4.955f, bc.body.getWorldCenter().x, bc.body.getWorldCenter().y, true);
                }
                if(Gdx.input.isKeyPressed(Keys.K)){
                    bc.body.applyLinearImpulse(0, -4.955f, bc.body.getWorldCenter().x, bc.body.getWorldCenter().y, true);
                }
                if(Gdx.input.isKeyPressed(Keys.J)){
                    bc.body.applyLinearImpulse(-4.955f, 0, bc.body.getWorldCenter().x, bc.body.getWorldCenter().y, true);
                }
                if(Gdx.input.isKeyPressed(Keys.L)){
                    bc.body.applyLinearImpulse(4.955f, 0, bc.body.getWorldCenter().x, bc.body.getWorldCenter().y, true);
                }
            }

            @Override
            public void render() {
            }
        });
        registry.addEntity(ent3);

        TestEntity ent4 = new TestEntity(750, 0);
        ent4.addComponent(new SpriteComponent(20, 20));
        ent4.addComponent(new BodyComponent(world, def));
        ent4.addComponent(new CircleColliderComponent(ent4.getComponent(BodyComponent.class), 10, 4));
        ent4.addComponent(new GravityComponent(ent4, registry));
        ent4.addComponent(new TrailComponent(Color.LIME));
        ent4.getComponent(BodyComponent.class).body.setLinearVelocity(0, orbitVelocity(ent1, ent4));
        ent4.addComponent(new CameraComponent(1280, 720)).active = false;
        registry.addEntity(ent4);

        TestEntity ent5 = new TestEntity(3300, 0);
        ent5.addComponent(new SpriteComponent(20, 20));
        ent5.addComponent(new BodyComponent(world, def));
        ent5.addComponent(new CircleColliderComponent(ent5.getComponent(BodyComponent.class), 10, 90));
        ent5.addComponent(new GravityComponent(ent5, registry));
        ent5.addComponent(new TrailComponent(Color.YELLOW));
        ent5.getComponent(BodyComponent.class).body.setLinearVelocity(0, orbitVelocity(ent1, ent5));
        ent5.addComponent(new CameraComponent(1280, 720)).active = false;
        registry.addEntity(ent5);

        trackingSystem.setReferenceEntity(cameraEntity);
        trailSystem.setReferenceEntity(cameraEntity);
        cameraEntity.addComponent(new CameraComponent(1280, 720));
        
        final IEntity[] focusableEntities = { cameraEntity, ent1, player, ent2, ent3, ent4, ent5 };

        inputs.addProcessor(new InputAdapter(){
            int index = 0;

            @Override
            public boolean keyTyped(char character) {
                if(character == 'r'){
                    focusableEntities[index].getComponent(CameraComponent.class).active = false;
                    index = Math.floorMod(index + 1, focusableEntities.length);
                    focusableEntities[index].getComponent(CameraComponent.class).active = true;
                    trackingSystem.setReferenceEntity(focusableEntities[index]);
                    trailSystem.setReferenceEntity(focusableEntities[index]);
                    return true;
                } else if(character == 'e'){
                    focusableEntities[index].getComponent(CameraComponent.class).active = false;
                    index = Math.floorMod(index - 1, focusableEntities.length);
                    focusableEntities[index].getComponent(CameraComponent.class).active = true;
                    trackingSystem.setReferenceEntity(focusableEntities[index]);
                    trailSystem.setReferenceEntity(focusableEntities[index]);
                    return true;
                }

                return false;
            }

            @Override
            public boolean scrolled(float amountX, float amountY){
                if(App.instance.camera == null) return false;
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

        App.instance.debug.render(world.getBox2DWorld(), App.instance.camera.combined);
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
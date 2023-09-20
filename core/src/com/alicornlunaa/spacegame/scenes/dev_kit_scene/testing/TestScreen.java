package com.alicornlunaa.spacegame.scenes.dev_kit_scene.testing;

import com.alicornlunaa.selene_engine.components.BodyComponent;
import com.alicornlunaa.selene_engine.components.BoxColliderComponent;
import com.alicornlunaa.selene_engine.components.CameraComponent;
import com.alicornlunaa.selene_engine.components.IScriptComponent;
import com.alicornlunaa.selene_engine.components.SpriteComponent;
import com.alicornlunaa.selene_engine.components.TextureComponent;
import com.alicornlunaa.selene_engine.components.TransformComponent;
import com.alicornlunaa.selene_engine.components.SpriteComponent.AnchorPoint;
import com.alicornlunaa.selene_engine.core.BaseEntity;
import com.alicornlunaa.selene_engine.core.IEntity;
import com.alicornlunaa.selene_engine.ecs.Registry;
import com.alicornlunaa.selene_engine.phys.PhysWorld;
import com.alicornlunaa.selene_engine.systems.CameraSystem;
import com.alicornlunaa.selene_engine.systems.PhysicsSystem;
import com.alicornlunaa.selene_engine.systems.RenderSystem;
import com.alicornlunaa.selene_engine.systems.ScriptSystem;
import com.alicornlunaa.spacegame.App;
import com.alicornlunaa.spacegame.objects.Player;
import com.alicornlunaa.spacegame.objects.ship.Ship;
import com.alicornlunaa.spacegame.objects.simulation.Celestial2;
import com.alicornlunaa.spacegame.objects.simulation.orbits.EllipticalConic;
import com.alicornlunaa.spacegame.systems.CustomRenderSystem;
import com.alicornlunaa.spacegame.util.Constants;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

public class TestScreen implements Screen {
    public static class WorldEntity extends BaseEntity {
        public WorldEntity(App game, PhysWorld world){
            addComponent(new TextureComponent(game.manager, "textures/dev_texture_32.png"));
            addComponent(new SpriteComponent(512, 16, AnchorPoint.CENTER));
            addComponent(new BodyComponent(world, new BodyDef()));
            addComponent(new BoxColliderComponent(getComponent(BodyComponent.class), 2.0f, 0.0625f, 1.f));
            
            getComponent(BodyComponent.class).body.setTransform(0, -1, 0);
        }
    }

    public static class TestEntity extends BaseEntity {
        public static Array<TestEntity> ents = new Array<>();

        public TestEntity(final App game, PhysWorld world, float x, float y, float density){
            BodyDef def = new BodyDef();
            def.type = BodyType.DynamicBody;

            addComponent(new TextureComponent(game.manager, "textures/dev_texture_32.png"));
            addComponent(new SpriteComponent(Constants.PPM * 0.05f * 2, Constants.PPM * 0.05f * 2, AnchorPoint.CENTER));
            addComponent(new BodyComponent(world, def));
            addComponent(new BoxColliderComponent(getComponent(BodyComponent.class), 0.05f, 0.05f, density));

            TransformComponent trans = getComponent(TransformComponent.class);
            BodyComponent b1 = getComponent(BodyComponent.class);
            trans.position.set(x, y);
            b1.sync(trans);
            ents.add(this);
        }
    }

    private final App game;
    private Stage stage;
    
    private Registry registry;
    private PhysicsSystem simulation;
    private PhysWorld world;
    private ShapeRenderer shapes = new ShapeRenderer();

    public Array<Celestial2> celestials = new Array<>();
    private EllipticalConic conic;

    private void createOrbit(IEntity parent, IEntity entity){
        BodyComponent b1 = entity.getComponent(BodyComponent.class);
        BodyComponent b2 = parent.getComponent(BodyComponent.class);

        Vector2 tangentDirection = b1.body.getPosition().cpy().sub(b2.body.getPosition()).nor().rotateDeg(90);
        float orbitalRadius = b1.body.getPosition().dst(b2.body.getPosition().cpy());
        float speed = (float)Math.sqrt((Constants.GRAVITY_CONSTANT * b2.body.getMass()) / orbitalRadius);

        b1.body.setLinearVelocity(tangentDirection.scl(speed).add(b1.body.getLinearVelocity()));
    }

    public Vector2 startVel(IEntity parent, IEntity entity){
        TransformComponent entityTransform = entity.getComponent(TransformComponent.class);
        TransformComponent celestialTransform = parent.getComponent(TransformComponent.class);
        BodyComponent celestialBodyComponent = parent.getComponent(BodyComponent.class);

        Vector2 tangentDirection = entityTransform.position.cpy().sub(celestialTransform.position).nor().rotateDeg(90);
        float orbitalRadius = entityTransform.position.dst(celestialTransform.position) / celestialBodyComponent.world.getPhysScale();
        float speed = (float)Math.sqrt((Constants.GRAVITY_CONSTANT * celestialBodyComponent.body.getMass()) / orbitalRadius);

        return tangentDirection.scl(speed);
    }

    public Celestial2 getParentCelestial(IEntity e){
        TransformComponent transform = e.getComponent(TransformComponent.class);
        BodyComponent bodyComponent = e.getComponent(BodyComponent.class);
        
        if(bodyComponent == null) return null;

        // Find closest
        Celestial2 parent = null;
        float minDistance = Float.MAX_VALUE;
        float minSOISize = Float.MAX_VALUE;

        for(Celestial2 c : celestials){
            float curDistance = c.getComponent(TransformComponent.class).position.dst(transform.position);
            float curSOI = c.getSphereOfInfluence();
            
            if(e == c) continue;
            if(curDistance >= minDistance || curDistance >= curSOI) continue;
            if(curSOI >= minSOISize) continue;
            if(bodyComponent.body.getMass() >= c.getComponent(BodyComponent.class).body.getMass()) continue;

            parent = c;
            minDistance = curDistance;
            minSOISize = curSOI;
        }

        return parent;
    }

    public TestScreen(final App game){
        this.game = game;

        stage = new Stage(new ScreenViewport());
        stage.setDebugAll(true);
        stage.addListener(new InputListener(){
            @Override
            public boolean keyDown(InputEvent event, int keycode){
                if(keycode == Keys.F5){
                    registry.reload(game.manager);
                    return true;
                }

                return false;
            }
        });

        registry = new Registry();
        registry.registerSystem(new CameraSystem(game));
        simulation = registry.registerSystem(new PhysicsSystem());
        registry.registerSystem(new RenderSystem(game));
        registry.registerSystem(new CustomRenderSystem(game));
        registry.registerSystem(new ScriptSystem());

        world = new PhysWorld(128.0f);
        simulation.addWorld(world);

        Ship ship = new Ship(game, world, -64, 0, 0);
        ship.load("./saves/ships/test.ship");
        registry.addEntity(ship);

        final Player p = new Player(game, world, -100, 0);
        p.getComponent(CameraComponent.class).active = true;
        p.addComponent(new IScriptComponent() {
            @Override
            public void start() {}

            @Override
            public void render() {}

            @Override
            public void update() {
                p.getComponent(CameraComponent.class).camera.zoom = 10.1f;

                Celestial2 parent = getParentCelestial(p);
                if(parent != null){
                    Body a = p.getComponent(BodyComponent.class).body;
                    Body b = parent.getComponent(BodyComponent.class).body;
                    
                    float m1 = a.getMass();
                    float m2 = b.getMass();
                    float r = b.getPosition().dst(a.getPosition());
                    Vector2 direction = b.getPosition().cpy().sub(a.getPosition()).cpy().nor();
                    a.applyForceToCenter(direction.scl(Constants.GRAVITY_CONSTANT * (m1 * m2) / (r * r)), true);
                    
                    p.getComponent(TransformComponent.class).sync(p.getComponent(BodyComponent.class));
                    conic = new EllipticalConic(parent, p);
                }
            }
        });
        registry.addEntity(p);

        Celestial2 celestial1 = new Celestial2(world, 1000, 2000, 0);
        celestials.add(celestial1);
        createOrbit(celestial1, p);
        registry.addEntity(celestial1);

        Celestial2 celestial2 = new Celestial2(world, celestial1, 200, 49000, 0, 0, 0.2f);
        celestial2.getComponent(TransformComponent.class).velocity.set(startVel(celestial1, celestial2));
        celestial2.getComponent(BodyComponent.class).sync(celestial2.getComponent(TransformComponent.class));
        celestial2.updateConic();
        celestials.add(celestial2);
        registry.addEntity(celestial2);
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(0.1f, 0.1f, 0.1f, 1);

        registry.update(delta);
        stage.act(delta);

        registry.render();
        stage.draw();

        shapes.begin(ShapeType.Filled);
        shapes.setProjectionMatrix(game.camera.combined);
        shapes.setTransformMatrix(new Matrix4());
        
        Constants.DEBUG = false;
        if(conic != null) conic.draw(shapes, 40);
        Constants.DEBUG = true;

        shapes.end();

        game.debug.render(world.getBox2DWorld(), game.camera.combined.cpy().scl(world.getPhysScale()));
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(stage);
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
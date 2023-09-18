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
import com.alicornlunaa.spacegame.objects.simulation.orbits.EllipticalConic;
import com.alicornlunaa.spacegame.scenes.game_scene.ShipViewPanel;
import com.alicornlunaa.spacegame.systems.CustomRenderSystem;
import com.alicornlunaa.spacegame.systems.EditorRenderSystem;
import com.alicornlunaa.spacegame.util.Constants;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

@SuppressWarnings("unused")
public class TestScreen implements Screen {

    public static final float GRAV_C = 10;

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

            addComponent(new IScriptComponent() {
                @Override
                public void start() {}

                @Override
                public void render() {}

                @Override
                public void update() {
                    for(IEntity entity : ents){
                        BodyComponent b1 = getComponent(BodyComponent.class);
                        BodyComponent b2 = entity.getComponent(BodyComponent.class);

                        if(b1 == b2) continue;
                        if(!(entity instanceof TestEntity)) continue;

                        Vector2 relative = b1.body.getPosition().cpy().sub(b2.body.getPosition());
                        Vector2 dir = relative.cpy().nor().scl(-1);
                        float radius = relative.len();

                        // b1.body.applyForceToCenter(dir.scl(GRAV_C * (b1.body.getMass() * b2.body.getMass()) / (radius * radius)), true);
                    }
                }
            });

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

    private void createOrbit(IEntity parent, IEntity entity){
        BodyComponent b1 = entity.getComponent(BodyComponent.class);
        BodyComponent b2 = parent.getComponent(BodyComponent.class);

        Vector2 tangentDirection = b1.body.getPosition().cpy().sub(b2.body.getPosition()).nor().rotateDeg(90);
        float orbitalRadius = b1.body.getPosition().dst(b2.body.getPosition().cpy());
        float speed = (float)Math.sqrt((GRAV_C * b2.body.getMass()) / orbitalRadius);

        b1.body.setLinearVelocity(tangentDirection.scl(speed).add(b1.body.getLinearVelocity()));
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
        // world.getBox2DWorld().setGravity(new Vector2(0, -0.5f));
        simulation.addWorld(world);

        registry.addEntity(new TestEntity(game, world, 0, 100, 1000));
        registry.addEntity(new TestEntity(game, world, 100, 100, 100));
        registry.addEntity(new TestEntity(game, world, 150, 100, 0.0001f));
        // registry.addEntity(new TestEntity(game, world, -300, 400));
        // registry.addEntity(new TestEntity(game, world, 202, 400));
        // registry.addEntity(new TestEntity(game, world, -332, 10));
        // registry.addEntity(new TestEntity(game, world, 300, 43));
        // registry.addEntity(new TestEntity(game, world, 232, 23));
        // registry.addEntity(new TestEntity(game, world, 111, 159));
        // registry.addEntity(new TestEntity(game, world, -400, 600));
        // registry.addEntity(new WorldEntity(game, world));

        // registry.getEntity(0).addComponent(new CameraComponent(1280, 720));
        // registry.getEntity(0).getComponent(TransformComponent.class).velocity.x += 5.f;

        Ship ship = new Ship(game, world, -64, 0, 0);
        ship.load("./saves/ships/test.ship");
        registry.addEntity(ship);

        Player p = new Player(game, world, -100, 0);
        p.getComponent(CameraComponent.class).active = true;
        p.getComponent(CameraComponent.class).camera.zoom = 1.f;
        registry.addEntity(p);
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
        
        EllipticalConic conic1 = new EllipticalConic(
            registry.getEntity(0),
            registry.getEntity(1),
            registry.getEntity(1).getComponent(BodyComponent.class).body.getPosition().cpy().sub(registry.getEntity(0).getComponent(BodyComponent.class).body.getPosition()),
            new Vector2(0, 180)
        );
        EllipticalConic conic2 = new EllipticalConic(
            registry.getEntity(1),
            registry.getEntity(2),
            registry.getEntity(2).getComponent(BodyComponent.class).body.getPosition().cpy().sub(registry.getEntity(1).getComponent(BodyComponent.class).body.getPosition()),
            new Vector2(0, 80)
        );
        Constants.DEBUG = false;
        conic1.draw(shapes, 1);
        conic2.draw(shapes, 1);
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
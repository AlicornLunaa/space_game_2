package com.alicornlunaa.spacegame.scenes.dev;

import com.alicornlunaa.selene_engine.components.BodyComponent;
import com.alicornlunaa.selene_engine.components.BoxColliderComponent;
import com.alicornlunaa.selene_engine.components.CameraComponent;
import com.alicornlunaa.selene_engine.components.IScriptComponent;
import com.alicornlunaa.selene_engine.components.SpriteComponent;
import com.alicornlunaa.selene_engine.components.TextureComponent;
import com.alicornlunaa.selene_engine.components.TransformComponent;
import com.alicornlunaa.selene_engine.components.SpriteComponent.AnchorPoint;
import com.alicornlunaa.selene_engine.core.BaseEntity;
import com.alicornlunaa.selene_engine.ecs.Registry;
import com.alicornlunaa.selene_engine.phys.PhysWorld;
import com.alicornlunaa.selene_engine.systems.CameraSystem;
import com.alicornlunaa.selene_engine.systems.PhysicsSystem;
import com.alicornlunaa.selene_engine.systems.RenderSystem;
import com.alicornlunaa.selene_engine.systems.ScriptSystem;
import com.alicornlunaa.spacegame.App;
import com.alicornlunaa.spacegame.objects.ship.Ship;
import com.alicornlunaa.spacegame.systems.CustomRenderSystem;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.Batch;
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
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

@SuppressWarnings("unused")
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

        public TestEntity(App game, PhysWorld world){
            BodyDef def = new BodyDef();
            def.type = BodyType.DynamicBody;

            addComponent(new TextureComponent(game.manager, "textures/dev_texture_32.png"));
            addComponent(new SpriteComponent(128, 128, AnchorPoint.CENTER));
            addComponent(new BodyComponent(world, def));
            addComponent(new BoxColliderComponent(getComponent(BodyComponent.class), 0.5f, 0.5f, 1.f));
            addComponent(new CameraComponent(1280, 720)).active = false;

            addComponent(new IScriptComponent() {
                TransformComponent tr = getComponent(TransformComponent.class);
                BodyComponent rb = getComponent(BodyComponent.class);

                @Override
                public void update(){
                    if(Gdx.input.isKeyPressed(Keys.W)){
                        rb.body.applyForceToCenter(0, 1.5f, true);
                    }
    
                    if(Gdx.input.isKeyPressed(Keys.S)){
                        rb.body.applyForceToCenter(0, -1.5f, true);
                    }
    
                    if(Gdx.input.isKeyPressed(Keys.A)){
                        rb.body.applyForceToCenter(-1.5f, 0, true);
                    }
    
                    if(Gdx.input.isKeyPressed(Keys.D)){
                        rb.body.applyForceToCenter(1.5f, 0, true);
                    }

                    if(Gdx.input.isKeyPressed(Keys.SPACE)){
                        rb.body.applyForceToCenter(0, 15.f, true);
                    }

                    if(Gdx.input.isKeyPressed(Keys.SHIFT_LEFT)){
                        tr.position.y += 0.4f;
                    }
                }
    
                @Override
                public void render(){}
            });
        }

    }

    private final App game;
    private Stage stage;
    private OrthographicCamera cam;
    
    private Registry registry;
    private PhysicsSystem simulation;
    private PhysWorld world;

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

        cam = (OrthographicCamera)stage.getCamera();
        cam.zoom = 0.5f;
        cam.position.set(0, 0, 0);
        cam.update();

        registry = new Registry();
        registry.registerSystem(new CameraSystem(game));
        simulation = registry.registerSystem(new PhysicsSystem());
        registry.registerSystem(new RenderSystem(game));
        registry.registerSystem(new CustomRenderSystem(game));
        registry.registerSystem(new ScriptSystem());

        world = new PhysWorld(128.0f);
        world.getBox2DWorld().setGravity(new Vector2(0, -0.5f));
        simulation.addWorld(world);

        registry.addEntity(new TestEntity(game, world));
        registry.addEntity(new WorldEntity(game, world));

        Ship ship = new Ship(game, world, -128, 128, 0);
        ship.load("./saves/ships/null.ship");
        registry.addEntity(ship);
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(0, 0, 0, 1);

        registry.update(delta);
        stage.act(delta);

        registry.render();
        stage.draw();

        game.debug.render(world.getBox2DWorld(), cam.combined.cpy().scl(world.getPhysScale()));
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
package com.alicornlunaa.spacegame.scenes.testing_scene;

import com.alicornlunaa.selene_engine.components_old.CircleColliderComponent;
import com.alicornlunaa.selene_engine.components_old.ScriptComponent;
import com.alicornlunaa.selene_engine.components_old.ShaderComponent;
import com.alicornlunaa.selene_engine.ecs.BodyComponent;
import com.alicornlunaa.selene_engine.ecs.CameraComponent;
import com.alicornlunaa.selene_engine.ecs.CameraSystem;
import com.alicornlunaa.selene_engine.ecs.PhysicsSystem;
import com.alicornlunaa.selene_engine.ecs.Registry;
import com.alicornlunaa.selene_engine.ecs.RenderSystem;
import com.alicornlunaa.selene_engine.ecs.SpriteComponent;
import com.alicornlunaa.selene_engine.ecs.TransformComponent;
import com.alicornlunaa.selene_engine.events.PhysicsEntityListener;
import com.alicornlunaa.selene_engine.phys.PhysWorld;
import com.alicornlunaa.selene_engine.systems.ScriptSystem;
import com.alicornlunaa.selene_engine.systems.ShapeRenderSystem;
import com.alicornlunaa.space_game.App;
import com.alicornlunaa.spacegame.components.GravityComponent;
import com.alicornlunaa.spacegame.components.PlanetSprite;
import com.alicornlunaa.spacegame.components.TrackedEntityComponent;
import com.alicornlunaa.spacegame.objects.simulation.Universe;
import com.alicornlunaa.spacegame.objects.simulation.cellular.CellBase;
import com.alicornlunaa.spacegame.objects.simulation.cellular.CellWorld;
import com.alicornlunaa.spacegame.objects.simulation.cellular.custom_cells.Gas;
import com.alicornlunaa.spacegame.objects.simulation.cellular.custom_cells.Sand;
import com.alicornlunaa.spacegame.objects.simulation.cellular.custom_cells.Steam;
import com.alicornlunaa.spacegame.objects.simulation.cellular.custom_cells.Water;
import com.alicornlunaa.spacegame.objects.simulation.cellular.custom_cells.Liquid;
import com.alicornlunaa.spacegame.objects.simulation.cellular.custom_cells.Oil;
import com.alicornlunaa.spacegame.systems.GravitySystem;
import com.alicornlunaa.spacegame.systems.SpaceRenderSystem;
import com.alicornlunaa.spacegame.systems.TrackingSystem;
import com.alicornlunaa.spacegame.util.Constants;
import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.Input.Buttons;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.utils.ScreenUtils;

@SuppressWarnings("unused")
public class TestScreen2 implements Screen {
    // Variables
    private InputMultiplexer inputs = new InputMultiplexer();
    private ShapeRenderer shapeRenderer = new ShapeRenderer();
    private Batch batch = new SpriteBatch();
    private Box2DDebugRenderer debugRenderer = new Box2DDebugRenderer();

    private Engine engine = new Engine();
    private PhysWorld world = new PhysWorld(128);

    // Private functions
    private Entity newPhysEntity(float x, float y){
        TransformComponent transform = new TransformComponent();
        transform.position.set(x, y);

        Entity entity = engine.createEntity();
        entity.add(transform);
        entity.add(new BodyComponent(world));
        engine.addEntity(entity);
        return entity;
    }

    // Constructor
    public TestScreen2(){
        engine.addSystem(new PhysicsSystem());
        engine.addSystem(new TestSystem(shapeRenderer));
        engine.addEntityListener(Family.all(BodyComponent.class).get(), new PhysicsEntityListener());
        newPhysEntity(5, 5);

        float width = 1280 / Constants.PPM;
        float height = 720 / Constants.PPM;

        App.instance.camera = new OrthographicCamera(width, height);
        App.instance.camera.position.set(width / 2.f, height / 2.f, 0);
        App.instance.camera.update();

        shapeRenderer.setAutoShapeType(true);

        inputs.addProcessor(new InputAdapter(){
            @Override
            public boolean keyTyped(char character) {
                if(character == 'r'){}

                return false;
            }

            @Override
            public boolean scrolled(float amountX, float amountY){
                if(Gdx.input.isKeyPressed(Keys.CONTROL_LEFT)){
                    if(App.instance.camera == null) return false;
                    float speed = Constants.MAP_VIEW_ZOOM_SENSITIVITY * App.instance.camera.zoom * amountY;
                    App.instance.camera.zoom = Math.min(Math.max(App.instance.camera.zoom + speed, 0.01f), 300000.0f);
                    return true;
                }

                return true;
            }
        });
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(0.02f, 0.02f, 0.02f, 1);
        
        Vector3 mouse = new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0.f);
        mouse.set(App.instance.camera.unproject(mouse));
        mouse.set((int)(mouse.x / Constants.TILE_SIZE), (int)(mouse.y / Constants.TILE_SIZE), 0.f);
        
        batch.setProjectionMatrix(App.instance.camera.combined);
        batch.setTransformMatrix(new Matrix4());
        batch.begin();
        

        batch.end();

        shapeRenderer.setProjectionMatrix(App.instance.camera.combined);
        shapeRenderer.setTransformMatrix(new Matrix4());
        shapeRenderer.begin();

        shapeRenderer.set(ShapeType.Line);
        shapeRenderer.setColor(Color.CYAN);
        


        shapeRenderer.end();


        debugRenderer.render(world.getBox2DWorld(), App.instance.camera.combined);

        // Controls
        if(Gdx.input.isButtonPressed(Buttons.LEFT)){
            
        } else if(Gdx.input.isButtonPressed(Buttons.RIGHT)){
            
        }

        // Updates
        engine.update(delta);
        world.update();
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
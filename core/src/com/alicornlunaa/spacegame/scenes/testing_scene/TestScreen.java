package com.alicornlunaa.spacegame.scenes.testing_scene;

import com.alicornlunaa.selene_engine.components.BodyComponent;
import com.alicornlunaa.selene_engine.components.CameraComponent;
import com.alicornlunaa.selene_engine.components.CircleColliderComponent;
import com.alicornlunaa.selene_engine.components.ScriptComponent;
import com.alicornlunaa.selene_engine.components.ShaderComponent;
import com.alicornlunaa.selene_engine.components.SpriteComponent;
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
import com.alicornlunaa.spacegame.components.GravityComponent;
import com.alicornlunaa.spacegame.components.PlanetSprite;
import com.alicornlunaa.spacegame.components.TrackedEntityComponent;
import com.alicornlunaa.spacegame.objects.simulation.Universe;
import com.alicornlunaa.spacegame.objects.simulation.cellular.CellBase;
import com.alicornlunaa.spacegame.objects.simulation.cellular.CellWorld;
import com.alicornlunaa.spacegame.systems.GravitySystem;
import com.alicornlunaa.spacegame.systems.SpaceRenderSystem;
import com.alicornlunaa.spacegame.systems.TrackingSystem;
import com.alicornlunaa.spacegame.util.Constants;
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
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.utils.ScreenUtils;

@SuppressWarnings("unused")
public class TestScreen implements Screen {
    // Variables
    private InputMultiplexer inputs = new InputMultiplexer();
    private CellWorld world = new CellWorld(7, 7);
    private ShapeRenderer shapeRenderer = new ShapeRenderer();
    private Batch batch = new SpriteBatch();

    private int brushSize = 1;

    // Constructor
    public TestScreen(){
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

                brushSize = Math.min(Math.max(brushSize - (int)Math.signum(amountY), 1), 20);
                return true;
            }
        });
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(0.02f, 0.02f, 0.02f, 1);
        
        App.instance.camera.position.set(
            world.width * Constants.CHUNK_SIZE * Constants.TILE_SIZE / 2.f,
            world.height * Constants.CHUNK_SIZE * Constants.TILE_SIZE / 2.f,
            0
        );
        App.instance.camera.update();
        
        Vector3 mouse = new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0.f);
        mouse.set(App.instance.camera.unproject(mouse));
        mouse.set((int)(mouse.x / Constants.TILE_SIZE) * Constants.TILE_SIZE, (int)(mouse.y / Constants.TILE_SIZE) * Constants.TILE_SIZE, 0.f);
        
        batch.setProjectionMatrix(App.instance.camera.combined);
        batch.setTransformMatrix(new Matrix4());
        batch.begin();
        world.draw(batch);
        batch.end();

        shapeRenderer.setProjectionMatrix(App.instance.camera.combined);
        shapeRenderer.setTransformMatrix(new Matrix4());
        shapeRenderer.begin();

        shapeRenderer.set(ShapeType.Line);
        shapeRenderer.setColor(Color.CYAN);
        shapeRenderer.rect(mouse.x, mouse.y, Constants.TILE_SIZE * brushSize, Constants.TILE_SIZE * brushSize);

        shapeRenderer.end();

        // Controls
        if(Gdx.input.isButtonJustPressed(Buttons.LEFT)){
            for(int i = (int)mouse.x; i < (int)(mouse.x + brushSize); i++){
                for(int k = (int)mouse.y; k < (int)(mouse.y + brushSize); k++){
                    world.setTile(i, k, new CellBase("stone"));
                }
            }
        }
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
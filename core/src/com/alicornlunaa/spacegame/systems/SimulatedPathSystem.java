package com.alicornlunaa.spacegame.systems;

import java.util.HashMap;

import com.alicornlunaa.selene_engine.components.BodyComponent;
import com.alicornlunaa.selene_engine.components.TransformComponent;
import com.alicornlunaa.selene_engine.core.IEntity;
import com.alicornlunaa.selene_engine.ecs.ISystem;
import com.alicornlunaa.spacegame.App;
import com.alicornlunaa.spacegame.objects.simulation.Celestial;
import com.alicornlunaa.spacegame.objects.simulation.Universe;
import com.alicornlunaa.spacegame.scripts.GravityScript;
import com.alicornlunaa.spacegame.util.Constants;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.utils.Array;

public class SimulatedPathSystem implements ISystem {

    public static final int MAX_STEPS = 20000;

    private App game;
    private Universe universe;
    private ShapeRenderer shapeRenderer;
    private HashMap<IEntity, Array<Vector2>> paths = new HashMap<>();

    public SimulatedPathSystem(App game, Universe universe){
        this.game = game;
        this.universe = universe;
        this.shapeRenderer = new ShapeRenderer();
    }

    public void simulate(IEntity entity){
        Array<Vector2> arr = new Array<>();
        
        TransformComponent transform = entity.getComponent(TransformComponent.class);
        BodyComponent bodyComponent = entity.getComponent(BodyComponent.class);

        Vector2 pos = transform.position.cpy();
        Vector2 vel = bodyComponent.body.getLinearVelocity().cpy();

        Celestial parent = universe.getParentCelestial(entity);
        if(parent == null) return;

        Body a = bodyComponent.body;
        Body b = parent.getComponent(BodyComponent.class).body;

        for(int i = 0; i < MAX_STEPS; i++){
            float m1 = a.getMass();
            float m2 = b.getMass();
            float r = b.getPosition().dst(a.getPosition());
            Vector2 direction = b.getPosition().cpy().sub(a.getPosition()).cpy().nor();
            vel.add(direction.scl(Constants.GRAVITY_CONSTANT * (m1 * m2) / (r * r)).scl(1.f / m1));
            
            arr.add(pos.cpy());
            arr.add(pos.cpy().add(vel));
            pos.add(vel);
        }

        paths.put(entity, arr);
    }

    @Override
    public void beforeUpdate() {}

    @Override
    public void afterUpdate() {}

    @Override
    public void update(IEntity entity) {
        simulate(entity);
    }

    @Override
    public void beforeRender() {
        shapeRenderer.setProjectionMatrix(game.camera.combined);
        shapeRenderer.begin(ShapeType.Filled);
    }

    @Override
    public void afterRender() {
        shapeRenderer.end();
    }

    @Override
    public void render(IEntity entity) {
        Array<Vector2> path = paths.get(entity);

        for(int i = 0; i < path.size; i += 2){
            shapeRenderer.setColor(Color.CYAN);
            shapeRenderer.rectLine(path.get(i), path.get(i + 1), 2000);
        }
    }

    @Override
    public boolean shouldRunOnEntity(IEntity entity) {
        return entity.hasComponent(GravityScript.class);
    }
    
}

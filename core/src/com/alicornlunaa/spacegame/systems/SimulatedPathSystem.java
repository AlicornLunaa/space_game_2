package com.alicornlunaa.spacegame.systems;

import java.util.HashMap;

import com.alicornlunaa.selene_engine.components.BodyComponent;
import com.alicornlunaa.selene_engine.components.TransformComponent;
import com.alicornlunaa.selene_engine.core.IEntity;
import com.alicornlunaa.selene_engine.ecs.ISystem;
import com.alicornlunaa.spacegame.App;
import com.alicornlunaa.spacegame.components.SimulatedPathScript;
import com.alicornlunaa.spacegame.objects.simulation.Celestial;
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
    private ShapeRenderer shapeRenderer;
    private HashMap<IEntity, Array<Vector2>> paths = new HashMap<>();

    public SimulatedPathSystem(App game){
        this.game = game;
        this.shapeRenderer = new ShapeRenderer();
    }

    public void simulate(IEntity entity){
        Array<Vector2> arr = new Array<>();
        
        TransformComponent transform = entity.getComponent(TransformComponent.class);
        BodyComponent bodyComponent = entity.getComponent(BodyComponent.class);

        Vector2 pos = transform.position.cpy();
        Vector2 vel = transform.velocity.cpy();

        for(int i = 0; i < MAX_STEPS; i++){
            for(Celestial other : game.gameScene.universe.getCelestials()){
                if(other == entity) continue;

                Body a = bodyComponent.body;
                Body b = other.getComponent(BodyComponent.class).body;
                Vector2 aPos = pos.cpy().scl(1 / Constants.PPM);
                Vector2 bPos = other.getComponent(TransformComponent.class).position.cpy().scl(1 / Constants.PPM);

                float m1 = a.getMass();
                float m2 = b.getMass();
                float r = bPos.dst(aPos);
                Vector2 direction = bPos.cpy().sub(aPos).cpy().nor();

                vel.add(direction.scl(Constants.GRAVITY_CONSTANT * (m1 * m2) / (r * r)));
            }
            
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
        return entity.hasComponent(SimulatedPathScript.class);
    }
    
}

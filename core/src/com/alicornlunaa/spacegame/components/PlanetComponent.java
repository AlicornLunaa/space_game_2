package com.alicornlunaa.spacegame.components;

import java.util.Stack;

import com.alicornlunaa.selene_engine.components.BodyComponent;
import com.alicornlunaa.selene_engine.components.ScriptComponent;
import com.alicornlunaa.selene_engine.components.ShaderComponent;
import com.alicornlunaa.selene_engine.components.TransformComponent;
import com.alicornlunaa.selene_engine.core.BaseEntity;
import com.alicornlunaa.selene_engine.core.IEntity;
import com.alicornlunaa.selene_engine.phys.PhysWorld;
import com.alicornlunaa.spacegame.App;
import com.alicornlunaa.spacegame.objects.simulation.Planet;
import com.alicornlunaa.spacegame.objects.world.ChunkManager;
import com.alicornlunaa.spacegame.objects.world.TerrainGenerator;
import com.alicornlunaa.spacegame.phys.PlanetaryPhysWorld;
import com.alicornlunaa.spacegame.util.Constants;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;

public class PlanetComponent extends ScriptComponent {
    // Variables
    private TransformComponent transform = getEntity().getComponent(TransformComponent.class);
    private BodyComponent bodyComponent = getEntity().getComponent(BodyComponent.class);
    private CelestialComponent celestialComponent = getEntity().getComponent(CelestialComponent.class);
    private ShaderComponent cartesianAtmosShader = getEntity().getComponents(ShaderComponent.class)[2];

    public int chunkWidth;
    public int chunkHeight;
    public float terrainRadius;
    public float atmosphereRadius;
    public float atmosphereDensity = 1.0f;
    public long terrainSeed = 123;
    public Vector3 starDirection = new Vector3(1.f, 0.f, 0.f);
    public PhysWorld physWorld;
    public ChunkManager chunkManager;
    
    private TerrainGenerator generator;
    private Array<Color> atmosComposition = new Array<>();
    private Array<Float> atmosPercentages = new Array<>();
    
    private Array<IEntity> entitiesOnPlanet = new Array<>();
    private Stack<IEntity> entitiesLeavingPlanet = new Stack<>();

    // Constructor
    public PlanetComponent(Planet entity) {
        super(entity);
    }

    // Functions
    public void addEntityWorld(IEntity e){
        // Formula: x = theta, y = radius
        TransformComponent entityTransform = e.getComponent(TransformComponent.class);
        BodyComponent entityBodyComponent = e.getComponent(BodyComponent.class);
        if(entityBodyComponent == null || entityTransform == null) return;

        // Convert orbital position to world
        Vector2 localPos = entityTransform.position.cpy().sub(transform.position);
        float x = (float)((localPos.angleRad() / Math.PI / -2.0) * (chunkWidth * Constants.CHUNK_SIZE * Constants.TILE_SIZE));
        float y = (localPos.len() / atmosphereRadius) * (chunkHeight * Constants.CHUNK_SIZE * Constants.TILE_SIZE);
        entityTransform.position.set(x, y);
        entityTransform.rotation -= (localPos.angleRad() + (float)Math.PI / 2);
        entityBodyComponent.sync(entityTransform);

        // Convert orbital velocity to world
        Vector2 vel = entityBodyComponent.body.getLinearVelocity().cpy();
        Vector2 tangent = localPos.cpy().nor().rotateDeg(-90);
        float velToPlanet = vel.dot(localPos.cpy().nor());
        float tangentVel = vel.dot(tangent);
        entityBodyComponent.body.setLinearVelocity(tangentVel, velToPlanet);

        // Add body
        entityBodyComponent.setWorld(physWorld);
        entitiesOnPlanet.add(e);
    }

    public void delEntityWorld(IEntity e){
        // Formula: theta = x, radius = y
        TransformComponent entityTransform = e.getComponent(TransformComponent.class);
        BodyComponent entityBodyComponent = e.getComponent(BodyComponent.class);
        if(entityBodyComponent == null || entityTransform == null) return;

        // Convert to space angles, spaceAngle = worldAngle + theta
        float theta = (float)((entityTransform.position.x / (chunkWidth * Constants.CHUNK_SIZE * Constants.TILE_SIZE)) * Math.PI * -2);
        float radius = (entityTransform.position.y / chunkHeight / Constants.CHUNK_SIZE / Constants.TILE_SIZE) * atmosphereRadius;
        float x = (float)(Math.cos(theta) * radius) + transform.position.x;
        float y = (float)(Math.sin(theta) * radius) + transform.position.y;
        entityTransform.position.set(x, y);
        entityTransform.rotation += (theta - (float)Math.PI / 2);
        entityBodyComponent.sync(entityTransform);

        // Convert to space velocity, tangent = x, planetToEntity = y
        Vector2 tangent = new Vector2(0, -1).rotateRad(theta);
        Vector2 planetToEnt = entityTransform.position.cpy().sub(transform.position).nor();
        Vector2 curVelocity = entityBodyComponent.body.getLinearVelocity().cpy();
        entityBodyComponent.body.setLinearVelocity(tangent.scl(curVelocity.x).add(planetToEnt.scl(curVelocity.y)));

        // Remove body
        entityBodyComponent.setWorld(bodyComponent.world);
        entitiesOnPlanet.removeValue(e, true);
    }

    public boolean checkEnterPlanet(IEntity e){
        // This function checks if the entity supplied
        // is within range to change its physics system to the planet's
        TransformComponent transform = e.getComponent(TransformComponent.class);

        if(transform.position.len() < celestialComponent.radius * 1.2f){
            // Move it into this world
            addEntityWorld(e);
            return true;
        }

        return false;
    }

    public boolean checkLeavePlanet(BaseEntity e){
        // This function checks if the entity supplied
        TransformComponent transform = e.getComponent(TransformComponent.class);

        // is far enough to leave the planet's physics world
        if(transform.position.y > celestialComponent.radius * 1.3f){
            // Move it into this world
            entitiesLeavingPlanet.push(e);
            return true;
        }

        return false;
    }

    public boolean isOnPlanet(IEntity e){
        return entitiesOnPlanet.contains(e, true);
    }

    public Color getAtmosphereColor(){
        float r = 0.f;
        float g = 0.f;
        float b = 0.f;
        float a = 0.f;

        for(int i = 0; i < atmosComposition.size; i++){
            Color c = atmosComposition.get(i).cpy();
            c.mul(atmosPercentages.get(i));

            r += c.r;
            g += c.g;
            b += c.b;
            a += c.a;
        }

        r /= atmosComposition.size;
        g /= atmosComposition.size;
        b /= atmosComposition.size;
        a /= atmosComposition.size;

        return new Color(r, g, b, a);
    }

    public ShaderComponent getCartesianShaderComponent(){
        return cartesianAtmosShader;
    }

    // Script functions
    @Override
    public void start() {
        generator = new TerrainGenerator(this);

        atmosComposition.add(Color.CYAN);
        atmosPercentages.add(1.f);
        
        physWorld = App.instance.gameScene.simulation.addWorld(new PlanetaryPhysWorld((Planet)getEntity(), Constants.PPM));
        chunkManager = new ChunkManager(generator, physWorld, chunkWidth, chunkHeight);
    }

    @Override
    public void update() {
        // starDirection.set(OrbitUtils.directionToNearestStar(game.gameScene.universe, Planet.this), 0);
        starDirection.set(1, 0, 0);

        // Remove entities in the world still
        // while(entitiesLeavingPlanet.size() > 0){
        //     delEntityWorld(entitiesLeavingPlanet.pop());
        // }

        // chunkManager.act(Gdx.graphics.getDeltaTime());
        chunkManager.update();
    }

    @Override
    public void render() {
    }
}

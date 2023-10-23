package com.alicornlunaa.spacegame.components;

import java.util.Stack;

import com.alicornlunaa.selene_engine.components.BodyComponent;
import com.alicornlunaa.selene_engine.components.ScriptComponent;
import com.alicornlunaa.selene_engine.components.ShaderComponent;
import com.alicornlunaa.selene_engine.components.TransformComponent;
import com.alicornlunaa.selene_engine.core.IEntity;
import com.alicornlunaa.selene_engine.ecs.Registry;
import com.alicornlunaa.selene_engine.phys.PhysWorld;
import com.alicornlunaa.spacegame.App;
import com.alicornlunaa.spacegame.objects.blocks.BaseTile;
import com.alicornlunaa.spacegame.objects.simulation.Planet;
import com.alicornlunaa.spacegame.objects.world.ChunkManager;
import com.alicornlunaa.spacegame.objects.world.TerrainGenerator;
import com.alicornlunaa.spacegame.phys.PlanetaryPhysWorld;
import com.alicornlunaa.spacegame.util.Constants;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;

public class PlanetComponent extends ScriptComponent {
    // Variables
    private TransformComponent transform = getEntity().getComponent(TransformComponent.class);
    private BodyComponent bodyComponent = getEntity().getComponent(BodyComponent.class);
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
    private Stack<IEntity> entitiesLeavingPlanet = new Stack<>();

    // Constructor
    public PlanetComponent(Registry registry, Planet entity, float terraRadius, float atmosRadius, float atmosDensity, int internalChunkHeight) {
        super(entity);
        
        // chunkHeight = (int)Math.floor(terraRadius / Tile.TILE_SIZE / Constants.CHUNK_SIZE);
        chunkHeight = 10;
        chunkWidth = (int)(2.0 * Math.PI * chunkHeight);
        terrainRadius = terraRadius;
        atmosphereRadius = atmosRadius;
        atmosphereDensity = atmosDensity;
        
        generator = new TerrainGenerator(this);

        atmosComposition.add(Color.CYAN);
        atmosPercentages.add(1.f);
        
        physWorld = App.instance.gameScene.simulation.addWorld(new PlanetaryPhysWorld((Planet)getEntity(), Constants.PPM));
        chunkManager = new ChunkManager(registry, physWorld, chunkWidth, chunkHeight, generator);
    }

    // Functions
    public TransformComponent convertToGlobalTransform(IEntity e){
        // Formula: x = theta, y = radius
        TransformComponent entityTransform = e.getComponent(TransformComponent.class);
        BodyComponent entityBodyComponent = e.getComponent(BodyComponent.class);
        TransformComponent convertedTransform = new TransformComponent();

        if(entityBodyComponent == null || entityTransform == null) return convertedTransform;

        // Convert orbital position to world
        float theta = (float)((entityTransform.position.x / (chunkWidth * Constants.CHUNK_SIZE * Constants.TILE_SIZE)) * Math.PI * -2);
        float radius = (entityTransform.position.y / chunkHeight / Constants.CHUNK_SIZE / Constants.TILE_SIZE) * atmosphereRadius;
        float x = (float)(Math.cos(theta) * radius) + transform.position.x;
        float y = (float)(Math.sin(theta) * radius) + transform.position.y;
        convertedTransform.position.set(x, y);
        convertedTransform.rotation = entityTransform.rotation + (theta - (float)Math.PI / 2);

        return convertedTransform;
    }

    public Vector2 convertToLocalForce(Vector2 position, Vector2 force){
        // Convert orbital velocity to world
        Vector2 toPlanetDirection = transform.position.cpy().sub(position).nor();
        Vector2 tangentPlanetDirection = toPlanetDirection.cpy().rotateDeg(-90);
        float velToPlanet = force.dot(toPlanetDirection);
        float tangentVel = force.dot(tangentPlanetDirection);
        return new Vector2(-tangentVel, -velToPlanet);
    }

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
    }

    public boolean checkEnterPlanet(IEntity e){
        // This function checks if the entity supplied
        // is within range to change its physics system to the planet's
        TransformComponent entityTransform = e.getComponent(TransformComponent.class);

        if(((entityTransform.position.cpy().sub(transform.position).len() / atmosphereRadius) * (chunkHeight * Constants.CHUNK_SIZE * Constants.TILE_SIZE)) < atmosphereRadius * 1.2f){
            // Move it into this world
            addEntityWorld(e);
            return true;
        }

        return false;
    }

    public boolean checkLeavePlanet(IEntity e){
        // This function checks if the entity supplied if its outside of the leaving radius
        TransformComponent entityTransform = e.getComponent(TransformComponent.class);

        // is far enough to leave the planet's physics world
        if((entityTransform.position.y / chunkHeight / Constants.CHUNK_SIZE / Constants.TILE_SIZE) * atmosphereRadius > atmosphereRadius * 1.3f){
            // Move it into this world
            entitiesLeavingPlanet.push(e);
            return true;
        }

        return false;
    }

    public boolean isOnPlanet(IEntity entity){
        BodyComponent bodyComponent = entity.getComponent(BodyComponent.class);

        if(bodyComponent != null)
            return (bodyComponent.world == physWorld);

        return false;
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
    }

    @Override
    public void update() {
        // starDirection.set(OrbitUtils.directionToNearestStar(game.gameScene.universe, Planet.this), 0);
        starDirection.set(1, 0, 0);
        chunkManager.update();

        //! TODO: DEBUG REMOVE THIS
        if(Gdx.input.isKeyJustPressed(Keys.F9)){
            chunkManager.reset();
            chunkManager.update();
        }

        if(Gdx.input.isKeyPressed(Keys.C)){
            Vector3 mouse = new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0.f);
            mouse.set(App.instance.camera.unproject(mouse));
            mouse.set((int)(mouse.x / Constants.TILE_SIZE), (int)(mouse.y / Constants.TILE_SIZE), 0.f);
            chunkManager.setTile(new BaseTile("dirt", (int)mouse.x, (int)mouse.y), (int)mouse.x, (int)mouse.y);
        } else if(Gdx.input.isKeyPressed(Keys.F)){
            Vector3 mouse = new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0.f);
            mouse.set(App.instance.camera.unproject(mouse));
            mouse.set((int)(mouse.x / Constants.TILE_SIZE), (int)(mouse.y / Constants.TILE_SIZE), 0.f);
            chunkManager.setTile(null, (int)mouse.x, (int)mouse.y);
        }

        // Remove entities in the world still
        while(entitiesLeavingPlanet.size() > 0){
            delEntityWorld(entitiesLeavingPlanet.pop());
        }
    }

    @Override
    public void render() {
    }
}

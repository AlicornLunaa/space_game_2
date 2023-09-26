package com.alicornlunaa.spacegame.components;

import java.util.Stack;

import com.alicornlunaa.selene_engine.components.BodyComponent;
import com.alicornlunaa.selene_engine.components.ScriptComponent;
import com.alicornlunaa.selene_engine.components.TransformComponent;
import com.alicornlunaa.selene_engine.core.BaseEntity;
import com.alicornlunaa.selene_engine.core.IEntity;
import com.alicornlunaa.selene_engine.phys.PhysWorld;
import com.alicornlunaa.spacegame.App;
import com.alicornlunaa.spacegame.objects.blocks.Tile;
import com.alicornlunaa.spacegame.objects.planet.Planet;
import com.alicornlunaa.spacegame.objects.planet.TerrainGenerator;
import com.alicornlunaa.spacegame.objects.planet.WorldBody;
import com.alicornlunaa.spacegame.phys.PlanetaryPhysWorld;
import com.alicornlunaa.spacegame.util.Constants;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;

public class PlanetComponent extends ScriptComponent {
    // Variables
    private TransformComponent transform = getEntity().getComponent(TransformComponent.class);
    private BodyComponent bodyComponent = getEntity().getComponent(BodyComponent.class);
    private CelestialComponent celestialComponent = getEntity().getComponent(CelestialComponent.class);

    public int chunkWidth;
    public int chunkHeight;
    public float atmosphereRadius;
    public float atmosphereDensity = 1.0f;
    public long terrainSeed = 123;
    public Vector3 starDirection = new Vector3(1.f, 0.f, 0.f);
    public PhysWorld physWorld;
    public WorldBody worldBody;
    
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
    public Vector2 getSpacePosition(IEntity e){
        // Convert the planetary coords to space coords
        TransformComponent entityTransform = e.getComponent(TransformComponent.class);
        float x = 0;
        float y = 0;

        if(entityTransform != null){
            // Convet to space position
            double theta = ((entityTransform.position.x / (chunkWidth * Constants.CHUNK_SIZE * Tile.TILE_SIZE)) * Math.PI * 2);
            float radius = entityTransform.position.y;
            x = (float)(Math.cos(theta) * radius);
            y = (float)(Math.sin(theta) * radius);
        }

        return new Vector2(x, y);
    }

    public Vector2 getSpaceVelocity(IEntity e){
        // Convert the planetary coords to space coords
        TransformComponent entityTransform = e.getComponent(TransformComponent.class);
        BodyComponent entityBody = e.getComponent(BodyComponent.class);
        Vector2 velocity = new Vector2();

        // Convet to space velocity
        if(entityTransform != null){
            double theta = ((transform.position.x / (chunkWidth * Constants.CHUNK_SIZE * Tile.TILE_SIZE)) * Math.PI * 2);

            Vector2 tangentDirection = new Vector2(0, 1).rotateRad((float)theta);
            Vector2 radialDirection = entityTransform.position.cpy().nor();
            Vector2 currentVelocity = entityBody.body.getLinearVelocity().cpy();

            velocity.set(tangentDirection.scl(currentVelocity.x).add(radialDirection.scl(currentVelocity.y)));
        }

        return velocity;
    }

    public void addEntityWorld(IEntity e){
        // Formula: x = theta, y = radius
        // Convert orbital position to world
        Vector2 localPos = transform.position.cpy();
        float ppm = bodyComponent.world.getPhysScale();
        float x = (float)((localPos.angleRad() / Math.PI / 2.0) * (chunkWidth * Constants.CHUNK_SIZE * Tile.TILE_SIZE));
        float y = localPos.len();
        bodyComponent.body.setTransform(x / ppm, y / ppm, bodyComponent.body.getAngle() - localPos.angleRad() + (float)Math.PI / 2);

        // Convert orbital velocity to world
        Vector2 vel = bodyComponent.body.getLinearVelocity().cpy().scl(ppm).scl(1 / Constants.PLANET_PPM);
        Vector2 tangent = localPos.cpy().nor().rotateDeg(90);
        float velToPlanet = vel.dot(localPos.cpy().nor());
        float tangentVel = vel.dot(tangent);
        bodyComponent.body.setLinearVelocity(tangentVel, -1 * Math.abs(velToPlanet));

        // Add body
        bodyComponent.setWorld(physWorld);
        transform.sync(bodyComponent);
        entitiesOnPlanet.add(e);
    }

    public void delEntityWorld(IEntity e){
        // Formula: theta = x, radius = y
        TransformComponent transform = e.getComponent(TransformComponent.class);
        BodyComponent bodyComponent = e.getComponent(BodyComponent.class);
        if(bodyComponent == null || transform == null) return;

        // Convert to space angles, spaceAngle = worldAngle + theta
        float ppm = bodyComponent.world.getPhysScale();
        float theta = ((transform.position.x / (chunkWidth * Constants.CHUNK_SIZE * Tile.TILE_SIZE)) * (float)Math.PI * 2);
        float radius = transform.position.y;
        float x = (float)(Math.cos(theta) * radius);
        float y = (float)(Math.sin(theta) * radius);
        bodyComponent.body.setTransform(x / ppm, y / ppm, bodyComponent.body.getAngle() + theta - (float)Math.PI / 2);

        // Convert to space velocity, tangent = x, planetToEntity = y
        Vector2 tangent = new Vector2(0, 1).rotateRad((float)theta);
        Vector2 planetToEnt = transform.position.cpy().nor();
        Vector2 curVelocity = bodyComponent.body.getLinearVelocity().cpy().scl(ppm).scl(1 / Constants.PPM);
        bodyComponent.body.setLinearVelocity(tangent.scl(curVelocity.x).add(planetToEnt.scl(curVelocity.y)));

        // Remove body
        bodyComponent.setWorld(bodyComponent.world);
        transform.sync(bodyComponent);
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

    // Script functions
    @Override
    public void start() {
        generator = new TerrainGenerator(
            chunkWidth * Constants.CHUNK_SIZE / 20,
            chunkHeight * Constants.CHUNK_SIZE / 20,
            terrainSeed
        );

        atmosComposition.add(Color.CYAN);
        atmosPercentages.add(1.f);
        
        physWorld = App.instance.gameScene.simulation.addWorld(new PlanetaryPhysWorld((Planet)getEntity(), Constants.PLANET_PPM));
        worldBody = new WorldBody(App.instance, physWorld, chunkWidth, (int)(atmosphereRadius / Constants.CHUNK_SIZE / Tile.TILE_SIZE) + 1);
    }

    @Override
    public void update() {
        // starDirection.set(OrbitUtils.directionToNearestStar(game.gameScene.universe, Planet.this), 0);
        starDirection.set(1, 0, 0);

        // Remove entities in the world still
        while(entitiesLeavingPlanet.size() > 0){
            delEntityWorld(entitiesLeavingPlanet.pop());
        }

        worldBody.act(Gdx.graphics.getDeltaTime());
        worldBody.update();
    }

    @Override
    public void render() {
    }
}

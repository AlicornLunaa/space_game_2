package com.alicornlunaa.spacegame.objects.Planet;

import java.util.ArrayList;
import java.util.HashMap;

import com.alicornlunaa.spacegame.App;
import com.alicornlunaa.spacegame.objects.Entity;
import com.alicornlunaa.spacegame.util.Constants;
import com.alicornlunaa.spacegame.util.OpenSimplexNoise;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;

/**
 * The World object will hold the data for the world's tiles
 * as well as the how to render the circular planet in space.
 */
public class Planet extends Entity {

    // Variables
    private final App game;
    private final World spaceWorld;
    private final OpenSimplexNoise noise;

    @SuppressWarnings("unused")
    private final Box2DDebugRenderer debug = new Box2DDebugRenderer();

    // Planet variables
    private float radius = 2500.0f;
    private float atmosRadius = 6500.0f;
    private float atmosDensity = 1.0f;
    private long seed = 123;

    // World variables
    private World planetWorld;
    private float physAccumulator;
    private HashMap<Vector2, Chunk> map = new HashMap<>();
    private TerrainGenerator generator;
    private ArrayList<Entity> planetEnts = new ArrayList<>(); // Entities on the rectangular planet

    // Space variables
    private CircleShape shape = new CircleShape();
    private Pixmap pixmap;
    private Texture terrainTexture;
    private TextureRegionDrawable terrainSprite;
    private Texture atmosTexture;
    private TextureRegionDrawable atmosSprite;

    // Private functions
    private void generateTerrainSprite(){
        pixmap = new Pixmap(Math.min((int)radius * 2, 2000), Math.min((int)radius * 2, 2000), Format.RGBA8888);

        for(int x = 0; x < pixmap.getWidth(); x++){
            for(int y = 0; y < pixmap.getHeight(); y++){
                int mX = x - pixmap.getWidth() / 2;
                int mY = y - pixmap.getHeight() / 2;
                int radSqr = mX * mX + mY * mY;
                float rand = (float)noise.eval(mX * 0.1f, mY * 0.1f);
                
                Color c = new Color(0.72f, 0.7f, 0.9f, 1);

                if(radSqr > (float)Math.pow(pixmap.getWidth() / 2, 2)) c.a = 0;
                if(rand < 0){ c.mul(0.9f); c.a *= 1.1; }

                pixmap.setColor(c);
                pixmap.drawPixel(x, y);
            }
        }

        terrainTexture = new Texture(pixmap);
        terrainSprite = new TextureRegionDrawable(terrainTexture);
        pixmap.dispose();
    }
    
    private void generateAtmosphereSprite(){
        pixmap = new Pixmap(Math.min((int)atmosRadius * 2, 2500), Math.min((int)atmosRadius * 2, 2500), Format.RGBA8888);

        for(int x = 0; x < pixmap.getWidth(); x++){
            for(int y = 0; y < pixmap.getHeight(); y++){
                int mX = x - pixmap.getWidth() / 2;
                int mY = y - pixmap.getHeight() / 2;
                int radSqr = mX * mX + mY * mY;

                // float planetRadSqr = stateRef.radius * stateRef.radius;
                // float atmosRadSqr = stateRef.atmosRadius * stateRef.atmosRadius;
                // float pixelRadSqr = mX * mX + mY * mY;
        
                // float atmosSurface = atmosRadSqr - planetRadSqr; // Atmosphere radius above surface
                // float pixelSurface = pixelRadSqr - planetRadSqr; // Pixel radius above surface
                
                Color c = new Color(0.6f, 0.6f, 0.9f, 0.2f);

                if(radSqr > (float)Math.pow(pixmap.getWidth() / 2, 2)) c.a = 0;

                pixmap.setColor(c);
                pixmap.drawPixel(x, y);
            }
        }

        atmosTexture = new Texture(pixmap);
        atmosSprite = new TextureRegionDrawable(atmosTexture);
        pixmap.dispose();
    }

    private void initializeWorld(){
        // Create new world for on the planet
        planetWorld = new World(new Vector2(), true);
        planetWorld.setGravity(new Vector2(0, -10 * (Constants.GRAVITY_CONSTANT * body.getMass()) / (radius * radius)));

        // Initial terrain
        int initialRad = 3;
        for(int x = -initialRad; x <= initialRad; x++){
            for(int y = -initialRad; y <= initialRad; y++){
                map.put(new Vector2(x, y), new Chunk(game, planetWorld, generator, x, y));
            }
        }
    }

    private void initializeSpace(){
        // Initialize space positions
        setSize(radius * 2, radius * 2);
        setOrigin(radius, radius);

        BodyDef def = new BodyDef();
        def.type = BodyType.DynamicBody;
        def.position.set(0, 0);
        setBody(spaceWorld.createBody(def));

        shape.setRadius(radius / getPhysScale());
        shape.setPosition(new Vector2(0, 0));
        body.createFixture(shape, 1.0f);

        // Create textures
        generateTerrainSprite();
        generateAtmosphereSprite();
    }

    // Constructor
    public Planet(final App game, final World world, float x, float y){
        super();
        this.game = game;
        this.spaceWorld = world;

        // Initialize generators
        noise = new OpenSimplexNoise(seed);
        generator = new TerrainGenerator(
            game,
            seed,
            (int)(2 * Math.PI * radius / Chunk.CHUNK_SIZE / Tile.TILE_SIZE),
            (int)(radius / Chunk.CHUNK_SIZE / Tile.TILE_SIZE),
            radius
        );
        
        // Initialize the two states
        initializeSpace();
        initializeWorld();

        // Positional
        setPosition(x, y);
    }

    // Functions
    public TerrainGenerator getGenerator(){ return generator; }
    public final World getPlanetWorld(){ return planetWorld; }
    public float getRadius(){ return radius; }
    public float getAtmosRadius(){ return atmosRadius; }
    public float getAtmosDensity(){ return atmosDensity; }
    public long getSeed(){ return seed; }

    /**
     * Converts the entity to 2d planet planar coordinates
     * @param e Entity to be converted
     */
    public void addEntity(Entity e){
        // Formula: x = theta, y = radius
        Vector2 localPos = e.getPosition().sub(getPosition());
        float worldWidthUnits = generator.getWidth() * Chunk.CHUNK_SIZE * Tile.TILE_SIZE;
        float x = (localPos.angleDeg() / 360 * worldWidthUnits);
        float y = localPos.len();
        e.setPosition(x, y);

        // Load space angle relative to the world
        float theta = localPos.angleDeg();
        float omega = e.getRotation();
        e.setRotation(omega - theta + 90);

        // Orbital velocity formula: v = sqrt((G * planetMass) / orbitRadius)
        Vector2 vel = e.getBody().getLinearVelocity().cpy().scl(getPhysScale()).scl(1 / Constants.PLANET_PPM);
        float velToPlanet = vel.dot(localPos.cpy().nor());
        float orbitRadius = localPos.len();
        float oldVel = e.getBody().getLinearVelocity().len();
        float tangentVel = (float)Math.sqrt((Constants.GRAVITY_CONSTANT * body.getMass()) / orbitRadius) * -(oldVel / Math.abs(oldVel + 0.00001f));
        e.getBody().setLinearVelocity(tangentVel, velToPlanet);

        // Add body
        e.loadBodyToWorld(planetWorld, Constants.PLANET_PPM);
        planetEnts.add(e);
    }

    /**
     * Convert the entity to the space planet circular coordinates
     * @param e Entity to be converted
     */
    public void delEntity(Entity e){
        // Formula: theta = x, radius = y
        float worldWidthUnits = generator.getWidth() * Chunk.CHUNK_SIZE * Tile.TILE_SIZE;
        double theta = ((e.getX() / worldWidthUnits) * Math.PI * 2);
        float radius = e.getY();

        // Convert to space angles, spaceAngle = worldAngle + theta - 90
        float worldAngle = e.getRotation();
        e.setRotation(worldAngle + (float)Math.toDegrees(theta) - 90);

        // Convet to space position
        float x = (float)(Math.cos(theta) * radius) + getX();
        float y = (float)(Math.sin(theta) * radius) + getY();
        e.setPosition(x, y);

        // Convert to space velocity, tangent = x, planetToEntity = y
        Vector2 tangent = new Vector2(1, 0).rotateRad((float)theta);
        Vector2 planetToEnt = e.getPosition().sub(this.getPosition()).nor();
        Vector2 curVelocity = e.getBody().getLinearVelocity().scl(e.getPhysScale()).scl(1 / Constants.PPM);
        e.getBody().setLinearVelocity(tangent.scl(curVelocity.x).add(planetToEnt.scl(curVelocity.y)));

        // Remove body
        e.loadBodyToWorld(game.gameScene.gamePanel.getWorld(), Constants.PPM);
        planetEnts.remove(e);
    }

    public ArrayList<Entity> getEntities(){ return planetEnts; }

    public void drawSpace(Batch batch, float parentAlpha){
        // Draws the circular world in space
        Matrix4 oldMatrix = batch.getTransformMatrix();
        batch.setTransformMatrix(new Matrix4().set(getTransform()));
        atmosSprite.draw(batch, getOriginX() - atmosRadius, getOriginY() - atmosRadius, atmosRadius * 2, atmosRadius * 2);
        terrainSprite.draw(batch, 0, 0, radius * 2, radius * 2);
        batch.setTransformMatrix(oldMatrix);
    }

    public void drawWorld(Batch batch, float parentAlpha){
        // Draws the flat planar world
        // Draw each chunk rotated around, theta = x, r = y
        for(Chunk chunk : map.values()){
            chunk.draw(batch);
        }

        // Draw every entity
        for(Entity e : planetEnts){
            e.draw(batch, parentAlpha);
        }

        // Debug rendering
        // debug.render(planetWorld, batch.getProjectionMatrix().cpy().scl(Constants.PLANET_PPM));
    }

    public void updateWorld(float delta){
        // Step the physics on the world
        physAccumulator += Math.min(delta, 0.25f);
        while(physAccumulator >= Constants.TIME_STEP){
            planetWorld.step(Constants.TIME_STEP, Constants.VELOCITY_ITERATIONS, Constants.POSITION_ITERATIONS);
            physAccumulator -= Constants.TIME_STEP;
        }
        
        // Constrain entities to the world
        float worldWidthPixels = generator.getWidth() * Chunk.CHUNK_SIZE * Tile.TILE_SIZE;

        for(Entity e : planetEnts){
            if(e.getX() > worldWidthPixels){
                e.setX(e.getX() - worldWidthPixels);
            } else if(e.getX() < 0){
                e.setX(e.getX() + worldWidthPixels);
            }
        }
    }

    @Override
    public void draw(Batch batch, float parentAlpha){ drawSpace(batch, parentAlpha); }

    @Override
    public boolean remove(){
        terrainTexture.dispose();
        atmosTexture.dispose();
        shape.dispose();
        return super.remove();
    }

    // Physics functions
    public void applyGravity(float delta, Body b){
        // Newtons gravitational law: F = (G(m1 * m2)) / r^2
        float orbitRadius = body.getPosition().dst(b.getPosition()); // Entity radius in physics scale
        Vector2 direction = body.getPosition().cpy().sub(b.getPosition()).nor();
        float force = (Constants.GRAVITY_CONSTANT * b.getMass() * body.getMass()) / (orbitRadius * orbitRadius);

        b.applyForceToCenter(direction.scl(force * delta), true);
    }

    public void applyDrag(float delta, Body b){
        // Newtons gravitational law: F = 1/2(density * velocity^2 * dragCoefficient * Area)
        float planetRadPhys = radius / physScale; // Planet radius in physics scale
        float atmosRadPhys = atmosRadius / physScale; // Atmosphere radius in physics scale
        float entRadPhys = body.getPosition().dst(b.getPosition()); // Entity radius in physics scale

        float atmosSurface = atmosRadPhys - planetRadPhys; // Atmosphere radius above surface
        float entSurface = entRadPhys - planetRadPhys; // Entity radius above surface
        float atmosDepth = Math.max(atmosSurface - entSurface, 0) / atmosSurface; // How far the entity is in the atmosphere, where zero is outside and 1 is submerged
        float density = atmosDensity * atmosDepth;

        Vector2 relVel = body.getLinearVelocity().cpy().sub(b.getLinearVelocity());
        Vector2 velDir = b.getLinearVelocity().cpy().nor();
        float velSqr = relVel.len2();
        float force = (1.0f / 2.0f) * (density * velSqr * Constants.DRAG_COEFFICIENT);

        b.applyForceToCenter(velDir.scl(-1 * force * delta), true);
    }

    public boolean checkTransfer(Entity e){
        // This function checks if the entity supplied
        // is within range to change its physics system to the planet's
        float dist = e.getPosition().dst(getPosition());

        if(dist < atmosRadius){
            // Move it into this world
            this.addEntity(e);
            game.setScreen(game.planetScene);

            return true;
        }

        return false;
    }

    // World functions
    public Chunk createChunk(int x, int y){
        Chunk c = new Chunk(game, planetWorld, generator, x, y);
        map.put(new Vector2(x, y), c);

        return c;
    }

    public Chunk getChunk(int x, int y){
        return map.get(new Vector2(x, y));
    }

    public Tile getTile(int x, int y){
        // Gets the tile from the world position
        return map.get(new Vector2(x / Chunk.CHUNK_SIZE, y / Chunk.CHUNK_SIZE)).getTile(x, y);
    }

    public HashMap<Vector2, Chunk> getMap(){
        return map;
    }
    
}

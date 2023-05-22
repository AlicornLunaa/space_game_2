package com.alicornlunaa.spacegame.objects.planet;

import java.util.Stack;

import com.alicornlunaa.selene_engine.components.BodyComponent;
import com.alicornlunaa.selene_engine.components.IScriptComponent;
import com.alicornlunaa.selene_engine.components.TransformComponent;
import com.alicornlunaa.selene_engine.core.BaseEntity;
import com.alicornlunaa.selene_engine.core.IEntity;
import com.alicornlunaa.selene_engine.phys.PhysWorld;
import com.alicornlunaa.spacegame.App;
import com.alicornlunaa.spacegame.components.CustomSpriteComponent;
import com.alicornlunaa.spacegame.objects.blocks.Tile;
import com.alicornlunaa.spacegame.objects.simulation.Celestial;
import com.alicornlunaa.spacegame.objects.simulation.orbits.OrbitUtils;
import com.alicornlunaa.spacegame.phys.PlanetaryPhysWorld;
import com.alicornlunaa.spacegame.util.Constants;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;

public class Planet extends Celestial {

    // Variables
    private final App game;

    private int terrestrialWidth; // Chunk-size
    private int terrestrialHeight; // Chunk-size
    private float atmosphereRadius;
    private float atmosphereDensity = 1.0f;
    private Array<Color> atmosComposition = new Array<>();
    private Array<Float> atmosPercentages = new Array<>();
    private long terrainSeed = 123;
    private TerrainGenerator generator;

    private Texture texture;
    private Texture debugTexture;
    private Texture surfaceRender;
    private int surfaceRenderResolution = 256;
    private ShaderProgram atmosShader;
    private ShaderProgram terraShader;

    private Vector3 starDirection = new Vector3(1.f, 0.f, 0.f);

    private PhysWorld physWorld;
    private Stack<BaseEntity> leavingEnts = new Stack<>();
    private WorldBody worldBlocks;

    // Private functions
    private void generateTexture(){
        Pixmap p = new Pixmap(1, 1, Format.RGBA8888);
        p.setColor(Color.WHITE);
        p.fill();
        texture = new Texture(p);
        p.dispose();
    }

    @SuppressWarnings("unused")
    private Vector3 cartesianToSpherical(Vector3 c){
        float theta = (float)Math.atan2(c.y, c.x);
        float phi = (float)Math.acos(c.z / c.len());
        float p = c.len();
        return new Vector3(p, theta, phi);
    }

    private Vector3 sphericalToCartesian(Vector3 c){
        float x = (float)(c.x * Math.sin(c.z) * Math.cos(c.y));
        float y = (float)(c.x * Math.sin(c.z) * Math.sin(c.y));
        float z = (float)(c.x * Math.cos(c.z));
        return new Vector3(x, y, z);
    }

    private Vector3 rectToSphere(Vector3 r){
        float theta = (float)(r.x * 2.0 * Math.PI);
        float radius = r.y;
        float phi = (float)(r.z * 2.0 * Math.PI);
        return new Vector3(radius, theta, phi);
    }

    @SuppressWarnings("unused")
    private Vector3 sphereToRect(Vector3 s){
        float x = (float)(s.y / (2.0 * Math.PI));
        float y = s.x;
        float z = (float)(s.z / (2.0 * Math.PI));
        return new Vector3(x, y, z);
    }

    private void generateSurface(){
        // Scan the point cloud data to create a height map
        Pixmap p = new Pixmap(surfaceRenderResolution, surfaceRenderResolution, Format.RGBA8888);
        Pixmap biomeMap = generator.getBiomeMap();

        p.setColor(Color.WHITE);
        p.fill();

        for(float z = 0; z < surfaceRenderResolution; z++){
            for(float x = 0; x < surfaceRenderResolution; x++){
                // Convert to spherical and grab closest
                Vector3 sphereCoord = rectToSphere(new Vector3(x, 1, z).scl(1.f / surfaceRenderResolution / (float)Math.PI, 1, 1.f / surfaceRenderResolution / (float)Math.PI));
                Vector3 coord = sphericalToCartesian(sphereCoord.add(0, 0, 0));
                coord.add(1, 1, 1);
                coord.scl(1 / 2.f);
                coord.scl(terrestrialWidth * Constants.CHUNK_SIZE);

                p.setColor(new Color(biomeMap.getPixel(biomeMap.getWidth() - (int)coord.x, biomeMap.getHeight() - (int)coord.z)));
                p.drawPixel((int)x, (int)z);
            }
        }

        surfaceRender = new Texture(p);
        p.dispose();
    }

    private void generatePhysWorld(){
        physWorld = game.simulation.addWorld(new PlanetaryPhysWorld(this, Constants.PLANET_PPM));

        // Create a world-body chunking system to take up the entirety of the planet
        worldBlocks = new WorldBody(game, physWorld, terrestrialWidth, (int)(getAtmosphereRadius() / Constants.CHUNK_SIZE / Tile.TILE_SIZE) + 1);
    }

    // Constructor
    public Planet(final App game, float x, float y, float terraRadius, float atmosRadius, float atmosDensity) {
        super(game, terraRadius);
        this.game = game;

        float ppm = bodyComponent.world.getPhysScale();
        bodyComponent.body.setTransform(x / ppm, y / ppm, bodyComponent.body.getAngle());

        terrestrialHeight = (int)Math.floor(radius / Tile.TILE_SIZE / Constants.CHUNK_SIZE);
        terrestrialWidth = (int)(2.0 * Math.PI * terrestrialHeight);
        atmosphereRadius = atmosRadius;
        atmosphereDensity = atmosDensity;
        generator = new TerrainGenerator(terrestrialWidth * Constants.CHUNK_SIZE / 2, terrestrialHeight * Constants.CHUNK_SIZE / 2, terrainSeed);

        atmosComposition.add(Color.CYAN);
        atmosPercentages.add(1.f);

        generateTexture();
        generateSurface();
        generatePhysWorld();
        addComponent(new IScriptComponent() {
            @Override
            public void update() {
                starDirection.set(OrbitUtils.directionToNearestStar(game.universe, Planet.this), 0);

                // Remove entities in the world still
                while(leavingEnts.size() > 0){
                    delEntityWorld(leavingEnts.pop());
                }

                worldBlocks.act(Gdx.graphics.getDeltaTime());
                worldBlocks.update();
            }

            @Override
            public void render() {} 
        });
        addComponent(new CustomSpriteComponent() {
            @Override
            public void render(Batch batch) {
                // Save rendering state
                Matrix4 proj = batch.getProjectionMatrix().cpy();
                Matrix4 trans = new Matrix4().set(getUniverseSpaceTransform());
                Matrix4 invProj = proj.cpy().inv();
                Vector2 worldPos = getUniverseSpaceTransform().getTranslation(new Vector2());
                
                // Shade the planet in
                batch.setTransformMatrix(trans);
                batch.setShader(terraShader);
                terraShader.setUniformMatrix("u_invCamTrans", invProj);
                terraShader.setUniformf("u_starDirection", starDirection);
                terraShader.setUniformf("u_planetWorldPos", worldPos);
                terraShader.setUniformf("u_planetRadius", getRadius());
                batch.draw(surfaceRender, radius * -1, radius * -1, radius * 2, radius * 2);

                batch.setProjectionMatrix(new Matrix4().setToOrtho2D(0, 0, 1280, 720));
                batch.setTransformMatrix(new Matrix4());

                batch.setShader(atmosShader);
                atmosShader.setUniformMatrix("u_invCamTrans", invProj);
                atmosShader.setUniformf("u_starDirection", starDirection);
                atmosShader.setUniformf("u_planetWorldPos", worldPos);
                atmosShader.setUniformf("u_planetRadius", getRadius());
                atmosShader.setUniformf("u_atmosRadius", getAtmosphereRadius());
                atmosShader.setUniformf("u_atmosColor", getAtmosphereColor());
                batch.draw(texture, 0, 0, 1280, 720);

                batch.setShader(null);
                batch.draw(debugTexture, 10, 10, 256, 256);
                batch.setProjectionMatrix(proj);
            }
        });

        debugTexture = new Texture(generator.getBiomeMap());

        atmosShader = game.manager.get("shaders/atmosphere", ShaderProgram.class);
        terraShader = game.manager.get("shaders/planet", ShaderProgram.class);
    }

    // Functions
    public float getAtmosphereRadius(){ return atmosphereRadius; }
    public float getAtmosphereDensity(){ return atmosphereDensity; }
    public Array<Color> getAtmosphereComposition(){ return atmosComposition; }
    public Array<Float> getAtmospherePercentages(){ return atmosPercentages; }
    public long getTerrainSeed(){ return terrainSeed; }
    public PhysWorld getInternalPhysWorld(){ return physWorld; }
    public WorldBody getWorldBody(){ return worldBlocks; }
    public int getTerrestrialWidth(){ return terrestrialWidth; }
    public int getTerrestrialHeight(){ return terrestrialHeight; }
    public Vector3 getStarDirection(){ return starDirection; }

    /**
     * Returns the position of the entity on the planet in terms of space coordinates
     * @param e
     * @return
     */
    public Vector2 getSpacePosition(IEntity e){
        // Convert the planetary coords to space coords
        TransformComponent entityTransform = e.getComponent(TransformComponent.class);
        float x = 0;
        float y = 0;

        if(entityTransform != null){
            // Convet to space position
            double theta = ((entityTransform.position.x / (getTerrestrialWidth() * Constants.CHUNK_SIZE * Tile.TILE_SIZE)) * Math.PI * 2);
            float radius = entityTransform.position.y;
            x = (float)(Math.cos(theta) * radius);
            y = (float)(Math.sin(theta) * radius);
        }

        return new Vector2(x, y);
    }

    /**
     * Returns the velocity of the entity on the planet in terms of space coordinates
     * @param e
     * @return
     */
    public Vector2 getSpaceVelocity(IEntity e){
        // Convert the planetary coords to space coords
        TransformComponent entityTransform = e.getComponent(TransformComponent.class);
        BodyComponent entityBodyComponent = e.getComponent(BodyComponent.class);
        Vector2 velocity = new Vector2();

        // Convet to space velocity
        if(entityTransform != null && entityBodyComponent != null){
            double theta = ((transform.position.x / (getTerrestrialWidth() * Constants.CHUNK_SIZE * Tile.TILE_SIZE)) * Math.PI * 2);

            Vector2 tangent = new Vector2(0, 1).rotateRad((float)theta);
            Vector2 planetToEnt = entityTransform.position.cpy().nor();
            Vector2 curVelocity = entityBodyComponent.body.getLinearVelocity().cpy().scl(bodyComponent.world.getPhysScale() / Constants.PPM);

            velocity.set(tangent.scl(curVelocity.x).add(planetToEnt.scl(curVelocity.y)));
        }

        return velocity;
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

    /**
     * Converts the entity to 2d planet planar coordinates
     * @param e Entity to be converted
     */
    public void addEntityWorld(IEntity e){
        // TODO: CHECK FOR ERRORS
        // Formula: x = theta, y = radius
        TransformComponent transform = e.getComponent(TransformComponent.class);
        BodyComponent bodyComponent = e.getComponent(BodyComponent.class);
        if(bodyComponent == null || transform == null) return;

        // Convert orbital position to world
        Vector2 localPos = transform.position.cpy();
        float ppm = bodyComponent.world.getPhysScale();
        float x = (float)((localPos.angleRad() / Math.PI / 2.0) * (terrestrialWidth * Constants.CHUNK_SIZE * Tile.TILE_SIZE));
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
    }

    /**
     * Convert the entity to the space planet circular coordinates
     * @param e Entity to be converted
     */
    public void delEntityWorld(BaseEntity e){
        // Formula: theta = x, radius = y
        // TODO: CHECK FOR ERRORS
        TransformComponent transform = e.getComponent(TransformComponent.class);
        BodyComponent bodyComponent = e.getComponent(BodyComponent.class);
        if(bodyComponent == null || transform == null) return;

        // Convert to space angles, spaceAngle = worldAngle + theta
        float ppm = bodyComponent.world.getPhysScale();
        float theta = ((transform.position.x / (terrestrialWidth * Constants.CHUNK_SIZE * Tile.TILE_SIZE)) * (float)Math.PI * 2);
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
        bodyComponent.setWorld(getInfluenceWorld());
    }

    public boolean checkTransferPlanet(IEntity e){
        // This function checks if the entity supplied
        // is within range to change its physics system to the planet's
        TransformComponent transform = e.getComponent(TransformComponent.class);
        float dist = transform.position.len();

        if(dist < radius * 1.2f){
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
        if(transform != null && transform.position.y > radius * 1.3f){
            // Move it into this world
            leavingEnts.push(e);
            return true;
        }

        return false;
    }

    public Vector2 applyDrag(BodyComponent bc){
        // Newtons gravitational law: F = 1/2(density * velocity^2 * dragCoefficient * Area)
        BodyComponent bodyComponent = this.getComponent(BodyComponent.class);
        if(bodyComponent == null || bc == null) return new Vector2();

        float ppm = bodyComponent.world.getPhysScale();
        float planetRadPhys = getRadius() / ppm; // Planet radius in physics scale
        float atmosRadPhys = getAtmosphereRadius() / ppm; // Atmosphere radius in physics scale
        float entRadPhys = bc.body.getPosition().len(); // Entity radius in physics scale

        float atmosSurface = atmosRadPhys - planetRadPhys; // Atmosphere radius above surface
        float entSurface = entRadPhys - planetRadPhys; // Entity radius above surface
        float atmosDepth = Math.max(atmosSurface - entSurface, 0) / atmosSurface; // How far the entity is in the atmosphere, where zero is outside and 1 is submerged
        float density = atmosphereDensity * atmosDepth * 0.001f;

        Vector2 relVel = bodyComponent.body.getLinearVelocity().cpy().sub(bc.body.getLinearVelocity());
        Vector2 velDir = bc.body.getLinearVelocity().cpy().nor();
        float velSqr = relVel.len2();
        float force = (1.0f / 2.0f) * (density * velSqr * Constants.DRAG_COEFFICIENT);

        return velDir.scl(-1 * force);
    }

    @Override
    public Vector2 applyPhysics(float delta, IEntity e){
        checkTransferPlanet(e);
        return super.applyPhysics(delta, e).add(applyDrag(e.getComponent(BodyComponent.class)));
    }

    @Override
    public void dispose(){
        texture.dispose();
    }

}

package com.alicornlunaa.spacegame.objects.Planet;

import java.util.ArrayList;
import java.util.Stack;

import com.alicornlunaa.spacegame.App;
import com.alicornlunaa.spacegame.objects.Entity;
import com.alicornlunaa.spacegame.objects.Player;
import com.alicornlunaa.spacegame.objects.Blocks.Tile;
import com.alicornlunaa.spacegame.objects.Ship.Ship;
import com.alicornlunaa.spacegame.objects.Simulation.Celestial;
import com.alicornlunaa.spacegame.phys.PhysWorld;
import com.alicornlunaa.spacegame.scenes.PlanetScene.PlanetScene;
import com.alicornlunaa.spacegame.util.Constants;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.utils.Array;

public class Planet extends Celestial {

    // Variables
    private final App game;

    private float terrestrialWidth;
    private float terrestrialHeight;
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
    private ArrayList<Entity> planetEnts = new ArrayList<>();
    private Stack<Entity> leavingEnts = new Stack<>();
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
                coord.scl(terrestrialWidth);

                p.setColor(new Color(biomeMap.getPixel(biomeMap.getWidth() - (int)coord.x, biomeMap.getHeight() - (int)coord.z)));
                p.drawPixel((int)x, (int)z);
            }
        }

        surfaceRender = new Texture(p);
        p.dispose();
    }

    private void generatePhysWorld(){
        physWorld = game.simulation.addWorld(new PhysWorld(Constants.PLANET_PPM){
            @Override
            public void onEntityUpdate(Entity e) {
                // Constrain entities to the world
                float worldWidthPixels = terrestrialWidth * Tile.TILE_SIZE;

                if(e.getX() > worldWidthPixels){
                    e.setX(e.getX() - worldWidthPixels);
                } else if(e.getX() < 0){
                    e.setX(e.getX() + worldWidthPixels);
                }
    
                checkLeavePlanet(e);
                
                // Taken from Celestial.java to correctly apply the right force
                Vector2 dragForce = applyDrag(e.getBody());
                float height = Math.max(e.getBody().getPosition().y, getRadius() / getPhysScale());
                float force = Constants.GRAVITY_CONSTANT * ((body.getMass() * e.getBody().getMass()) / (height * height));
                e.getBody().applyForceToCenter(dragForce.x, force * -0.5f + dragForce.y, true);
            }

            @Override
            public void onAfterUpdate() {
                // Remove entities in the world still
                while(leavingEnts.size() > 0){
                    delEntityWorld(leavingEnts.pop());
                }
            }
        });

        worldBlocks = new WorldBody(game, physWorld, (int)terrestrialWidth, (int)terrestrialHeight);
    }

    // Constructor
    public Planet(App game, PhysWorld world, float x, float y, float terraRadius, float atmosRadius, float atmosDensity) {
        super(game, world, terraRadius);
        this.game = game;

        setPosition(x, y);

        terrestrialHeight = 35;//(float)Math.floor(radius / 10);
        terrestrialWidth = (int)(2.0 * Math.PI * terrestrialHeight);
        atmosphereRadius = atmosRadius;
        atmosphereDensity = atmosDensity;
        generator = new TerrainGenerator((int)terrestrialWidth, (int)terrestrialHeight, terrainSeed);

        atmosComposition.add(Color.CYAN);
        atmosPercentages.add(1.f);

        generateTexture();
        generateSurface();
        generatePhysWorld();

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
    public PhysWorld getPhysWorld(){ return physWorld; }
    public WorldBody getWorldBody(){ return worldBlocks; }
    public float getTerrestrialWidth(){ return terrestrialWidth; }
    public float getTerrestrialHeight(){ return terrestrialHeight; }

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

    public void setStarDirection(Vector3 v){ starDirection.set(v); }

    /**
     * Converts the entity to 2d planet planar coordinates
     * @param e Entity to be converted
     */
    public void addEntityWorld(Entity e){
        if(planetEnts.contains(e)) return;
        if(e.getDriver() != null) this.addEntityWorld(e.getDriver());

        // Formula: x = theta, y = radius
        Vector2 localPos = e.getPosition();
        float x = (localPos.angleRad() / (float)Math.PI / 2.f * terrestrialWidth);
        float y = localPos.len();
        e.setPosition(x, y);

        // Load space angle relative to the world
        float theta = localPos.angleDeg();
        float omega = e.getRotation();
        e.setRotation(omega - theta + 90);

        // Convert orbital velocity to world
        Vector2 vel = e.getBody().getLinearVelocity().cpy().scl(getPhysScale()).scl(1 / Constants.PLANET_PPM);
        Vector2 tangent = localPos.cpy().nor().rotateDeg(-90);
        float velToPlanet = vel.dot(localPos.cpy().nor());
        float tangentVel = vel.dot(tangent);
        e.getBody().setLinearVelocity(tangentVel, -1 * Math.abs(velToPlanet));

        // Add body
        game.simulation.addEntity(physWorld, e);
        planetEnts.add(e);
    }

    /**
     * Convert the entity to the space planet circular coordinates
     * @param e Entity to be converted
     */
    public void delEntityWorld(Entity e){
        // Formula: theta = x, radius = y
        double theta = ((e.getX() / terrestrialWidth) * Math.PI * 2);
        float radius = e.getY();

        // Convert to space angles, spaceAngle = worldAngle + theta - 90
        float worldAngle = e.getRotation();
        e.setRotation(worldAngle + (float)Math.toDegrees(theta) - 90);

        // Convet to space position
        float x = (float)(Math.cos(theta) * radius);
        float y = (float)(Math.sin(theta) * radius);
        e.setPosition(x, y);

        // Convert to space velocity, tangent = x, planetToEntity = y
        Vector2 tangent = new Vector2(0, -1).rotateRad((float)theta);
        Vector2 planetToEnt = e.getPosition().nor();
        Vector2 curVelocity = e.getBody().getLinearVelocity().scl(e.getPhysScale()).scl(1 / Constants.PPM);
        e.getBody().setLinearVelocity(tangent.scl(curVelocity.x).add(planetToEnt.scl(curVelocity.y)));

        if(e instanceof Player || e instanceof Ship)
            game.setScreen(game.spaceScene);

        // Remove body
        game.simulation.addEntity(getWorld(), e);
        planetEnts.remove(e);
    }

    public ArrayList<Entity> getPlanetEntities(){ return planetEnts; }

    public boolean checkTransferPlanet(Entity e){
        // This function checks if the entity supplied
        // is within range to change its physics system to the planet's
        float dist = e.getPosition().len();

        if(dist < radius * 1.2f){
            // Move it into this world
            this.addEntityWorld(e);

            if(e instanceof Player || e instanceof Ship)
                game.setScreen(new PlanetScene(game, this));

            return true;
        }

        return false;
    }

    public boolean checkLeavePlanet(Entity e){
        // This function checks if the entity supplied
        // is far enough to leave the planet's physics world
        if(e.getY() > radius * 1.3f){
            // Move it into this world
            leavingEnts.push(e);
            return true;
        }

        return false;
    }

    public Vector2 applyDrag(Body b){
        // Newtons gravitational law: F = 1/2(density * velocity^2 * dragCoefficient * Area)
        float planetRadPhys = radius / physScale; // Planet radius in physics scale
        float atmosRadPhys = atmosphereRadius / physScale; // Atmosphere radius in physics scale
        float entRadPhys = b.getPosition().len(); // Entity radius in physics scale

        float atmosSurface = atmosRadPhys - planetRadPhys; // Atmosphere radius above surface
        float entSurface = entRadPhys - planetRadPhys; // Entity radius above surface
        float atmosDepth = Math.max(atmosSurface - entSurface, 0) / atmosSurface; // How far the entity is in the atmosphere, where zero is outside and 1 is submerged
        float density = atmosphereDensity * atmosDepth * 0.001f;

        Vector2 relVel = body.getLinearVelocity().cpy().sub(b.getLinearVelocity());
        Vector2 velDir = b.getLinearVelocity().cpy().nor();
        float velSqr = relVel.len2();
        float force = (1.0f / 2.0f) * (density * velSqr * Constants.DRAG_COEFFICIENT);

        return velDir.scl(-1 * force);
    }

    @Override
    public Vector2 applyPhysics(float delta, Entity e){
        checkTransferPlanet(e);
        return super.applyPhysics(delta, e).add(applyDrag(e.getBody()));
    }

    @Override
    public void draw(Batch batch, float a){
        // Save rendering state
        Matrix4 proj = batch.getProjectionMatrix().cpy();
        Matrix4 trans = batch.getTransformMatrix().cpy();
        Matrix4 invProj = proj.cpy().inv();
        Vector2 worldPos = getUniverseSpaceTransform().getTranslation(new Vector2());

        super.draw(batch, a);
        
        // Shade the planet in
        batch.setShader(terraShader);
        terraShader.setUniformMatrix("u_invCamTrans", invProj);
        terraShader.setUniformf("u_starDirection", starDirection);
        terraShader.setUniformf("u_planetWorldPos", worldPos);
        terraShader.setUniformf("u_planetRadius", getRadius());
        batch.draw(surfaceRender, radius * -1, radius * -1, radius * 2, radius * 2);

        batch.setProjectionMatrix(new Matrix4().setToOrtho2D(0, 0, 1280, 720));
        batch.setTransformMatrix(new Matrix4());

        batch.setShader(null);
        batch.draw(debugTexture, 10, 10, 256, 256);

        batch.setShader(atmosShader);
        atmosShader.setUniformMatrix("u_invCamTrans", invProj);
        atmosShader.setUniformf("u_starDirection", starDirection);
        atmosShader.setUniformf("u_planetWorldPos", worldPos);
        atmosShader.setUniformf("u_planetRadius", getRadius());
        atmosShader.setUniformf("u_atmosRadius", getAtmosphereRadius());
        atmosShader.setUniformf("u_atmosColor", getAtmosphereColor());
        batch.draw(texture, 0, 0, 1280, 720);

        batch.setShader(null);
        batch.setProjectionMatrix(proj);
        batch.setTransformMatrix(trans);
    }
    
    @Override
    public boolean remove(){
        texture.dispose();
        return super.remove();
    }

}

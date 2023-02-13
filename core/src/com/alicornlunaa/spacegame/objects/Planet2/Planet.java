package com.alicornlunaa.spacegame.objects.Planet2;

import java.util.HashMap;

import com.alicornlunaa.spacegame.App;
import com.alicornlunaa.spacegame.objects.Simulation.Celestial;
import com.alicornlunaa.spacegame.util.OpenSimplexNoise;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Array;

public class Planet extends Celestial {

    // Variables
    private float terrestrialWidth;
    private float terrestrialHeight;
    private float atmosphereRadius;
    private float atmosphereDensity = 1.0f;
    private Array<Color> atmosComposition = new Array<>();
    private Array<Float> atmosPercentages = new Array<>();
    private long terrainSeed = 123;

    private OpenSimplexNoise noise;
    private Texture texture; // Used for rendering, just 1x1 white
    private Texture surfaceRender;
    private int surfaceRenderResolution = 100;
    private ShaderProgram atmosShader;
    private ShaderProgram terraShader;

    private Vector3 starDirection = new Vector3(1.f, 0.f, 0.f);

    private HashMap<Vector3, Float> pointCloud = new HashMap<>();
    private int slice = 1;
    private float frequency = 8.f;
    private float amplitude = 1.f;

    // Private functions
    private void generateTexture(){
        Pixmap p = new Pixmap(1, 1, Format.RGBA8888);
        p.setColor(Color.WHITE);
        p.fill();
        texture = new Texture(p);
        p.dispose();
    }

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

    private void generateGroundData(){
        // Starts generating a planet surface, using a 3d rectangle with the size of 2πr by 2πr by r
        pointCloud.clear();

        for(int y = 0; y < terrestrialHeight; y++){
            for(int x = 0; x < terrestrialWidth; x++){
                for(int z = 0; z < terrestrialWidth; z++){
                    Vector3 p = new Vector3(x, y, z);
                    float val = (float)(noise.eval(x / frequency, y / frequency, z / frequency) + 1) / 2.f * amplitude;

                    if(val < 0.5f) continue;

                    pointCloud.put(p, val);
                }
            }
        }
    }

    private void generateSurface(){
        // Scan the point cloud data to create a height map
        for(int y = 0; y < surfaceRenderResolution; y++){
            for(int x = 0; x < surfaceRenderResolution; x++){
                // Convert to spherical and grab closest
                float theta = (float)(((float)y / surfaceRenderResolution) * Math.PI);
                float phi = (float)(((float)x / surfaceRenderResolution) * Math.PI);
                Vector3 coord = sphericalToCartesian(new Vector3(terrestrialHeight, theta, phi));
                coord.set((int)coord.x, (int)coord.y, (int)coord.z);

                // TODO: Store into texture
                if(pointCloud.containsKey(coord)){
                    pointCloud.put(coord, 1.f);
                }
            }
        }
    }

    // Constructor
    public Planet(App game, World world, float x, float y, float terraRadius, float atmosRadius, float atmosDensity) {
        super(game, world, terraRadius);

        setPosition(x, y);

        // TODO: Create planet but as a flat plane first, then convert to a sphere/circle

        noise = new OpenSimplexNoise(0);
        terrestrialHeight = 50;//!(float)Math.floor(radius / 1000);
        terrestrialWidth = 50;//!(int)(2.0 * Math.PI * terrestrialHeight);
        atmosphereRadius = atmosRadius;
        atmosphereDensity = atmosDensity;

        atmosComposition.add(Color.CYAN);
        atmosPercentages.add(1.f);

        generateTexture();
        generateGroundData();
        generateSurface();

        atmosShader = game.manager.get("shaders/atmosphere", ShaderProgram.class);
        terraShader = game.manager.get("shaders/planet", ShaderProgram.class);
    }

    // Functions
    public float getAtmosphereRadius(){ return atmosphereRadius; }
    public float getAtmosphereDensity(){ return atmosphereDensity; }
    public Array<Color> getAtmosphereComposition(){ return atmosComposition; }
    public Array<Float> getAtmospherePercentages(){ return atmosPercentages; }
    public long getTerrainSeed(){ return terrainSeed; }

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

    public void setSurface(float frequency){
        this.frequency = frequency;
        generateGroundData();
        generateSurface();
    }

    public void setSlice(int s){
        slice = s;
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
        batch.draw(texture, radius * -1, radius * -1, radius * 2, radius * 2);

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
        batch.setProjectionMatrix(proj);
        batch.setTransformMatrix(trans);

        //! Debug
        batch.end();
        ShapeRenderer renderer = game.shapeRenderer;
        renderer.setProjectionMatrix(proj);
        renderer.setTransformMatrix(trans);
        renderer.begin(ShapeType.Filled);

        for(Vector3 p : pointCloud.keySet()){
            if(p.z != slice) continue;

            float val = pointCloud.get(p);
            renderer.setColor(val, val, val, 1.f);
            renderer.rect(p.x / terrestrialHeight * radius * 2 - radius, p.y / terrestrialHeight * radius * 2 - radius, radius * 2 / terrestrialHeight, radius * 2 / terrestrialHeight);
        }

        renderer.end();
        batch.begin();
    }
    
    @Override
    public boolean remove(){
        texture.dispose();
        return super.remove();
    }

}

package com.alicornlunaa.spacegame.objects.Planet2;

import com.alicornlunaa.spacegame.App;
import com.alicornlunaa.spacegame.objects.Simulation.Celestial;
import com.alicornlunaa.spacegame.util.OpenSimplexNoise;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Array;

public class Planet extends Celestial {

    // Variables
    private float atmosphereRadius;
    private float atmosphereDensity = 1.0f;
    private Array<Color> atmosComposition = new Array<>();
    private Array<Float> atmosPercentages = new Array<>();
    private long terrainSeed = 123;

    private OpenSimplexNoise noise;
    private Texture texture; // Used for rendering, just 1x1 white
    private ShaderProgram atmosShader;
    private ShaderProgram terraShader;

    private Vector3 starDirection = new Vector3(1.f, 0.f, 0.f);

    // Private functions
    private void generateTexture(){
        Pixmap p = new Pixmap(1, 1, Format.RGBA8888);
        p.setColor(Color.WHITE);
        p.fill();
        texture = new Texture(p);
        p.dispose();
    }

    private void generateSurface(){
        // Generate a surface texture for the planet using 3d noise
        
    }

    // Constructor
    public Planet(App game, World world, float x, float y, float terraRadius, float atmosRadius, float atmosDensity) {
        super(game, world, terraRadius);

        setPosition(x, y);

        noise = new OpenSimplexNoise(terrainSeed);
        atmosphereRadius = atmosRadius;
        atmosphereDensity = atmosDensity;

        atmosComposition.add(Color.CYAN);
        atmosPercentages.add(1.f);

        generateTexture();
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
    }
    
    @Override
    public boolean remove(){
        texture.dispose();
        return super.remove();
    }

}

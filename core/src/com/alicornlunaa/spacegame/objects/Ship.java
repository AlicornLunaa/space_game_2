package com.alicornlunaa.spacegame.objects;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.alicornlunaa.spacegame.App;
import com.alicornlunaa.spacegame.parts.Part;
import com.alicornlunaa.spacegame.states.ShipState;
import com.alicornlunaa.spacegame.util.Constants;
import com.alicornlunaa.spacegame.util.ControlSchema;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Matrix3;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.GdxRuntimeException;

/**
 * The ship class will hold the physical ship in space as well as
 * a tilemap and physics world for the player inside the ship
 */
public class Ship extends Entity {
    
    // Variables
    private final App game;
    private Array<Part> parts = new Array<>();
    
    public ShipState state = new ShipState(); // Ship controls and stuff
    
    private World internalWorld;
    private float physAccumulator = 0.0f;
    private Body internalBody;

    // Private functions
    private void generateExterior(World world){
        // Create exterior body for the real-world scenes
        BodyDef def = new BodyDef();
        def.type = BodyType.DynamicBody;
        setBody(world.createBody(def));
    }

    private void generateInterior(){
        // Creates interior world and generates the interior squares
        internalWorld = new World(new Vector2(), true);

        BodyDef def = new BodyDef();
		def.type = BodyType.StaticBody;
		internalBody = internalWorld.createBody(def);
    }

    // Constructor
    public Ship(final App game, World world, float x, float y, float rotation){
        this.game = game;
        
        generateExterior(world);
        generateInterior();

        setPosition(x, y);
        setRotation(rotation);
    }

    // Interior functions
    public void drawWorld(Batch batch, float parentAlpha){
        for(Part p : parts){
            // TODO: Add interior rendering method
            p.draw(batch, parentAlpha);
        }

        game.player.draw(batch, parentAlpha);
        game.debug.render(internalWorld, batch.getProjectionMatrix().cpy().scl(Constants.SHIP_PPM));
    }

    public void updateWorld(float delta){
        // Step the physics world inside the ship
        physAccumulator += Math.min(delta, 0.25f);
        while(physAccumulator >= Constants.TIME_STEP){
            internalWorld.step(Constants.TIME_STEP, Constants.VELOCITY_ITERATIONS, Constants.POSITION_ITERATIONS);
            physAccumulator -= Constants.TIME_STEP;
        }

        game.player.act(delta);
    }

    public World getInteriorWorld(){ return internalWorld; }

    // Space functions
    public void assemble(){
        // Puts all the parts together with their respective physics bodies
        for(Fixture f : body.getFixtureList()){
            body.destroyFixture(f);
        }
        
        for(Part p : parts){
            p.setParent(body, Constants.PPM);
            p.buildInterior(internalBody, Constants.SHIP_PPM);
        }
    }

    public Array<Part> getParts(){ return parts; }

    public void addPart(Part p){
        parts.add(p);
    }

    public boolean save(String path){
        try {
            FileHandle file = Gdx.files.local(path);
            JSONObject data = new JSONObject();
            JSONArray arr = new JSONArray();

            for(Part p : parts){
                arr.put(p.serialize());
            }

            data.put("assembly", arr);
            file.writeString(data.toString(2), false);
            return true;
        } catch(GdxRuntimeException|JSONException e){
            System.out.println("Error saving ship");
            e.printStackTrace();
        }
        
        return false;
    }

    public boolean load(String path){
        try {
            // Read filedata
            FileHandle file = Gdx.files.local(path);
            JSONObject data = new JSONObject(file.readString());

            // Reset body
            body.setLinearVelocity(0, 0);
            body.setAngularVelocity(0);

            // Load body data
            JSONArray partArray = data.getJSONArray("assembly");
            parts.clear();
            for(int i = 0; i < partArray.length(); i++){
                parts.add(Part.unserialize(game, this, partArray.getJSONObject(i)));
            }
            assemble();

            System.out.printf("Ship %s loaded\n", path);
            return true;
        } catch (GdxRuntimeException|JSONException e){
            System.out.println("Error reading ship");
            e.printStackTrace();
        }

        return false;
    }

    private void computeSAS(){
        // Reduce angular velocity with controls
        float angVel = body.getAngularVelocity();
        angVel = Math.min(Math.max(angVel * 2, -1), 1); // Clamp value

        if(Math.abs(angVel) <= 0.005f) angVel = 0;

        state.artifRoll = angVel;
    }

    @Override
    public void act(float delta){
        if(state.sas){ computeSAS(); } else { state.artifRoll = 0; }
        if(driver == null) return;

        // Update parts
        for(Part p : parts){
            p.update(delta);
        }

        // Ship controls
        if(Gdx.input.isKeyPressed(ControlSchema.SHIP_INCREASE_THROTTLE)){
            state.throttle = Math.min(state.throttle + 0.01f, 1);
        } else if(Gdx.input.isKeyPressed(ControlSchema.SHIP_DECREASE_THROTTLE)){
            state.throttle = Math.max(state.throttle - 0.01f, 0);
        }
        
        if(Gdx.input.isKeyPressed(ControlSchema.SHIP_ROLL_LEFT)){
            state.roll = -1;
        } else if(Gdx.input.isKeyPressed(ControlSchema.SHIP_ROLL_RIGHT)){
            state.roll = 1;
        } else {
            state.roll = 0;
        }
        
        if(Gdx.input.isKeyPressed(ControlSchema.SHIP_TRANSLATE_UP)){
            state.vertical = 1;
        } else if(Gdx.input.isKeyPressed(ControlSchema.SHIP_TRANSLATE_DOWN)){
            state.vertical = -1;
        } else {
            state.vertical = 0;
        }
        
        if(Gdx.input.isKeyPressed(ControlSchema.SHIP_TRANSLATE_LEFT)){
            state.horizontal = -1;
        } else if(Gdx.input.isKeyPressed(ControlSchema.SHIP_TRANSLATE_RIGHT)){
            state.horizontal = 1;
        } else {
            state.horizontal = 0;
        }
        
        super.act(delta);
    }

    @Override
    protected void afterWorldChange(){
        for(Part p : parts){
            p.setParent(body, getPhysScale());
        }
    }

    @Override
    public void draw(Batch batch, float parentAlpha){
        Matrix3 transform = getTransform();
        batch.setTransformMatrix(batch.getTransformMatrix().mul(new Matrix4().set(transform)));

        for(Part p : parts){
            p.draw(batch, Gdx.graphics.getDeltaTime());
        }
        
        batch.setTransformMatrix(batch.getTransformMatrix().mul(new Matrix4().set(transform.inv())));
    }

    @Override
    public boolean remove(){
        for(Part p : parts){
            p.dispose();
        }

        return super.remove();
    }

}

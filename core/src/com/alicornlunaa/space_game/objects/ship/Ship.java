package com.alicornlunaa.space_game.objects.ship;

import org.json.JSONException;
import org.json.JSONObject;

import com.alicornlunaa.selene_engine.ecs.BodyComponent;
import com.alicornlunaa.selene_engine.phys.PhysWorld;
import com.alicornlunaa.space_game.App;
import com.alicornlunaa.space_game.components.ship.parts.Part;
import com.alicornlunaa.space_game.objects.ship.interior.InteriorComponent;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.Null;
import com.badlogic.ashley.core.Entity;

// Ship is a tree of parts, doublely linked
public class Ship extends Entity {
    // Static classes
    public static class State {
        /** Contains a list of all the ship statistics and variables
         * such as fuel level, electricity level, thrust level, RCS,
         * SAS, and other instance based variables
         */
        public boolean debug = false; // Debug drawings
        
        public boolean rcs = false; // RCS thrusters
        public boolean sas = false; // Stability controller
    
        public float throttle = 0; // Thruster throttle
        public float roll = 0; // Rotational movement intention
        public float vertical = 0; // Translation movement intention
        public float horizontal = 0; // Translation movement intention
        public float artifRoll = 0; // Artificial roll, used by computer control
    
        public float rcsStored = 0.0f;
        public float rcsCapacity = 0.0f;
        public float liquidFuelStored = 0.0f;
        public float liquidFuelCapacity = 0.0f;
        public float batteryStored = 0.0f;
        public float batteryCapacity = 0.0f;
    }

    // Variables
    private InteriorComponent interior;
    private @Null Part rootPart = null;
    private State state = new State();

    // Private functions
    private void generateExterior(PhysWorld world){
        // Create exterior body for the real-world scenes
        BodyDef def = new BodyDef();
        def.type = BodyType.DynamicBody;
        
        // bodyComponent = addComponent(new BodyComponent(world, def){
        //     @Override
        //     public void afterWorldChange(PhysWorld world){
        //         Ship.super.afterWorldChange(world);
        //         Ship.this.assemble();
        //     }
        // });
        
        // bodyComponent.setWorld(world);
        // bodyComponent.body.setTransform(-1, 1, 0);
    }

    private void computeSAS(){
        // Reduce angular velocity with controls
        float angVel = 0;//bodyComponent.body.getAngularVelocity();
        angVel = Math.min(Math.max(angVel * 2, -1), 1); // Clamp value

        if(Math.abs(angVel) <= 0.005f) angVel = 0;

        state.artifRoll = angVel;
    }

    // Constructor
    public Ship(final App game, PhysWorld world, float x, float y, float rotation){
        generateExterior(world);
        
        // addComponent(new PlanetPhysScript(this));
        // addComponent(new CustomSpriteComponent() {
        //     @Override
        //     public void render(Batch batch) {
        //         if(rootPart == null) return;

        //         Matrix4 trans = batch.getTransformMatrix().cpy().rotate(0, 0, 1, rootPart.getRotation());
        //         rootPart.draw(batch, trans.cpy());
        //     }
        // });
        // addComponent(new ScriptComponent(this) {
        //     @Override
        //     public void start(){}
            
        //     @Override
        //     public void update() {
        //         // Ship controls
        //         if(getDriver() == null) return;

        //         if(Gdx.input.isKeyPressed(ControlSchema.SHIP_INCREASE_THROTTLE)){
        //             state.throttle = Math.min(state.throttle + 0.01f, 1);
        //         } else if(Gdx.input.isKeyPressed(ControlSchema.SHIP_DECREASE_THROTTLE)){
        //             state.throttle = Math.max(state.throttle - 0.01f, 0);
        //         }
                
        //         if(Gdx.input.isKeyPressed(ControlSchema.SHIP_ROLL_LEFT)){
        //             state.roll = -1;
        //         } else if(Gdx.input.isKeyPressed(ControlSchema.SHIP_ROLL_RIGHT)){
        //             state.roll = 1;
        //         } else {
        //             state.roll = 0;
        //         }
                
        //         if(Gdx.input.isKeyPressed(ControlSchema.SHIP_TRANSLATE_UP)){
        //             state.vertical = 1;
        //         } else if(Gdx.input.isKeyPressed(ControlSchema.SHIP_TRANSLATE_DOWN)){
        //             state.vertical = -1;
        //         } else {
        //             state.vertical = 0;
        //         }
                
        //         if(Gdx.input.isKeyPressed(ControlSchema.SHIP_TRANSLATE_LEFT)){
        //             state.horizontal = -1;
        //         } else if(Gdx.input.isKeyPressed(ControlSchema.SHIP_TRANSLATE_RIGHT)){
        //             state.horizontal = 1;
        //         } else {
        //             state.horizontal = 0;
        //         }

        //         if(state.sas){
        //             computeSAS();
        //         } else {
        //             state.artifRoll = 0;
        //         }

        //         if(rootPart != null)
        //             rootPart.tick(Gdx.graphics.getDeltaTime());
        //     }

        //     @Override
        //     public void render() {
        //         if(Gdx.input.isKeyJustPressed(ControlSchema.SHIP_TOGGLE_RCS)){
        //             state.rcs = !state.rcs;
        //         }
        //         if(Gdx.input.isKeyJustPressed(ControlSchema.SHIP_TOGGLE_SAS)){
        //             state.sas = !state.sas;
        //         }
        //         if(Gdx.input.isKeyJustPressed(ControlSchema.SHIP_FULL_THROTTLE)){
        //             state.throttle = 1;
        //         }
        //         if(Gdx.input.isKeyJustPressed(ControlSchema.SHIP_NO_THROTTLE)){
        //             state.throttle = 0;
        //         }

        //         // Entering the ship
        //         if(Gdx.input.isButtonJustPressed(Buttons.LEFT)){
        //             Vector3 clickPosInWorld = game.camera.unproject(new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0));
        //             Vector2 position = new Vector2(clickPosInWorld.x, clickPosInWorld.y).sub(transform.position.cpy().scl(Constants.PPM)).add(bodyComponent.body.getLocalCenter().cpy().scl(Constants.PPM));
                    
        //             if(rootPart.contains(position)){
        //                 System.out.println("Drive");
        //                 // drive(getDriver());
        //             }
        //         }
        //     }
        // });
        // addComponent(new CameraComponent(1280, 720)).active = false;
        interior = new InteriorComponent(game, this);
    
        // transform.position.set(x, y);
        // transform.rotation = rotation;
    }

    // Functions
    public boolean save(String path){
        try {
            FileHandle file = Gdx.files.local(path);
            JSONObject data = new JSONObject();
            data.put("assembly", rootPart.serialize());
            file.writeString(data.toString(2), false);
            return true;
        } catch(GdxRuntimeException|JSONException e){
            System.out.println("Error saving ship");
            e.printStackTrace();
        }
        
        return false;
    }

    public boolean load(String path){
        // try {
        //     // Read filedata
        //     FileHandle file = Gdx.files.local(path);
        //     JSONObject data = new JSONObject(file.readString());

        //     // Reset body
        //     for(Fixture f : bodyComponent.body.getFixtureList()){
        //         bodyComponent.body.destroyFixture(f);
        //     }

        //     bodyComponent.body.setLinearVelocity(0, 0);
        //     bodyComponent.body.setAngularVelocity(0);

        //     // Load body data
        //     rootPart = Part.unserialize(game, this, data.getJSONObject("assembly"));
        //     this.assemble();

        //     System.out.printf("Ship %s loaded\n", path);
        //     return true;
        // } catch (GdxRuntimeException|JSONException e){
        //     System.out.println("Error reading ship");
        //     e.printStackTrace();
        // }

        return false;
    }
    
    public void setRootPart(Part p){ this.rootPart = p; }
    public Part getRootPart(){ return rootPart; }
    
    public void assemble(){
        if(rootPart != null)
            rootPart.setParent(this, new Matrix4());

        if(interior != null)
            interior.assemble();
    }

    public BodyComponent getBody(){
        // return bodyComponent;
        return new BodyComponent();
    }

    public InteriorComponent getInterior(){
        return interior;
    }

    public State getState(){
        return state;
    }
}

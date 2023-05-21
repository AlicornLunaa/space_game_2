package com.alicornlunaa.spacegame.objects.ship;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.alicornlunaa.selene_engine.components.BodyComponent;
import com.alicornlunaa.selene_engine.components.IScriptComponent;
import com.alicornlunaa.selene_engine.core.DriveableEntity;
import com.alicornlunaa.selene_engine.phys.PhysWorld;
import com.alicornlunaa.spacegame.App;
import com.alicornlunaa.spacegame.components.CustomSpriteComponent;
import com.alicornlunaa.spacegame.objects.ship.interior.Interior;
import com.alicornlunaa.spacegame.objects.ship.parts.Part;
import com.alicornlunaa.spacegame.objects.simulation.Celestial;
import com.alicornlunaa.spacegame.scripts.GravityScript;
import com.alicornlunaa.spacegame.scripts.PlanetPhysScript;
import com.alicornlunaa.spacegame.states.ShipState;
import com.alicornlunaa.spacegame.util.Constants;
import com.alicornlunaa.spacegame.util.ControlSchema;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.GdxRuntimeException;

/**
 * The ship class will hold the physical ship in space as well as
 * a tilemap and physics world for the player inside the ship
 */
public class Ship extends DriveableEntity {
    
    // Variables
    private final App game;
    public BodyComponent bodyComponent;

    private Array<Part> parts = new Array<>();
    private AttachmentList attachments = new AttachmentList();
    private Interior interior;

    public ShipState state = new ShipState(); // Ship controls and stuff
    
    // Private functions
    private void generateExterior(PhysWorld world){
        // Create exterior body for the real-world scenes
        BodyDef def = new BodyDef();
        def.type = BodyType.DynamicBody;
        bodyComponent = addComponent(new BodyComponent(world, def){
            @Override
            public void afterWorldChange(PhysWorld world){
                Ship.super.afterWorldChange(world);

                for(Part p : parts){
                    p.setParent(bodyComponent.body, getPhysScale());
                }
            }
        });
        bodyComponent.setWorld(world);
    }

    // Constructor
    public Ship(final App game, PhysWorld world, float x, float y, float rotation){
        super(game);
        this.game = game;
        generateExterior(world);
        interior = new Interior(game, this);

        addComponent(new GravityScript(game, this));
        addComponent(new PlanetPhysScript(this));
        addComponent(new IScriptComponent() {
            @Override
            public void update(){
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
            }

            @Override
            public void render(){}
        });
        addComponent(new CustomSpriteComponent() {
            @Override
            public void render(Batch batch) {
                // Update SAS
                if(state.sas){ computeSAS(); } else { state.artifRoll = 0; }

                // Update parts
                // TODO: Parts shouldnt be here
                for(Part p : parts){
                    p.update(Gdx.graphics.getDeltaTime());
                }

                // Finish rendering
                Vector2 pos = bodyComponent.body.getPosition().cpy().scl(bodyComponent.world.getPhysScale());
                Matrix4 trans = new Matrix4();
                Celestial parent = game.universe.getParentCelestial(Ship.this);
                if(parent != null) trans.set(parent.getUniverseSpaceTransform());
                trans.translate(pos.x, pos.y, 0.0f);
                trans.rotateRad(0, 0, 1, bodyComponent.body.getAngle());
                batch.setTransformMatrix(trans);

                for(Part p : parts){
                    p.draw(batch, Gdx.graphics.getDeltaTime());
                }
            }
        });

        setPosition(x, y);
        setRotation(rotation);
    }

    // Interior functions
    public void drawWorld(Batch batch, float parentAlpha){
        interior.draw(batch);
        // game.player.render(batch); TODO: REPLACE WITH RENDER SYSTEM
    }

    public PhysWorld getInteriorWorld(){ return interior.getWorld(); }

    // Space functions
    public void assemble(){
        // Puts all the parts together with their respective physics bodies
        for(Part p : parts){
            p.setParent(bodyComponent.body, Constants.PPM);
        }

        interior.assemble();
    }

    public AttachmentList getAttachments(){ return attachments; }

    public Array<Part> getParts(){ return parts; }

    public void addPart(Part p){
        parts.add(p);
        attachments.addPart(p);
    }

    public void removePart(Part p){
        parts.removeValue(p, true);
        attachments.removePart(p);
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
            for(Fixture f : bodyComponent.body.getFixtureList()){
                bodyComponent.body.destroyFixture(f);
            }

            bodyComponent.body.setLinearVelocity(0, 0);
            bodyComponent.body.setAngularVelocity(0);

            // Load body data
            JSONArray partArray = data.getJSONArray("assembly");
            parts.clear();
            for(int i = 0; i < partArray.length(); i++){
                addPart(Part.unserialize(game, this, partArray.getJSONObject(i)));
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
        float angVel = bodyComponent.body.getAngularVelocity();
        angVel = Math.min(Math.max(angVel * 2, -1), 1); // Clamp value

        if(Math.abs(angVel) <= 0.005f) angVel = 0;

        state.artifRoll = angVel;
    }

    @Override
    public void dispose(){
        for(Part p : parts){
            p.dispose();
        }
    }

}

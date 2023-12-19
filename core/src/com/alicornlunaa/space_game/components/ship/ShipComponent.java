package com.alicornlunaa.space_game.components.ship;

import org.json.JSONException;
import org.json.JSONObject;

import com.alicornlunaa.space_game.components.ship.parts.Part;
import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.Null;

/* Entering the ship
if(Gdx.input.isButtonJustPressed(Buttons.LEFT)){
    Vector3 clickPosInWorld = game.camera.unproject(new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0));
    Vector2 position = new Vector2(clickPosInWorld.x, clickPosInWorld.y).sub(transform.position.cpy().scl(Constants.PPM)).add(bodyComponent.body.getLocalCenter().cpy().scl(Constants.PPM));
                    
    if(rootPart.contains(position)){
        System.out.println("Drive");
        drive(getDriver());
    }
}
*/

public class ShipComponent implements Component {
    // Variables
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

    public @Null Part rootPart = null;

    // Constructors
    public ShipComponent(){
        // Empty ship constructor
    }

    public ShipComponent(String path){
        // Load default ship
        this();

        // Load ship from file
        try {
            // Read filedata
            FileHandle file = Gdx.files.local(path);
            JSONObject data = new JSONObject(file.readString());
            rootPart = Part.unserialize(this, data.getJSONObject("assembly"));
        } catch (GdxRuntimeException|JSONException e){
            System.out.println("Error reading ship");
            e.printStackTrace();
        }
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
}

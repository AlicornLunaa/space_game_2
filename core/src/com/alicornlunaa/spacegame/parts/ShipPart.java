package com.alicornlunaa.spacegame.parts;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.alicornlunaa.spacegame.util.Assets;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.utils.GdxRuntimeException;

public class ShipPart extends Actor {
    // Variables
    protected Body parent;
    private PolygonShape shape;
    private TextureRegion region;
    private ArrayList<Vector2> attachmentPoints;

    // Constructor
    public ShipPart(Body parent, TextureRegion texture, Vector2 size, Vector2 posOffset, float rotOffset){
        this.parent = parent;
        region = texture;
        shape = new PolygonShape();
        attachmentPoints = new ArrayList<Vector2>();

        setSize(size.x, size.y);
        setOrigin(size.x / 2.f - posOffset.x, size.y / 2.f - posOffset.y);
        setPosition(posOffset.x, posOffset.y);
        setRotation(rotOffset);

        shape.setAsBox(size.x / 2.f, size.y / 2.f, posOffset, rotOffset * (float)(Math.PI / 180.f));
        parent.createFixture(shape, 0.5f);
    }

    // Functions
    @Override
    public void setRotation(float r){
        super.setRotation(r);
        shape.setAsBox(getWidth() / 2.f, getHeight() / 2.f, new Vector2(getX(), getY()), getRotation() * (float)(180.f / Math.PI));
    }

    @Override
    public void setPosition(float x, float y){
        super.setPosition(x, y);
        shape.setAsBox(getWidth() / 2.f, getHeight() / 2.f, new Vector2(getX(), getY()), getRotation() * (float)(180.f / Math.PI));
    }

    @Override
    public void setSize(float w, float h){
        super.setSize(w, h);
        shape.setAsBox(getWidth() / 2.f, getHeight() / 2.f, new Vector2(getX(), getY()), getRotation() * (float)(180.f / Math.PI));
    }

    @Override
    public void act(float delta){
        super.act(delta);
    }

    @Override
    public void draw(Batch batch, float parentAlpha){
        Color c = getColor();
        batch.setColor(c.r, c.g, c.b, c.a * parentAlpha);
        batch.draw(
            region,
            parent.getPosition().x + getX() - getWidth() / 2,
            parent.getPosition().y + getY() - getHeight() / 2,
            getOriginX(),
            getOriginY(),
            getWidth(),
            getHeight(),
            getScaleX(),
            getScaleY(),
            (parent.getAngle() * (float)(180.f / Math.PI)) + getRotation()
        );
    }

    @Override
    public boolean remove(){
        shape.dispose();
        return super.remove();
    }

    // Static methods
    public static ShipPart fromJSON(Assets manager, JSONObject data, Body parent, Vector2 posOffset, float rotOffset){
        // Part information is in parts_layout.md
        try {
            // Load part information from the json object
            String type = data.getString("type");
            String name = data.getString("name");
            String desc = data.getString("desc");
            String texture = data.getString("texture");
            TextureRegion region = new TextureRegion(
                manager.get(texture, Texture.class),
                data.getJSONObject("uv").getInt("x"),
                data.getJSONObject("uv").getInt("y"),
                data.getJSONObject("uv").getInt("width"),
                data.getJSONObject("uv").getInt("height")
            );
            Vector2 size = new Vector2(
                data.getJSONObject("scale").getFloat("width") * data.getJSONObject("scale").getFloat("scale"),
                data.getJSONObject("scale").getFloat("height") * data.getJSONObject("scale").getFloat("scale")
            );
            float density = data.getFloat("density");
            ArrayList<Vector2> attachmentPoints = new ArrayList<Vector2>();
            
            JSONArray points = data.getJSONArray("attachmentPoints");
            for(int i = 0; i < points.length(); i++){
                JSONObject o = points.getJSONObject(i);
                attachmentPoints.add(new Vector2(o.getFloat("x"), o.getFloat("y")));
            }

            JSONObject metadata = data.getJSONObject("metadata");

            switch(type){
                case "AERO":
                    float drag = metadata.getFloat("drag");
                    float lift = metadata.getFloat("lift");
                    return new Aero(parent, region, size, posOffset, rotOffset, name, desc, density, drag, lift);
                    
                case "STRUCTURAL":
                    float fuel = metadata.getFloat("fuelCapacity");
                    float battery = metadata.getFloat("batteryCapacity");
                    return new Structural(parent, region, size, posOffset, rotOffset, name, desc, density, fuel, battery);
                    
                case "THRUSTER":
                    float power = metadata.getFloat("power");
                    float cone = metadata.getFloat("cone");
                    float usage = metadata.getFloat("fuelUsage");
                    return new Thruster(parent, region, size, posOffset, rotOffset, name, desc, density, power, cone);
            }
        } catch(GdxRuntimeException|JSONException e){
            System.out.println("Error reading the part data");
            e.printStackTrace();
        }

        return null;
    }
}

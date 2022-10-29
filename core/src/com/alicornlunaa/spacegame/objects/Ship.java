package com.alicornlunaa.spacegame.objects;

import java.util.ArrayList;

import com.alicornlunaa.spacegame.parts.ShipPart;
import com.alicornlunaa.spacegame.util.Assets;
import com.alicornlunaa.spacegame.util.PartManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.scenes.scene2d.Actor;

public class Ship extends Actor {
    private Assets manager;
    private PartManager partManager;
    private Body body;
    private ArrayList<ShipPart> parts;

    public Ship(Assets manager, PartManager partManager, World world, float x, float y, float rotation){
        this.manager = manager;
        this.partManager = partManager;

        BodyDef def = new BodyDef();
		def.type = BodyType.DynamicBody;
		def.position.set(x, y);
        def.angle = rotation;
		body = world.createBody(def);

        setPosition(body.getPosition().x, body.getPosition().y);
        setRotation(body.getAngle() * (float)(180.f / Math.PI));

        parts = new ArrayList<ShipPart>();
        parts.add(ShipPart.fromJSON(manager, partManager.get("AERO", "MED_CMD_POD"), body, new Vector2(0, 16), 0.f));
        parts.add(ShipPart.fromJSON(manager, partManager.get("STRUCTURAL", "BSC_FUSELAGE"), body, new Vector2(0, 0), 0.f));
        parts.add(ShipPart.fromJSON(manager, partManager.get("THRUSTER", "BSC_THRUSTER"), body, new Vector2(0, -16), 0.f));
    }

    public ShipPart addPart(String type, String id, Vector2 pos, float rot){
        ShipPart part = ShipPart.fromJSON(manager, partManager.get(type, id), body, pos, rot);
        parts.add(part);

        return part;
    }
    
    public boolean delPart(ShipPart part){
        for(int i = 0; i < parts.size(); i++){
            if(parts.get(i).equals(part)){
                parts.remove(i);
                i--;
                
                return true;
            }
        }

        return false;
    }

    public int getPartCount(){
        return parts.size();
    }

    @Override
    public void act(float delta){
        super.act(delta);

        for(ShipPart p : parts){
            p.act(delta);
        }

        setPosition(body.getPosition().x, body.getPosition().y);
        setRotation(body.getAngle() * (float)(180.f / Math.PI));
    }

    @Override
    public void draw(Batch batch, float parentAlpha){
        Color c = getColor();
        
        for(ShipPart p : parts){
            p.draw(batch, c.a * parentAlpha);
        }
    }

    @Override
    public boolean remove(){
        for(ShipPart p : parts){
            p.remove();
        }

        return super.remove();
    }
}

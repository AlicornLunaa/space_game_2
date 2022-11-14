package com.alicornlunaa.spacegame.objects.Ship;

import java.util.HashMap;

import com.alicornlunaa.spacegame.parts.Part;
import com.badlogic.gdx.math.Matrix3;
import com.badlogic.gdx.math.Vector2;

/**
 * The attachment tree holds the positions for attachments in the ship in ship-space coordinates
 * Everytime a part is attached to the ship, add the new points that dont already exist
 */
public class AttachmentList {

    // Variables
    private HashMap<Vector2, Boolean> attachments = new HashMap<>();
    
    // Constructor
    public AttachmentList(){}

    // Functions
    public void addPart(Part p){
        Matrix3 partTrans = new Matrix3().translate(p.getX(), p.getY()).rotate(p.getRotation()).scale(p.getFlipX() ? -1 : 1, p.getFlipY() ? -1 : 1);
        for(Vector2 a : p.getAttachmentPoints()){
            Vector2 transformed = a.cpy().mul(partTrans);
            attachments.put(transformed, attachments.containsKey(transformed));
        }
    }

    public void removePart(Part p){
        Matrix3 partTrans = new Matrix3().translate(p.getX(), p.getY()).rotate(p.getRotation()).scale(p.getFlipX() ? -1 : 1, p.getFlipY() ? -1 : 1);
        for(Vector2 a : p.getAttachmentPoints()){
            Vector2 transformed = a.cpy().mul(partTrans);

            if(attachments.containsKey(transformed)){
                attachments.remove(transformed);
            }
        }
    }

    public void setActive(Vector2 pos, boolean active){
        attachments.put(pos, active);
    }

    public boolean getActive(Vector2 pos){
        return attachments.get(pos);
    }

    public HashMap<Vector2, Boolean> getMap(){ return attachments; }

}

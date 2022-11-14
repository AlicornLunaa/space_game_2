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

    // Classes
    private static class Attachment {
        /** This class exists to count the number of attachments so when a part is removed, the correct
         * points are kept.
         */
        private boolean inUse = false;
        private int count = 0;

        private Attachment(boolean inUse, int count){
            this.inUse = inUse;
            this.count = count;
        }
    };

    // Variables
    private HashMap<Vector2, Attachment> attachments = new HashMap<>();
    
    // Constructor
    public AttachmentList(){}

    // Functions
    public void addPart(Part p){
        Matrix3 partTrans = new Matrix3().translate(p.getX(), p.getY()).rotate(p.getRotation()).scale(p.getFlipX() ? -1 : 1, p.getFlipY() ? -1 : 1);
        for(Vector2 a : p.getAttachmentPoints()){
            Vector2 transformed = a.cpy().mul(partTrans);

            // If the attachment exists, add one ot the count, otherwise add new
            if(attachments.containsKey(transformed)){
                attachments.get(transformed).inUse = true;
                attachments.get(transformed).count++;
            } else {
                attachments.put(transformed, new Attachment(false, 1));
            }
        }
    }

    public void removePart(Part p){
        Matrix3 partTrans = new Matrix3().translate(p.getX(), p.getY()).rotate(p.getRotation()).scale(p.getFlipX() ? -1 : 1, p.getFlipY() ? -1 : 1);
        for(Vector2 a : p.getAttachmentPoints()){
            Vector2 transformed = a.cpy().mul(partTrans);

            if(attachments.containsKey(transformed)){
                attachments.get(transformed).count--;

                if(attachments.get(transformed).count == 0){
                    attachments.remove(transformed);
                } else if(attachments.get(transformed).count == 1){
                    attachments.get(transformed).inUse = false;
                }
            }
        }
    }

    public boolean getActive(Vector2 pos){
        return attachments.get(pos).inUse;
    }

    public HashMap<Vector2, Attachment> getMap(){ return attachments; }

}

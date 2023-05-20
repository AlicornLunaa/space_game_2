package com.alicornlunaa.selene_engine.vfx;

import com.badlogic.gdx.utils.Array;

// Manages all the visual effects
public class VfxManager {
    
    // Variables
    private Array<IVfx> vfxList = new Array<>();

    // Functions
    public void add(IVfx vfx){
        vfxList.add(vfx);
    }

    public void update(float delta){
        for(int i = 0; i < vfxList.size; i++){
            if(vfxList.get(i).update(delta)){
                vfxList.removeIndex(i);
                i--;
            }
        }
    }

}

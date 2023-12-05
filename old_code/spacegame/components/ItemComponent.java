package com.alicornlunaa.spacegame.components;

import com.alicornlunaa.selene_engine.ecs.IComponent;
import com.alicornlunaa.selene_engine.util.asset_manager.Assets;
import com.alicornlunaa.selene_engine.util.asset_manager.Assets.Reloadable;
import com.alicornlunaa.space_game.App;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class ItemComponent implements IComponent,Reloadable {
    // Variables
    public String itemID;
    public int maxStack;
    public int count;
    public TextureRegion itemTexture;

    // Constructor
    public ItemComponent(String itemID){
        this.itemID = itemID;
        itemTexture = App.instance.atlas.findRegion("items/" + itemID);
    }

    public ItemComponent(String itemID, int count, int maxStack){
        this(itemID);
        this.maxStack = maxStack;
        this.count = count;
    }

    // Functions
    public void leftClick(){}
    public void rightClick(){}
    public void drop(){}

    @Override
    public void reload(Assets assets) {
        itemTexture = App.instance.atlas.findRegion("items/" + itemID);
    }
}

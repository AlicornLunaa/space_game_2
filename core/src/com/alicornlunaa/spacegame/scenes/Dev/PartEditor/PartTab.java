package com.alicornlunaa.spacegame.scenes.Dev.PartEditor;

import org.json.JSONObject;

import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.kotcrab.vis.ui.widget.VisTable;
import com.kotcrab.vis.ui.widget.tabbedpane.Tab;

public class PartTab extends Tab {

    private VisTable content = new VisTable();
    private JSONObject part;

    public PartTab(JSONObject part){
        super(true, false);
        this.part = part;
    }

    public JSONObject getData(){
        return part;
    }

    @Override
    public String getTabTitle(){
        return part.getString("id");
    }

    @Override
    public Table getContentTable(){
        return content;
    }
    
}

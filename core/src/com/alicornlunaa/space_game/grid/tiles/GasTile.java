package com.alicornlunaa.space_game.grid.tiles;

import com.alicornlunaa.selene_engine.phys.Collider;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class GasTile extends TileElement {
    public GasTile(Element element, TextureRegion texture, Collider collider) {
        super(element, State.GAS, texture, collider);
    }
}

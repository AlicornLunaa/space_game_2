package com.alicornlunaa.space_game.components.ship;

import com.alicornlunaa.space_game.components.ship.interior.InteriorCell;
import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.utils.Array;

public class InteriorComponent implements Component {
    // Variables
    public Array<InteriorCell> cells = new Array<>();
}

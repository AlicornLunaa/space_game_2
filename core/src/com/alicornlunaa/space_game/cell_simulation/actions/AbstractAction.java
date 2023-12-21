package com.alicornlunaa.space_game.cell_simulation.actions;

import com.alicornlunaa.space_game.cell_simulation.Simulation;

public abstract class AbstractAction {
    public abstract boolean commit(Simulation simulation);
}

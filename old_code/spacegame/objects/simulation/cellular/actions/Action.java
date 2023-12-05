package com.alicornlunaa.spacegame.objects.simulation.cellular.actions;

import com.alicornlunaa.spacegame.objects.simulation.cellular.CellWorld;

public abstract class Action {
    abstract public boolean commit(CellWorld world);
}

package com.alicornlunaa.spacegame.util;

public class RootSolver {

    public static interface EquationInterface { public float func(float x); }

    public static float bisection(float a, float b, EquationInterface equation){
        float tolerance = 1e-6f;
        int maxIter = 96;

        for(int i = 0; i < maxIter; i++){
            float c = (a + b) / 2;
            float ea = equation.func(a);
            float ec = equation.func(c);

            if(ec == 0 || (b - a) / 2 < tolerance){
                return c;
            }

            if(Math.signum(ec) == Math.signum(ea)){
                a = c;
            } else {
                b = c;
            }
        }

        return 0.f;
    }
    
}

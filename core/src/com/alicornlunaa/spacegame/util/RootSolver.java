package com.alicornlunaa.spacegame.util;

public class RootSolver {

    public static interface EquationInterface { public double func(double x); }

    public static double bisection(double a, double b, EquationInterface equation){
        double tolerance = 1e-6f;
        int maxIter = 96;

        for(int i = 0; i < maxIter; i++){
            double c = (a + b) / 2;
            double ea = equation.func(a);
            double ec = equation.func(c);

            if(ec == 0 || (b - a) / 2 < tolerance){
                return c;
            }

            if(Math.signum(ec) == Math.signum(ea)){
                a = c;
            } else {
                b = c;
            }
        }

        return 0.0;
    }

    public static double newtonian(double meanAnomaly, EquationInterface equation){
        double stepSize = 1e-3;
        double epsilon = 1e-8;
        double guess = meanAnomaly;
        int maxIter = 128;

        for(int i = 0; i < maxIter; i++){
            double y = equation.func(guess);

            if(Math.abs(y) < epsilon) break;

            double slope = (equation.func(guess + stepSize) - y) / stepSize;
            double step = y / slope;
            guess -= step;
        }

        return guess;
    }
    
}

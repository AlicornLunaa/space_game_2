package com.alicornlunaa.spacegame.util;

public class RootSolver {

    public static interface EquationInterface { public double func(double x); }

    public static double bisection(double a, double b, EquationInterface equationInterface){
        double tolerance = 1e-9;
        int maxIter = 256;

        for(int i = 0; i < maxIter; i++){
            double c = (a + b) / 2;
            double ea = equationInterface.func(a);
            double ec = equationInterface.func(c);

            if(Math.abs(ec) < tolerance || (b - a) / 2 < tolerance){
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

    public static double newtonian(double initial, EquationInterface equation){
        double stepSize = 1e-6;
        double epsilon = 1e-9;
        double guess = initial;
        int maxIter = 256;

        for(int i = 0; i < maxIter; i++){
            double y = equation.func(guess);

            if(Math.abs(y) < epsilon) break;

            double slope = (equation.func(guess + stepSize) - y) / stepSize;
            double step = y / slope;
            guess -= step;
        }

        return guess;
    }

    public static double newtonian(double initial, EquationInterface equation, EquationInterface derivation){
        double epsilon = 1e-9;
        double guess = initial;
        int maxIter = 256;

        for(int i = 0; i < maxIter; i++){
            double step = equation.func(guess) / derivation.func(guess);

            if(Math.abs(step) < epsilon) break;

            guess -= step;
        }

        return guess;
    }
    
}

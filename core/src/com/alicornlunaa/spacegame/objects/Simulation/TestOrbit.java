package com.alicornlunaa.spacegame.objects.Simulation;

public class TestOrbit {

  public static double[] keplerianToCartesian(double a, double e, double i, double omega, double w, double M,
      double mu) {
    // Calculate the semi-latus rectum
    double p = a * (1 - e * e);

    // Calculate the eccentric anomaly
    double E = M;
    double tolerance = 1e-6;
    double error = 1;
    while (error > tolerance) {
      double ratio = (E - e * Math.sin(E) - M) / (1 - e * Math.cos(E));
      E = E - ratio;
      error = Math.abs(ratio);
    }

    // Calculate the true anomaly
    double nu = 2 * Math.atan2(Math.sqrt(1 + e) * Math.sin(E / 2), Math.sqrt(1 - e) * Math.cos(E / 2));

    // Calculate the position and velocity in the orbital plane
    double r = p / (1 + e * Math.cos(nu));
    double v = Math.sqrt(mu / p);
    double[] pos = { r * Math.cos(nu), r * Math.sin(nu) };
    double[] vel = { -v * Math.sin(nu), v * (e + Math.cos(nu)) };

    // Rotate the position and velocity vectors to the desired reference plane
    double[] posRotated = rotate(pos, 0, omega, w - Math.PI / 2);
    double[] velRotated = rotate(vel, 0, omega, w - Math.PI / 2);

    // Return the Cartesian state vector
    return new double[] { posRotated[0], posRotated[1], posRotated[2], velRotated[0], velRotated[1], velRotated[2] };
  }

  private static double[] rotate(double[] vec, double i, double omega, double w) {
    double x = vec[0];
    double y = vec[1];
    double cosI = Math.cos(i);
    double sinI = Math.sin(i);
    double cosOmega = Math.cos(omega);
    double sinOmega = Math.sin(omega);
    double cosW = Math.cos(w);
    double sinW = Math.sin(w);
    double[] rotated = new double[3];
    rotated[0] = x * (cosOmega * cosW - sinOmega * sinW * cosI) - y * (sinOmega * cosW + cosOmega * sinW * cosI);
    rotated[1] = x * (cosOmega * sinW + sinOmega * cosW * cosI) + y * (cosOmega * cosW * cosI - sinOmega * sinW);
    rotated[2] = x * sinW * sinI + y * cosW * sinI;
    return rotated;
  }

}
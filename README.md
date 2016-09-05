# opencl-montecarlo
Monte Carlo integration for 2d functions

This is a basic Monte Carlo integrator running on java using OpenCL (via JOCL).

To put it simply, Monte Carlo integration uses brute force to approximate the area of a function in a non-deterministic way.

Let's say you want to evaluate the area of the unit circle. You could put a boundary square of 1 unit around the circle which has the known area of 1. Then you generate a huge number of random points inside the boundary square, and evaluate each point to see if it's inside the circle, or not. If you compute the ratio of the points inside the circle and the number of total points, you'll get an approximation of the circle's area. Using more sample points will gain you more precision. The precision can be given by the following formula: 1/sqrt(n), (where n is the number of sample points), so to gain twice the precision, you'll have to use 4 times as much points. 
Of course a circle has a known figure for its area, but this method of integration can be used for more complex functions as well.

Volume of N dimensional functions can also estimated using this method, with the same precision formula described above (1/sqrt(n)), when traditional numeric integration methods increase in complexity a lot faster .

![alt tag](https://upload.wikimedia.org/wikipedia/commons/b/b0/MonteCarloIntegrationCircle.png)

To run the application run the jar file from the command line with:
'java -jar opencl-montecarlo.jar'

The application required an installed OpenCL driver. AMD's OpenCL driver can provide OpenCL support for both Linux and Windows, on any CPU that supports SSE2.

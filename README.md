# opencl-montecarlo
Monte Carlo integration for 2d functions

This is a simple Monte Carlo integrator running on java using OpenCL (via JOCL).

Monte Carlo integration uses brute force computation to approximate the area of a function in a non-deterministic way.

Let's say you want to evaluate the area of an arbitrary function, that is inside the 0.0-1.0 region in both x and y axes. You could put a boundary square of 1 unit around the function which has the known area of 1. Then you generate a huge number of random points inside the boundary square, and evaluate each point to see if it's under the function, or not. If you compute the ratio of the points under the function and the number of total points, you'll get an approximation of the function's area. Using more sample points will gain you more precision. The precision can be given by the following formula: 1/sqrt(n), (where n is the number of sample points), so to gain twice the precision, you'll have to use 4 times as much points. 

By evaluating a circle's area, an estimate of PI can be calculated. Volume of N dimensional functions can also estimated using this method, with the same precision formula described above (1/sqrt(n)), when traditional numeric integration methods increase in complexity a lot faster.

![alt tag](https://upload.wikimedia.org/wikipedia/commons/b/b0/MonteCarloIntegrationCircle.png)

The application runs from the command line, and provides options to view and select the available OpenCL devices in your system. Depending on your device's capabilities, double precision floating point numbers, and 64 bit integers can be used in the calculations.

To run the application run the jar file from the command line with:
'java -jar opencl-montecarlo.jar'

The application requires an installed OpenCL driver. AMD's OpenCL driver can provide OpenCL support for both Linux and Windows, on any CPU that supports SSE2.

package app;

import java.util.List;
import java.util.Scanner;

import bi106z.montecarlo.CalcResult;
import bi106z.montecarlo.Calculator;
import bi106z.montecarlo.shapes.CircleShape;
import bi106z.montecarlo.shapes.IShapeCalculator;
import bi106z.montecarlo.shapes.LinearShape;
import bi106z.montecarlo.shapes.SineShape;
import clframework.common.CLDevice;

public class Main {

	private static final String appVersion = "v0.8";
	
	public static void main(String[] args) {
		run();
	}

	private static void run() {
		List<CLDevice> deviceList = CLDevice.GetAllAvailableDevices();

		if (deviceList.size() == 0) {
			System.out.println("No OpenCL devices found! Please install appropriate drivers!");
			return;
		}

		int currentDeviceIdx = 0;
		CLDevice currentDevice = deviceList.get(currentDeviceIdx);
		Calculator calc = new Calculator(currentDevice);

		IShapeCalculator shape = new CircleShape();

		System.out.println("OpenCL Monte Carlo integrator " + appVersion);
		printHelp();
		System.out.println("");
		System.out.println("Enter command:");

		Scanner s = new Scanner(System.in);
		while (s.hasNext()) {
			String command = s.nextLine().toLowerCase();
			try {

				if (command.startsWith("listdevices")) {
					System.out.println("Available OpenCL devices:");
					for (int i = 0; i < deviceList.size(); i++) {
						System.out.println("" + (i + 1) + ": " + deviceList.get(i).getDeviceName());
					}
				} else if (command.startsWith("showdevice")) {
					System.out.println("Device platform: " + currentDevice.getPlatformName());
					System.out.println("Device name: " + currentDevice.getDeviceName());
					System.out.println("OpenCL version: " + currentDevice.getOpenCLVersion());
					System.out.println("Compute units: " + currentDevice.getComputeUnitCount());
					System.out.println("Compute unit speed: " + currentDevice.getClockFrequencyMhz() + " Mhz");
					System.out
							.println("Global memorysize: " + ((currentDevice.getGlobalMemory() / 1024) / 1024) + " MB");
					System.out.println(
							"64 bit float support: " + (currentDevice.isExtSupported("cl_khr_fp64") ? "Yes" : "No"));
					System.out.println("64 bit int atomics support: "
							+ (currentDevice.isExtSupported("cl_khr_int64_base_atomics") ? "Yes" : "No"));

				} else if (command.startsWith("precision ")) {
					String arg = command.replace("precision ", "");
					long prec;
					try {
						prec = Long.parseLong(arg.trim());
					} catch (Exception e) {
						throw new Exception(arg + " is not a number!");
					}
					calc.setPrecision(prec);
				} else if (command.startsWith("setshape ")) {
					String arg = command.replace("setshape ", "").trim();
					if (arg.equals("circle")) {
						shape = new CircleShape();
						System.out.println("Circle shape selected!");
					} else if (arg.equals("linear")) {
						shape = new LinearShape();
						System.out.println("Linear shape selected!");
					} else if (arg.equals("sin") || arg.equals("sine")) {
						shape = new SineShape();
						System.out.println("Sine shape selected!");
					} else {
						System.out.println("Invalid shape: '" + arg + "'!");
					}
				} else if (command.startsWith("usedouble ")) {
					String arg = command.replace("usedouble ", "");
					if (arg.trim().toLowerCase().equals("true"))
						calc.setUseDouble(true);
					else
						calc.setUseDouble(false);
					System.out.println(calc.isUseDouble() ? "64-bit floats will be used if available!"
							: "32-bit floats will be used!");
				} else if (command.startsWith("changedevice ")) {
					String arg = command.replace("changedevice ", "");
					int did;
					try {
						did = Integer.parseInt(arg) - 1;
					} catch (Exception e) {
						throw new Exception(arg + " is not a number!");
					}
					if (did < 0 || did >= deviceList.size())
						throw new Exception("There is no device with the given id!");

					currentDeviceIdx = did;
					currentDevice = deviceList.get(did);
					if (calc != null)
						calc.delete();
					calc = new Calculator(currentDevice);
					System.out.println("Compute device changed to: " + currentDevice.getDeviceName());
				} else if (command.startsWith("calculate")) {
					String prec = command.replace("calculate", "");
					try {
						calc.setPrecision(Long.parseLong(prec.trim()));
					} catch (Exception e) {
					}

					System.out.println("Running calculations with " + calc.getPrecision() * 16 + " iterations...");
					long time1 = System.nanoTime();
					CalcResult result = calc.Calculate(shape);
					long time2 = System.nanoTime();
					System.out.println("Calculation finished!");
					System.out.println(result.GetResult());
					System.out.println("Calculation time = " + ((time2 - time1) / 1000000) + "ms");
				} else if (command.startsWith("help")) {
					printHelp();
				} else if (command.startsWith("exit")) {
					System.exit(0);
				} else {
					System.out.println("Type 'help' to get a list of available commands!");
					throw new Exception("No such command");
				}

			} catch (Exception e) {
				System.out.println("Error: " + e.getMessage());
			}
			System.out.println("");
			System.out.println("Enter command:");
		}
		s.close();
	}

	private static void printHelp() {
		System.out.println("Available commands:");
		System.out.println("setshape <shape>                		Sets the shape. Valid entires are: circle, linear, sin");
		System.out.println(
				"calculate [precision]        			Calculates Pi with the given precision. Each precision adds 24 iterations. Default precision is 8192");
		System.out.println(
				"precision <precision>				Sets the precision to the given value. Each precision adds 24 iterations");
		System.out.println(
				"useDouble <true or false>			Sets if 64 bit float numbers should be used (if available on device). Default: false");
		System.out.println("listdevices              			Lists all available OpenCL devices");
		System.out
				.println("changedevice <deviceid>  			Switches the currently used device. e.g: changedevice 3");
		System.out.println("showdevice               			Prints info about current device");
		System.out.println("help                     			Prints this help");
		System.out.println("exit                     			Exits");
		System.out.println("");
	}
}
package bi106z.montecarlo;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import org.jocl.Pointer;
import org.jocl.Sizeof;
import org.jocl.cl_mem;

import bi106z.montecarlo.shapes.IShapeCalculator;
import clframework.common.CLContext;
import clframework.common.CLDevice;
import clframework.common.CLKernel;
import clframework.common.CLSourceLoader;
import clframework.common.MemObject;

import static org.jocl.CL.CL_DEVICE_NAME;
import static org.jocl.CL.CL_DEVICE_VENDOR;
import static org.jocl.CL.CL_DEVICE_VERSION;
import static org.jocl.CL.CL_MEM_COPY_HOST_PTR;
import static org.jocl.CL.CL_MEM_READ_ONLY;
import static org.jocl.CL.CL_MEM_WRITE_ONLY;
import static org.jocl.CL.CL_SUCCESS;
import static org.jocl.CL.CL_TRUE;

public class Calculator {

	private static boolean VERBOSE = true;
	
	private CLDevice device;
	private long precision = 8192;

	private static final int rndDataSize = 65535 * 64; //16MB/32MB random data
	private float[] floatRndData;
	private double[] doubleRndData;

	private boolean useDouble = false;
	
	private long[] g_work_item_sizes = new long[1];
	
	private IShapeCalculator shape = null;
	
	private CLContext clContext;

	public Calculator(CLDevice _device) {
		device = _device;
		floatRndData = new float[rndDataSize];
		doubleRndData = new double[rndDataSize];

		Random r = new Random(System.currentTimeMillis());

		for (int i = 0; i < rndDataSize; i++) {
			floatRndData[i] = r.nextFloat();
			doubleRndData[i] = r.nextDouble();
		}
		
		clContext = new CLContext(device);
	}
	
	public void delete()	{
		clContext.delete();
	}

	public boolean isUseDouble() {
		return useDouble;
	}

	public void setUseDouble(boolean useDouble) {
		this.useDouble = useDouble;
	}

	public long getPrecision() {
		return precision;
	}

	public void setPrecision(long precision) {
		this.precision = precision;
	}

	public CalcResult Calculate(IShapeCalculator shape) throws Exception {
		if (device == null)
			throw new Exception("Invalid device given!");

		final int threadWorkSize = 24; //One thread will calculate this much tries
		final long totalTries = precision * threadWorkSize;
		boolean use64bitAtomics = false;
		
		if (useDouble && !device.isExtSupported("cl_khr_fp64")) {
			System.out
					.println("64 bit floats are not supported on the selected device! Falling back to 32 bit floats.");
			useDouble = false;
		}
		
		if ( totalTries >= Integer.MAX_VALUE )
		{
			if ( device.isExtSupported("cl_khr_int64_base_atomics"))
				use64bitAtomics = true;
			else
				throw new Exception("A device with 64 bit integer atomics capability is required to calculate with the defined precision!");
		}

		if (VERBOSE && useDouble)
			System.out.println("Using 64-bit floats.");
		
		if (VERBOSE && use64bitAtomics)
			System.out.println("Using 64-bit integers.");

		HashMap<String, String> params = new HashMap<String, String>();
		params.put("%64bit_float_ext%", useDouble ? "#pragma OPENCL EXTENSION cl_khr_fp64 : enable" : "");
		params.put("%numberformat%", useDouble ? "double" : "float");
		params.put("%64bit_atomic_int_ext%", use64bitAtomics ?"#pragma OPENCL EXTENSION cl_khr_int64_base_atomics : enable" : "");
		params.put("%use64bitatomics%", use64bitAtomics ? "#define USE_64BIT_ATOMICS" : "");
		params.put("%threaditercount%", Integer.toString(threadWorkSize));
		params.put("%rnddatasize%", Integer.toString(rndDataSize));
		params.put("%shapefunc%", shape.GetFunction());

		String programSrc = CLSourceLoader.getLocalSource("/clsrc/picalc.cl", params);
		CLKernel clKernel = new CLKernel(clContext, new String[] { programSrc },
				new String[] { "picalc" }, "-cl-fast-relaxed-math -cl-mad-enable", VERBOSE ? new BuildEventPrinter() : null);

		
		g_work_item_sizes[0] = precision;

		List<MemObject> parameters = new ArrayList<MemObject>(3);

		int[] seed_i = new int[] { new Random(System.currentTimeMillis()).nextInt(rndDataSize) };
		int[] initialData_i = new int[]{0};
		long[] seed_l = new long[] { new Random(System.currentTimeMillis()).nextLong() % rndDataSize };
		long[] initialData_l = new long[]{0};
		
		MemObject result = null;

		try {
			if (useDouble)
				parameters.add(MemObject.createMemObjectReadOnly(clContext, Sizeof.cl_double * rndDataSize,
						Pointer.to(doubleRndData)));
			else
				parameters.add(MemObject.createMemObjectReadOnly(clContext, Sizeof.cl_float * rndDataSize,
						Pointer.to(floatRndData)));

			if ( use64bitAtomics)
			{
				parameters.add(MemObject.createMemObjectReadOnly(clContext, Sizeof.cl_long * seed_l.length, Pointer.to(seed_l)));
				result = MemObject.createMemObjectReadWrite(clContext, Sizeof.cl_long, Pointer.to(initialData_l));
			}
			else
			{
				parameters.add(MemObject.createMemObjectReadOnly(clContext, Sizeof.cl_int * seed_i.length, Pointer.to(seed_i)));
				result = MemObject.createMemObjectReadWrite(clContext, Sizeof.cl_int, Pointer.to(initialData_i));
			}
			
			parameters.add(result);
		} catch (Exception e) {
			throw new Exception("Failed to create memory objects! " + e.getMessage());
		}

		clKernel.enqueueNDRangeKernel("picalc", parameters, g_work_item_sizes, null, null);

		int[] hitCount_i = new int[] { 0 };
		long[] hitCount_l = new long[] { 0 };

		result.ReadBufferWithBlocking( use64bitAtomics ? Pointer.to(hitCount_l) : Pointer.to(hitCount_i));

		long hitCount = use64bitAtomics ? hitCount_l[0] : hitCount_i[0];
		
		parameters.clear();

		clKernel.delete();

		if ( VERBOSE )
		{
			NumberFormat percFormatter = new DecimalFormat("#0.00%");   
			System.out.println("Hitcount: " + hitCount + " of " + totalTries + " (" + percFormatter.format((double)hitCount / (double)totalTries) + ")");
		}
		shape.OnResult(hitCount, totalTries);
		
		return new CalcResult(shape);
	}
}

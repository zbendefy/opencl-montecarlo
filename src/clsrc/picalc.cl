%64bit_float_ext%
%64bit_atomic_int_ext%

typedef %numberformat% RealNumber;

inline bool isInsideShape(const RealNumber x, const RealNumber y)
{
	%shapefunc%
}

inline RealNumber GetRandom(__global const RealNumber* rndData, const long dataSize, long offset, long seed)
{
	return rndData[(offset + seed) % dataSize];
}

#define THREAD_ITER_COUNT %threaditercount%
#define RANDOM_DATA_SIZE %rnddatasize%
%use64bitatomics%

#ifdef USE_64BIT_ATOMICS
typedef long ResultCounterType;
#else
typedef int ResultCounterType;
#endif

//kernels are distributed in 1 dimension. 1 thread group has the size of 32. One thread will calculate 8 iterations
__kernel void picalc(__global const RealNumber* rndData, __constant long* seed, __global ResultCounterType* output) 
{ 
	__local int localHitCount;
	const size_t globalId = get_global_id(0);
	const size_t localId = get_local_id(0);
	
	if ( localId == 0 )
		localHitCount = 0;	

	barrier( CLK_LOCAL_MEM_FENCE );
	
	#pragma unroll THREAD_ITER_COUNT
	for( int i = 0; i < THREAD_ITER_COUNT; ++i )
	{
		const RealNumber rnd_x = GetRandom(rndData, RANDOM_DATA_SIZE, globalId * THREAD_ITER_COUNT * 2 + i, *seed); 
		const RealNumber rnd_y = GetRandom(rndData, RANDOM_DATA_SIZE, globalId * THREAD_ITER_COUNT * 2 + i + 1, *seed);
		
		if ( isInsideShape(rnd_x, rnd_y) )
			atomic_inc(&localHitCount);
	}
	
	barrier( CLK_LOCAL_MEM_FENCE );
	
	if ( localId == 0 )
	{
#ifdef USE_64BIT_ATOMICS
		atom_add(output, localHitCount);
#else
		atomic_add(output, localHitCount);
#endif
	}
}
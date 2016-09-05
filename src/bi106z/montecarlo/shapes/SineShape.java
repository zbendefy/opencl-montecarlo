package bi106z.montecarlo.shapes;

public class SineShape implements IShapeCalculator {

	private double hitRatio;
	
	@Override
	public String GetFunction() {
		return "return y < sin( x * 3.14159265359 );";
	}

	@Override
	public void OnResult(long hitCount, long tries) {
		hitRatio = (double)hitCount / (double)tries;
	}

	@Override
	public String GetResult() {
		return "Area: " + (hitRatio * Math.PI) + System.getProperty("line.separator");
	}

	
}

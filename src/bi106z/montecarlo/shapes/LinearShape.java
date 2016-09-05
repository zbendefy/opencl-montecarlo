package bi106z.montecarlo.shapes;

public class LinearShape implements IShapeCalculator {

	private double hitRatio;
	
	@Override
	public String GetFunction() {
		return "return y < x;";
	}

	@Override
	public void OnResult(long hitCount, long tries) {
		hitRatio = (double)hitCount / (double)tries;
	}

	@Override
	public String GetResult() {
		return "Area: " + hitRatio;
	}

	
}

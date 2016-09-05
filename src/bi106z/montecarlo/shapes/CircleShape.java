package bi106z.montecarlo.shapes;

public class CircleShape implements IShapeCalculator {

	private double hitRatio;
	
	@Override
	public String GetFunction() {
		return "return x*x + y*y < 1.0;";
	}

	@Override
	public void OnResult(long hitCount, long tries) {
		hitRatio = (double)hitCount / (double)tries;
	}

	@Override
	public String GetResult() {
		return "Area: " + hitRatio + System.getProperty("line.separator") + "Pi: " + hitRatio * 4.0;
	}

	
}

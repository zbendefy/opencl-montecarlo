package bi106z.montecarlo.shapes;

public interface IShapeCalculator {

	public String GetFunction();
	public void OnResult(long hitCount, long tries);
	public String GetResult();
}

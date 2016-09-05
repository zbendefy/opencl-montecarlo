package bi106z.montecarlo;

import bi106z.montecarlo.shapes.IShapeCalculator;

public class CalcResult {

	private IShapeCalculator shape;

	public CalcResult(IShapeCalculator shape) {
		super();
		this.shape = shape;
	}

	public String GetResult(){
		return shape.GetResult();
	}
}

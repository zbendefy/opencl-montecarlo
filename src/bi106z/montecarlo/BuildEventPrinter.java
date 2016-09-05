package bi106z.montecarlo;

import clframework.common.IBuildEventListener;

public class BuildEventPrinter implements IBuildEventListener {

	private static String sep = System.getProperty("line.separator");
	
	@Override
	public void ShowBuildLog(String log) {
		System.out.println("Build log: " + sep  + log + sep);
	}

}

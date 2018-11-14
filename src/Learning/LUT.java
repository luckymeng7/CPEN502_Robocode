package Learning;

import java.io.File;
import java.io.IOException;

import Interface.LUTInterface;
/*
 * May not develop, if so, LUTInterface and this file could be removed
 */
public class LUT implements LUTInterface {

	public LUT (
 		int argNumInputs,
 		int [] argVariableFloor,
 		int [] argVariableCeiling ) {
		
		
	}

	@Override
	public double[] outputFor(double[] X) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public double train() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void save(File argFile) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void load(String argFileName) throws IOException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void initialiseLUT() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public int indexFor(double[] X) {
		// TODO Auto-generated method stub
		return 0;
	}

}

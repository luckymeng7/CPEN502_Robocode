package NeuralNet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.junit.Before;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

public class UnitTest {
	//Unipolar

	LUTNeuralNet testNeuronNetWorks = new LUTNeuralNet();
	Neuron testNeuron = new Neuron("test");
	@Before
	public void setUp() throws Exception {

	}

/*	@Disabled
	@Test
	public void runCoverge() throws IOException {
		testNeuronNetWorks.tryConverge(100000, 0.05);
		testNeuronNet.printRunResults(testNeuronNet.getErrorArray(),"1c_result.csv");
	}
	
	@Disabled
	@Test
	public void testBias() {
		double bias = testNeuronNet.getBias();
		System.out.println(bias);
	}
	
	@Disabled
	@Test
	public void outputFiles () throws IOException {
		//testNeuronNet.tryConverge(100000, 0.05);
		//testNeuronNet.printRunResults(testNeuronNet.getErrorArray(),"1b_result.csv");
		int average = testNeuronNet.averageConverageNum(500, 10000, 0.05);
		System.out.println("Average epochs number: " + average + " to reach total error less than 0.05");
		testNeuronNet.printEachTrail(testNeuronNet.getEpocNumArray(),"1b_result_epoch.csv");
	}*/
	
}


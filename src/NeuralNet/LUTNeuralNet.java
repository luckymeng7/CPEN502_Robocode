package NeuralNet;

import Learning.*;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

public class LUTNeuralNet {
	
	/***Test Data*****/	
	private static int numStateCategory = 6;
	private static int numInput = numStateCategory;
	private static int numHidden = 40;
	private static int numOutput = 1;	
	private static double expectedOutput[][]; //numStates*numActions
	private static double learningRate = 0.005;
	private static double momentumRate = 0.9;
	private static double lowerBound = -1.0;
	private static double upperBound = 1.0;
	private static double []maxQ = new double[Action.NumRobotActions];
	private static double []minQ = new double[Action.NumRobotActions];

	private static ArrayList<Double> errorInEachEpoch;
	private static ArrayList<NeuralNet> neuralNetworks;

	
	Neuron testNeuron = new Neuron("test");
	
	public static void main(String[] args){
		QTable lut = new QTable();
		//File file = new File("E:\\Work\\java\\RoboCode_RL_NN\\LUT.dat");
		File file = new File("LUT.dat");
		lut.loadData(file);
		double inputData[][] = new double [State.NumStates][numStateCategory];
		double normExpectedOutput[][][] = new double [Action.NumRobotActions][State.NumStates][numOutput];
		expectedOutput = lut.getTable();
		//System.out.println(Arrays.deepToString(expectedOutput));
/*		int index = States.getStateIndex(2, 5, 3,1,1, 0);
		int [] states = States.getStateFromIndex(index);
		double [] normstates = normalizeInputData(states);
		System.out.println(Arrays.toString(normstates));*/
		//System.out.println(Double.toString(normalizeExpectedOutput(110,maxQ,minQ,upperBound,lowerBound)));

		for(int act = 0; act<Action.NumRobotActions;act++) {
			maxQ[act] = findMax(getColumn(expectedOutput,act));
			minQ[act] = findMin(getColumn(expectedOutput,act));
			//maxQ[act] = 120;
			//minQ[act] = -20;
		}
		
		for(int stateid = 0; stateid < State.NumStates; stateid++) {
			int[]state = State.getStateFromIndex(stateid);
			inputData[stateid] = normalizeInputData(state);
			for(int act = 0; act < Action.NumRobotActions; act++) {
				normExpectedOutput[act][stateid][numOutput-1] =normalizeExpectedOutput(expectedOutput[stateid][act],maxQ[act],minQ[act],upperBound,lowerBound);
			}
		}
		neuralNetworks = new ArrayList<NeuralNet>();
/*		NeuralNet testNeuronNet = new NeuralNet(numInput,numHidden,numOutput,learningRate,momentumRate,lowerBound,upperBound,6); //Construct a new neural net object
		try {
			tryConverge(testNeuronNet,inputData,normExpectedOutput[6],10000, 0.13);//Train the network with step and error constrains
			testNeuronNet.printRunResults(errorInEachEpoch, "bipolarMomentum.csv");
			//File file = new File("Weight_"+testNeuronNet.getNetID()+".txt");
			//file.createNewFile();
			//testNeuronNet.save(file);
			}
			catch(IOException e){
				System.out.println(e);
			}*/	
		
		for(int act = 0; act < Action.NumRobotActions; act++) {
			int average = EpochAverage(act,inputData,normExpectedOutput[act],0.000002,10000,1);
			System.out.println(act+"The average of number of epoches to converge is: "+average+"\n");
		}
		
		for(NeuralNet net : neuralNetworks) {
			try {
					File weight = new File("Weight_"+net.getNetID()+".dat");
					weight.createNewFile();
					net.save(weight);
			}catch(IOException e) {
				System.out.println(e);
			}
		}
		
		System.out.println("Test ends here");
		
	}
	
	public static double [] normalizeInputData(int [] states) {
		double [] normalizedStates = new double [6];
		for(int i = 0; i < 6; i++) {
			switch (i) {
			case 0:
				normalizedStates[0] = -1.0 + ((double)states[0])*2.0/((double)(State.NumHeading-1));
				break;
			case 1:
				normalizedStates[1] = -1.0 + ((double)states[1])*2.0/((double)(State.NumTargetDistance-1));;
				break;
			case 2:
				normalizedStates[2] = -1.0 + ((double)states[2])*2.0/((double)(State.NumTargetBearing-1));;
				break;
			case 3:
				normalizedStates[3] = -1.0 + ((double)states[3])*2.0;
				break;
			case 4:
				normalizedStates[4] = -1.0 + ((double)states[4])*2.0;
				break;
			case 5:
				normalizedStates[5] = -1.0 + ((double)states[5])*2.0;
				break;
			default:
				System.out.println("The data doesn't belong here.");
			}
		}
		return normalizedStates;
	}
	
	public static double normalizeExpectedOutput(double expected, double max, double min, double upperbound, double lowerbound){
		double normalizedExpected = 0.0;
		if(expected > max) {
			expected = max;
		}else if(expected < min) {
			expected = min;
		}
		
			normalizedExpected = lowerbound +(expected-min)*(upperbound-lowerbound)/(max - min);
		
		
		return normalizedExpected;
	}
	
	public static double  remappingOutputToQ (double output, double max, double min, double upperbound, double lowerbound) {
		double remappedQ = 0.0;
		remappedQ = min + (output-lowerbound)*(max-min)/(upperbound - lowerbound);		
		return remappedQ;
	}
	
	/***
	 * This function calculates the average of amount of epoch that one trial of network training takes, 
	 * it take in parameters of a neural network and returns the average of epoch number
	 * @param momentum
	 * @param lowerbound
	 * @param upperbound
	 * @param input
	 * @param expected
	 * @param minError
	 * @param maxSteps
	 * @param numTrials
	 * @return the average of number of epochs
	 */
	public static int EpochAverage(int act,double[][] input, double[][] expected,double minError, int maxSteps, int numTrials) {
		int epochNumber, failure,success;
		double average = 0f;
		epochNumber = 0;
		failure = 0;
		success = 0;
		NeuralNet testNeuronNet = null;
		for(int i = 0; i < numTrials; i++) {
			testNeuronNet = new NeuralNet(numInput,numHidden,numOutput,learningRate,momentumRate,lowerBound,upperBound,act ); //Construct a new neural net object
			tryConverge(testNeuronNet,input,expected,maxSteps, minError);//Train the network with step and error constrains
			epochNumber = getErrorArray().size(); //get the epoch number of this trial.
			try {
				printRunResults(getErrorArray(), "Error_for_act_"+act+"_trail_"+numTrials+".csv");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if( epochNumber < maxSteps) {
				average = average +  epochNumber;
				success ++; 
			}
			else {
				failure++;
			}			
		}
		double convergeRate = 100*success/(success+failure);
		System.out.println("The net converges for "+convergeRate+" percent of the time.\n" );
		average = average/success;
		neuralNetworks.add(testNeuronNet);		
		return (int)average;		
	}	
	/**
	 * This method run train for many epochs till the NN converge subjects to the max step constrain.	
	 * @param maxStep
	 * @param minError
	 */
	public static void tryConverge(NeuralNet theNet, double[][] input, double [][] expected,int maxStep, double minError) {
		int i;
		double totalerror = 1;
		double previousError = 0; 
		errorInEachEpoch = new ArrayList<>();
		for(i = 0; i < maxStep && Math.abs(totalerror-previousError) > minError; i++) {
			previousError = totalerror;
			totalerror = 0.0;
			for(int j = 0; j < input.length; j++) {
				totalerror += theNet.train(input[j],expected[j]);				
			}
			//totalerror = totalerror*0.5;
			totalerror = Math.sqrt(totalerror/input.length);
			errorInEachEpoch.add(totalerror);
			//System.out.println("totalerror: " + totalerror);
			//System.out.println("previousError: " + previousError);
		}
		System.out.println("Sum of squared error in last epoch = " + totalerror);
		System.out.println("Number of epoch: "+ i + "\n");
		if(i == maxStep) {
			System.out.println("Error in training, try again!");
		}
		
	}
	
	
	public static void printRunResults(ArrayList<Double> errors, String fileName) throws IOException {
		int epoch;
		PrintWriter printWriter = new PrintWriter(new FileWriter(fileName));
		printWriter.printf("Epoch Number, Total Squared Error, \n");
		for(epoch = 0; epoch < errors.size(); epoch++) {
			printWriter.printf("%d, %f, \n", epoch, errors.get(epoch));
		}
		printWriter.flush();
		printWriter.close();
	}
	
	public static ArrayList <Double> getErrorArray(){
		return errorInEachEpoch;
	} 
	
	public static void setErrorArray(ArrayList<Double> errors) {
		errorInEachEpoch = errors;
	}

	public static double findMax (double []theValues) {
		double maxQValue = theValues[0];
		int maxIndex = 0;
		for (int i = 0; i < theValues.length; i++) {
			if(maxQValue < theValues[i]) {
				maxQValue = theValues[i];
				maxIndex = i;
			}
		}
		return maxQValue;
	}
	
	public static double findMin (double []theValues) {
		double minQValue = theValues[0];
		int minIndex = 0;
		for (int i = 0; i < theValues.length; i++) {
			if(minQValue > theValues[i]) {
				minQValue = theValues[i];
				minIndex = i;
			}
		}
		return minQValue;
	}
	
	public static double[] getColumn(double[][] array, int index) {
		double[] column = new double[State.NumStates];
		for(int i = 0; i< column.length; i++ ) {
			column[i] = array[i][index];
		}
		return column;
	}
	
	public static int getNumInput() {
		return numInput;
	}
	
	public static int getNumHidden() {
		return numHidden;
	}
	
	public static int getNumOutput() {
		return numOutput;
	}
	
	public static double getLearningRate() {
		return learningRate;
	}
	
	public static double getMomentumRate() {
		return momentumRate;
	}
}

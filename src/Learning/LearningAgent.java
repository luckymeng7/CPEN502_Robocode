package Learning;
import java.util.ArrayList;
import java.util.Random;
//import java.io.File;
//import java.io.IOException;

import NeuralNet.*;

public class LearningAgent {

	public static final double discountRate = 0.9;   // gamma
	public static double explorationRate = 0.3;   
	private int currentState;   
	private int currentAction;   
	private boolean firstRound = true;   
	private QTable table;  
	
	/***Test Data*****/	
	private static int numStateCategory = 6;
	private static int numInput = numStateCategory;
	private static int numHidden = LUTNeuralNet.getNumHidden();
	private static int numOutput = LUTNeuralNet.getNumoutput();	
	private static double learningRate_NN = LUTNeuralNet.getLearningRate(); // alpha
	private static double learningRate = 0.2; // alpha
	private static double momentumRate = LUTNeuralNet.getMomentumRate();
	private static double lowerBound = -1.0;
	private static double upperBound = 1.0;
	
	private double []maxQ = new double[Action.NumRobotActions];
	private double []minQ = new double[Action.NumRobotActions];
	
	
	private int[] currentStateArray = new int [numStateCategory];
	private int[] newStateArray = new int [numStateCategory];
	private double [] currentActionOutput  = new double [Action.NumRobotActions];
	private double [] newActionOutput = new double[Action.NumRobotActions];
	private double [] currentActionQ  = new double[Action.NumRobotActions];
	private double [] newActionQ = new double[Action.NumRobotActions];
	
	private ArrayList<NeuralNet> neuralNetworks = new ArrayList<NeuralNet>();
	public LearningAgent (QTable table) {
		this.table = table;
		for (int act = 0; act<Action.NumRobotActions; act++) {
			maxQ[act] = 120;
			minQ[act] = -20;
		}
	}
		
	// Off-policy
	public void QLearn (int nextState, int nextAction, double reward) {
		double oldQ;
		double newQ;
		if (firstRound) {
			firstRound = false;
		} else {
			oldQ = table.getQValue(currentState, currentAction);
			newQ = oldQ + learningRate*(reward + discountRate * table.getMaxQValue(nextState)-oldQ);
			table.setQvalue(currentState, currentAction, newQ);
			
		}
		currentState = nextState;
		currentAction = nextAction;

	}
	
	// On-policy
	public void SARSALearn (int nextState, int nextAction, double reward) {
		double oldQ;
		double newQ;
		if (firstRound) {
			firstRound = false;
		} else {
			oldQ = table.getQValue(currentState, currentAction);
			newQ = oldQ + learningRate*(reward + discountRate * table.getQValue(nextState, nextAction)-oldQ);
			table.setQvalue(currentState, currentAction, newQ);
			
		}
		currentState = nextState;
		currentAction = nextAction;
	}
	
	// e-greedy
	public int selectAction(int state, boolean isOnline) {
		double thres = Math.random();
		int action = 0;
		double [] inputData = normalizeInputData(currentStateArray);
		if (thres < explorationRate) {
			// take exploration move
			Random ran = new Random();
			action = ran.nextInt(Action.NumRobotActions);
		} else {
			// take greedy move
			if (isOnline) {
				// Send input into 7 NeuralNet and get 7 output
				for(NeuralNet theNet : neuralNetworks) {
					int act = theNet.getNetID();
					double currentNetOutput = theNet.outputFor(inputData)[0];
					double currentNetQValue = LUTNeuralNet.inverseMappingOutput(currentNetOutput, maxQ[act], minQ[act], upperBound, lowerBound);//Reverse map output to big scale
					int currentNetIndex = theNet.getNetID();
					setCurrentActionValue(currentNetOutput,currentNetIndex);//Probably wrong
					setCurrentQValue(currentNetQValue,currentNetIndex);
				}
				action = getMaxIndex(currentActionQ);
			} else {
				action = table.getBestAction(state);
			}
		}
		return action;
	}
	
			
	
	// Get state (Current state)
	// Choose action
	// Send Current State into 7 NN 
	// Get 7 output
	//  Remaping to Q (Current)
	// Choose the max action

	// Execute action
	// Get new state 
	// Send into 7 NN
	// Get 7 output
	// Choose max OUTPUT (new)
	// Remap max OUTPUT to Q -> Q'
	// Calculate the expected Q
	// Use expected Q to do backpropogation 
	public void nn_QLearn( int action, double reward) {
		//Need to make currentData Array and new Data array is set before calling this function
		double currentStateQValue = getCurrentQValues()[action] ;
		double [] newInputData = new double[numStateCategory];
		newInputData = LUTNeuralNet.normalizeInputData(getNewStateArray());
		for(NeuralNet theNet: neuralNetworks) {
			int act = theNet.getNetID();
			double tempOutput = theNet.outputFor(newInputData)[0];
			double tempQValue = LUTNeuralNet.inverseMappingOutput(tempOutput, maxQ[act], minQ[act], upperBound, lowerBound);
			setNewActionValue(tempOutput,theNet.getNetID());
			setNewQValue(tempQValue,theNet.getNetID());
		}//Update the NewActionValue and newQValues Arrays
		
		int maxNewStateActionIndex = getMaxIndex(getNewActionValues());
		double maxNewQValue = getNewQValues()[maxNewStateActionIndex];
		double expectedQValue = currentStateQValue + learningRate*(reward + discountRate *maxNewQValue -currentStateQValue); 
		double [] expectedOutput = new double[1];
		expectedOutput[0] = LUTNeuralNet.normalizeExpectedOutput(expectedQValue, maxQ[action], minQ[action], upperBound, lowerBound);
		NeuralNet learningNet = neuralNetworks.get(action);
		double [] currentInputData = LUTNeuralNet.normalizeInputData(getNewStateArray());
		learningNet.train(currentInputData, expectedOutput);
	}

	public void onlineLearn (int action, double reward) {
		double []inputData = new double [numStateCategory];
		// Get current output , duplicated steps from select action
		/*inputData = normalizeInputData(currentStateArray);
		for (NeuralNet net: neuralNetworks) {
			currentActionOutput[net.getNetID()] = net.outputFor(inputData)[0];
		}*/
		// Map current output to current Q
		double currentQ = currentActionQ[action];
		
		// Send input into 7 NeuralNet and get 7 output
		inputData = normalizeInputData(newStateArray);
		for (NeuralNet net: neuralNetworks) {
			newActionOutput[net.getNetID()] = net.outputFor(inputData)[0];
			newActionQ[net.getNetID()] = remappingOutputToQ(newActionOutput[net.getNetID()], maxQ[net.getNetID()], minQ[net.getNetID()], upperBound, lowerBound);
		}
		// Choose max new output
		double maxNewQ = newActionQ[0];
		//int maxOutputIndex = 0;
		for (int act = 0; act < Action.NumRobotActions ; act++) {
			if (maxNewQ < newActionQ[act]) {
				maxNewQ = newActionQ[act];
				//maxOutputIndex = act;
			}
		}		
		
		// Expected Q
		double expectedQ;
		expectedQ = currentQ + learningRate*(reward + discountRate*maxNewQ-currentQ);
		
		// Update the weight for chosen action's network
		double []expectedOutput = new double[numOutput];
		expectedOutput[0] = normalizeExpectedOutput(expectedQ, maxQ[action], minQ[action], upperBound, lowerBound);
		neuralNetworks.get(action).train(normalizeInputData(currentStateArray), expectedOutput);
	}
	
	public void setCurrentStateArray (int state) {
		currentStateArray = State.getStateFromIndex(state);
	}
	public void setNewStateArray (int state) {
		newStateArray = State.getStateFromIndex(state);
	}
	
	public void initializeNeuralNetworks(){
		for(int i = 0; i < Action.NumRobotActions; i++) {
			NeuralNet theNewNet = new NeuralNet(numInput,numHidden,numOutput,learningRate_NN,momentumRate,lowerBound,upperBound,i);
			neuralNetworks.add(theNewNet);
		}
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
		double localExpected = expected;
		if(localExpected > max) {
			localExpected = max;
		}else if(localExpected < min) {
			localExpected = min;
		}
		
			normalizedExpected = lowerbound +(localExpected-min)*(upperbound-lowerbound)/(max - min);
		
		
		return normalizedExpected;
	}
	
	public static double  remappingOutputToQ (double output, double maxQ, double minQ, double upperbound, double lowerbound) {
		double remappedQ = 0.0;
		double currentOutput = output;
		if(currentOutput < -1.0) {
			currentOutput = -1.0;
		}else if(currentOutput > 1.0) {
			currentOutput = 1.0;
		}
		remappedQ = minQ + (currentOutput-lowerbound)/(upperbound-lowerbound)*(maxQ - minQ);		
		return remappedQ;
	}
	
	public static double[] getColumn(double[][] array, int index) {
		double[] column = new double[State.NumStates];
		for(int i = 0; i< column.length; i++ ) {
			column[i] = array[i][index];
		}
		return column;
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
	
	public int getMaxIndex(double [] theValues) {
		double maxQValue = theValues[0];
		int maxIndex = 0;
		for(int i = 0; i < theValues.length; i++) {
			if(maxQValue < theValues[i]) {
				maxQValue = theValues[i];
				maxIndex = i;
			}
		}
		return maxIndex;
	} 
	
	public void setCurrentActionValue(double theValues, int theIndex) {
		currentActionOutput[theIndex] = theValues;
	}
	public double [] getCurrentActionValues() {
		return this.currentActionOutput;
	}
	
	public void setCurrentQValues(double [] theValues) {
		currentActionQ = theValues;
	}
	public void setCurrentQValue(double theValues, int theIndex) {
		currentActionQ[theIndex] = theValues;
	}
	
	public double [] getCurrentQValues() {
		return this.currentActionQ;
	}

	public void setNewQValues(double [] theValues) {
		newActionQ = theValues;
	}
	public void setNewQValue(double theValues, int theIndex) {
		newActionQ[theIndex] = theValues;
	}
	public double [] getNewQValues() {
		return this.newActionQ;
	}
	
	public int [] getNewStateArray(){
		return this.newStateArray;
	}
	
	public void setCurrentActionValues(double [] theValues) {
		currentActionOutput = theValues;
	}

	
	public void setNewActionValues(double [] theValues) {
		newActionOutput = theValues;
	}
	public void setNewActionValue(double theValues, int theIndex) {
		newActionOutput[theIndex] = theValues;
	}
	public double [] getNewActionValues() {
		return this.newActionOutput;
	}
	public ArrayList<NeuralNet> getNeuralNetworks(){
		return this.neuralNetworks;
	}

}

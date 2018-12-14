package Learning;
import java.util.ArrayList;
import java.util.Random;
import java.io.File;
import java.io.IOException;

import NeuralNet.*;

public class LearningAgent {

	public static final double discountRate = 0.9;   // gamma
	public static double explorationRate = 0.2;   
	private int currentState;   
	private int currentAction;   
	private boolean firstRound = true;   
	private QTable table;  
	
	/***Test Data*****/	
	private static int numStateCategory = 6;
	private static int numInput = numStateCategory;
	private static int numHidden = 40;
	private static int numOutput = 1;	
	private static double learningRate = 0.005; // alpha
	private static double momentumRate = 0.9;
	private static double lowerBound = -1.0;
	private static double upperBound = 1.0;
	private static double maxQ = 120;
	private static double minQ = -20;

	public LearningAgent (QTable table) {
		this.table = table;
	}
	
	private int[] currentStateArray = new int [numStateCategory];
	private int[] newStateArray = new int [numStateCategory];
	private double currentActionOutput [] = new double [Action.NumRobotActions];
	private double newActionOutput[] = new double [Action.NumRobotActions];
	
	
	public ArrayList<NeuralNet> neuralNetworks = new ArrayList<NeuralNet>();
	
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
		
		if (thres < explorationRate) {
			// take exploration move
			Random ran = new Random();
			action = ran.nextInt(Action.NumRobotActions);
		} else {
			// take greedy move
			if (isOnline) {
				// Send input into 7 NeuralNet and get 7 output
				for (NeuralNet net: neuralNetworks) {
					currentActionOutput[net.getNetID()] = net.outputFor(normalizeInputData(currentStateArray))[0];
				}
				// Choose max output
				double maxOutput = currentActionOutput[0];
				int maxOutputIndex = 0;
				for (int act = 0; act < Action.NumRobotActions ; act++) {
					if (maxOutput < currentActionOutput[act]) {
						maxOutput = currentActionOutput[act];
						maxOutputIndex = act;
					}
				}
				action = maxOutputIndex;
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
	

	public void onlineLearn (int state, int action, double reward) {
		double []inputData = new double [numStateCategory];
		// Get current output 
		inputData = normalizeInputData(currentStateArray);
		/*for (NeuralNet net: neuralNetworks) {
			currentActionOutput[net.getNetID()] = net.outputFor(inputData)[0];
		}*/
		// Map current output to current Q
		double currentQ;
		currentQ = remappingOutputToQ(currentActionOutput[action], maxQ, minQ, upperBound, lowerBound);
		
		// Send input into 7 NeuralNet and get 7 output
		inputData = normalizeInputData(newStateArray);
		for (NeuralNet net: neuralNetworks) {
			newActionOutput[net.getNetID()] = net.outputFor(inputData)[0];
		}
		// Choose max output
		double maxOutput = newActionOutput[0];
		double maxOutputIndex = 0;
		for (int act = 0; act < Action.NumRobotActions ; act++) {
			if (maxOutput < newActionOutput[act]) {
				maxOutput = newActionOutput[act];
				maxOutputIndex = act;
			}
		}
		
		// Remap max Output to Q
		double newQ;
		newQ = remappingOutputToQ(maxOutput, maxQ, minQ, upperBound, lowerBound);
		
		// Expected Q
		double expectedQ;
		expectedQ = currentQ + learningRate*(reward + discountRate*newQ-currentQ);
		
		// Back Propagation
		double []expectedOutput = new double[numOutput];
		expectedOutput[0] = normalizeExpectedOutput(expectedQ, maxQ, minQ, upperBound, lowerBound);
		neuralNetworks.get(action).train(normalizeInputData(currentStateArray), expectedOutput);
	}
	
	public void setCurrentStateArray (int state) {
		currentStateArray = State.getStateFromIndex(state);
	}
	public void setNewStateArray (int state) {
		newStateArray = State.getStateFromIndex(state);
	}
	
	public void initializeNeuralNetworks () {
		for (int act = 0; act < Action.NumRobotActions; act++) {
			NeuralNet testNeuronNet = new NeuralNet(numInput,numHidden,numOutput,learningRate,momentumRate,lowerBound,upperBound,act);
			neuralNetworks.add(testNeuronNet);
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
	
	
}

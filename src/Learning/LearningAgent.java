package Learning;
import java.util.Random;

public class LearningAgent {

	public static final double LearningRate = 0.3;   // alpha
	public static final double DiscountRate = 0.9;   // gamma
	public static double explorationRate = 0.2;   
	private int currentState;   
	private int currentAction;   
	private boolean firstRound = true;   
	private QTable table;  
	
	public LearningAgent (QTable table) {
		this.table = table;
	}
	
	// Off-policy
	public void QLearn (int nextState, int nextAction, double reward) {
		double oldQ;
		double newQ;
		if (firstRound) {
			firstRound = false;
		} else {
			oldQ = table.getQValue(currentState, currentAction);
			newQ = oldQ + LearningRate*(reward + DiscountRate * table.getMaxQValue(nextState)-oldQ);
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
			newQ = oldQ + LearningRate*(reward + DiscountRate * table.getQValue(nextState, nextAction)-oldQ);
			table.setQvalue(currentState, currentAction, newQ);
			
		}
		currentState = nextState;
		currentAction = nextAction;
	}
			
	// Get state
	// Send into 7 NN
	// Get 7 output
	//  Remaping to Q
	// Choose the max action
	
	// Execute action
	// Get new state
	// Send into 7 NN
	// Get 7 output
	// Choose max OUTPUT (new)
	// Remap max OUTPUT to Q -> Q'
	// Calculate the expected Q
	// Use expected Q to do backpropogation 
	// 
	
	public void onlineLearn (int nextState, int nextAction) {
		
		
	}
	// e-greedy
	public int selectAction(int state) {
		double thres = Math.random();
		int action = 0;
		
		if (thres < explorationRate) {
			// take exploration move
			Random ran = new Random();
			action = ran.nextInt(Action.NumRobotActions);
		} else {
			// table greedy move
			action = table.getBestAction(state);
		}
		return action;
	}
}

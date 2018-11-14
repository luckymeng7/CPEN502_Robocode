package Learning;
import java.util.Random;

public class LearningAgent {

	public static final double LearningRate = 0.1;   // alpha
	public static final double DiscountRate = 0.9;   // gamma
	public static double explorationRate = 0.8;   
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
		//currentAction = nextAction;
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
	
	// e-greedy
	public int selectAction(int state) {
		double thres = Math.random();
		int action = 0;
		
		if (thres < explorationRate) {
			// take exploration move
			Random ran = new Random();
			action = ran.nextInt(Action.NumRobotActions -1);
		} else {
			// table greedy move
			action = table.getBestAction(state);
		}
		return action;
	}
}

package Banana;

import java.awt.Color;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;

import robocode.*;

import Learning.*;
// API help : http://robocode.sourceforge.net/docs/robocode/robocode/Robot.html
import NeuralNet.NeuralNet;

/**
 * CrazyBanana - a robot by xmli01
 */
public class CrazyBanana extends AdvancedRobot {
	
	public static final double PI = Math.PI;   
	private Target target;   
	private QTable table;   
	private LearningAgent agent;   
	private double reward = 0.0;   
	//private int isHitWall = 0;   
	private int isHitByBullet = 0;   
	
	private double oppoDist, oppoBearing;
	private boolean found = false;
	private int state, action, currentState; 
	
	private double rewardForWin=100;
	private double rewardForDeath=-20;
	private double accumuReward=0.0;
	
	private boolean interRewards = true;
	private boolean isSARSA = false;
	private boolean isOnline = true;
	private boolean isNaive = true;
	
	private int chosenState;
	private int chosenAction = 6;
	private double errorChosenToPrint;
	ArrayList<NeuralNet> templist = new ArrayList<NeuralNet>();
	
	/**
	 * run: CrazyBanana's default behavior
	 */   
	public void run() {
		// Initialization of the RL
		table = new QTable(); 
        agent = new LearningAgent(table);   
        target = new Target();   
        target.distance = 100000;
       

		// Initialization of the robot
		setAllColors(Color.red);
		setAdjustGunForRobotTurn(true); //Gun not Fix to body
		setAdjustRadarForGunTurn(true); // Radar not Fix to boby
		execute();
        
		 if(isOnline){
				agent.initializeNeuralNetworks();
				templist = agent.getNeuralNetworks();
				
				if(isNaive) {
					if(getRoundNum()>0) {
						for(NeuralNet theNet: agent.getNeuralNetworks()) {
							try {
								theNet.load(getDataFile("Weight_"+theNet.getNetID()+".dat"));
							} catch (IOException e) {
								e.printStackTrace();
							}
						}
					}
				}
				else {
					for(NeuralNet theNet: agent.getNeuralNetworks()) {
						try {
							theNet.load(getDataFile("Weight_"+theNet.getNetID()+".dat"));
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				}
				
				state = getState();//Get Initial State		
							
				
				while(true) {
					turnRadarRightRadians(2*PI);
					agent.setCurrentStateArray(state);
					action = agent.selectAction(state, isOnline);
					currentState = state;
					if (State.getHeading(getHeading()) == 2 ) chosenState = currentState;
					//chosenAction = action;
					switch(action) {
						case Action.RobotAhead:
							setAhead(Action.RobotMoveDistance);
							break;
						case Action.RobotBack:
							setBack(Action.RobotMoveDistance);
							break;
						case Action.RobotAheadTurnLeft:
							setAhead(Action.RobotMoveDistance);
							setTurnLeft(Action.RobotTurnDegree);
							break;
						case Action.RobotAheadTurnRight:
							setAhead(Action.RobotMoveDistance);
							setTurnRight(Action.RobotTurnDegree);
							break;
						case Action.RobotBackTurnLeft:
							setBack(Action.RobotMoveDistance);
							setTurnLeft(Action.RobotTurnDegree);
							break;
						case Action.RobotBackTurnRight:
							setBack(Action.RobotMoveDistance);
							setTurnRight(Action.RobotTurnDegree);
							break;
						case Action.RobotFire:
							ahead(0);
							turnLeft(0);
							scanAndFire();
							break;
						default:
							System.out.println("Action Not Found");
							break;					
					}	
					execute();					
					turnRadarRightRadians(2*PI);
					//Update states
					state = getState();
					agent.setNewStateArray(state);
					agent.nn_QLearn(currentState, action, reward);
					if (State.getHeading(getHeading()) == 2 && action == chosenAction) errorChosenToPrint = agent.getQError(currentState, action);
					accumuReward += reward;					
					//Reset Values
					reward = 0.0d;
					isHitByBullet = 0;
				}
			} else if(isSARSA) {		
				//Get Last State
				state = getState();
				turnRadarRightRadians(2*PI);
				action = agent.selectAction(state, isOnline);
				while(true) {									
					switch(action) {
						case Action.RobotAhead:
							setAhead(Action.RobotMoveDistance);
							break;
						case Action.RobotBack:
							setBack(Action.RobotMoveDistance);
							break;
						case Action.RobotAheadTurnLeft:
							setAhead(Action.RobotMoveDistance);
							setTurnLeft(Action.RobotTurnDegree);
							break;
						case Action.RobotAheadTurnRight:
							setAhead(Action.RobotMoveDistance);
							setTurnRight(Action.RobotTurnDegree);
							break;
						case Action.RobotBackTurnLeft:
							setBack(Action.RobotMoveDistance);
							setTurnLeft(Action.RobotTurnDegree);
							break;
						case Action.RobotBackTurnRight:
							setBack(Action.RobotMoveDistance);
							setTurnRight(Action.RobotTurnDegree);
							break;
						case Action.RobotFire:
							ahead(0);
							turnLeft(0);
							scanAndFire();
							break;
						default:
							System.out.println("Action Not Found");
							break;					
					}					
				execute();					
				turnRadarRightRadians(2*PI);
				//Update states
				state = getState();
				action = agent.selectAction(state, isOnline);
				agent.SARSALearn(state, action, reward);
				accumuReward += reward;
				
				//Reset Values
				reward = 0.0d;
				isHitByBullet = 0;
			}
		} else {
			state = getState();//Get Last State
			while(true) {
				//state = getState();//Get Last State
				turnRadarRightRadians(2*PI);					
				action = agent.selectAction(state,isOnline);					
				switch(action) {
					case Action.RobotAhead:
						setAhead(Action.RobotMoveDistance);
						break;
					case Action.RobotBack:
						setBack(Action.RobotMoveDistance);
						break;
					case Action.RobotAheadTurnLeft:
						setAhead(Action.RobotMoveDistance);
						setTurnLeft(Action.RobotTurnDegree);
						break;
					case Action.RobotAheadTurnRight:
						setAhead(Action.RobotMoveDistance);
						setTurnRight(Action.RobotTurnDegree);
						break;
					case Action.RobotBackTurnLeft:
						setBack(Action.RobotMoveDistance);
						setTurnLeft(Action.RobotTurnDegree);
						break;
					case Action.RobotBackTurnRight:
						setBack(Action.RobotMoveDistance);
						setTurnRight(Action.RobotTurnDegree);
						break;
					case Action.RobotFire:
						ahead(0);
						turnLeft(0);
						scanAndFire();
						break;
					default:
						System.out.println("Action Not Found");
						break;					
				}	
				execute();					
				turnRadarRightRadians(2*PI);
				//Update states
				state = getState();
				agent.QLearn(state, action, reward);
				accumuReward += reward;					
				//Reset Values
				reward = 0.0d;
				isHitByBullet = 0;
			}
		}		
	}
	
	
	////======= Internal Supportive Functions ========
	private void scanAndFire() {
		found = false;
		while (!found) {
			setTurnRadarLeft(360);
			execute();
		}
		turnGunLeft(getGunHeading() - getHeading() - oppoBearing);
		double currentOppoDist = oppoDist;
		if (currentOppoDist < 101) fire(6);
		else if (currentOppoDist < 201) fire(4);
		else if (currentOppoDist < 301) fire(2);
		else fire(1);
	}

	private int getState() {
		int heading = State.getHeading(getHeading());   
		int targetDistance = State.getTargetDistance(target.distance);   
		int targetBearing = State.getTargetBearing(target.bearing);   
		int HorizontalNSafe = State.getHorizontalNSafe(getX(),getBattleFieldWidth());
		int VerticalNSafe = State.getVerticalNSafe(getY(), getBattleFieldHeight());
		//System.out.println("State(" + heading + ", " + targetDistance + ", " + targetBearing + ", " + isHitWall + ", " + isHitByBullet + ")");   
		//int state = State.Mapping[heading][targetDistance][targetBearing][isHitWall][isHitByBullet]; 
		int state = State.Mapping[heading][targetDistance][targetBearing][HorizontalNSafe][VerticalNSafe][isHitByBullet]; 
		return state;  
	}

	
	/*
	 * Make sure bearing is within the -pi to pi range
	 */
	private double NormalizeBearing(double radarOffset) {
		while (radarOffset > PI) {
			radarOffset -= 2*PI;
		}
		while (radarOffset < -PI) {
			radarOffset += 2*PI;
		}
		return radarOffset;
	}
	
	//======= Event ========
	
	/**
	 * onBulletHit: What to do when hit other robots
	 */
	public void onBulletHit(BulletHitEvent e)   
    {  
		if (target.name == e.getName()) {     
		    double change = e.getBullet().getPower() * 9;   
		    System.out.println("Bullet Hit: " + change);   
		    if (interRewards) reward += change;   
		}   
    }  
	
	
	/**
	 * onBulletMissed: What to do when miss other robots
	 */
	public void onBulletMissed(BulletMissedEvent e)   
    {   
		double change = -e.getBullet().getPower() * 7.5;   
		System.out.println("Bullet Missed: " + change);   
		if (interRewards) reward += change;   
    }  

	/**
	 * onScannedRobot: What to do when you see another robot
	 */
	public void onScannedRobot(ScannedRobotEvent e) {
		found = true;
		oppoDist = e.getDistance();
		oppoBearing = e.getBearing();
		if ((e.getDistance() < target.distance)||(target.name == e.getName()))   
        {   
          //the next line gets the absolute bearing to the point where the bot is   
          double absbearing_rad = (getHeadingRadians()+e.getBearingRadians())%(2*PI);   
          //this section sets all the information about our target   
          target.name = e.getName();   
          double h = NormalizeBearing(e.getHeadingRadians() - target.head);   
          h = h/(getTime() - target.ctime);   
          target.changehead = h;   
          target.x = getX()+Math.sin(absbearing_rad)*e.getDistance(); //works out the x coordinate of where the target is   
          target.y = getY()+Math.cos(absbearing_rad)*e.getDistance(); //works out the y coordinate of where the target is   
          target.bearing = e.getBearingRadians();   
          target.head = e.getHeadingRadians();   
          target.ctime = getTime();             //game time at which this scan was produced   
          target.speed = e.getVelocity();   
          target.distance = e.getDistance();   
          target.energy = e.getEnergy();
        }
	}

	/**
	 * onHitByBullet: What to do when you're hit by a bullet
	 */
	public void onHitByBullet(HitByBulletEvent e) {
		if (target.name == e.getName())   {   
			double power = e.getBullet().getPower();   
			//double change = -(4 * power + 2 * (power - 1)) * 2;   
			double change = -6 * power;
			System.out.println("Hit By Bullet: " + change);   
			if (interRewards) reward += change;   
		}
		isHitByBullet = 1;  
	}
	
	/**
	 * onHitByBullet: What to do when you're hit by a robot
	 */
	public void onHitRobot(HitRobotEvent e) {   
		if (target.name == e.getName()) {   
			double change = -6.0;   
			System.out.println("Hit Robot: " + change);   
			if (interRewards) reward += change;   
		}   
    }  
	
	/**
	 * onHitWall: What to do when you hit a wall
	 */
	public void onHitWall(HitWallEvent e) {
		//double change = -(Math.abs(getVelocity()) * 0.5 - 1) * 10;   
		double change = -10.0;   
		System.out.println("Hit Wall: " + change);   
		if (interRewards) reward += change;   
       // isHitWall = 1;
      
	}	
	
	/**
	 * onRobotDeath: What to do when other robot dead
	 */
	public void onRobotDeath(RobotDeathEvent e) {   
		if (e.getName() == target.name) {
			target.distance = 10000; 
		}
		if (interRewards) reward += 20;
    }   
	
	/**
	 *  onWin: Robot win the game
	 */
	public void onWin(WinEvent event)   
    {   
		reward+=rewardForWin;
		templist = agent.getNeuralNetworks();
		if (!isOnline) {
			saveData(); 
		}	
		if (isOnline) {
			for (NeuralNet net : agent.getNeuralNetworks()) {
				net.save_robot(getDataFile("Weight_"+net.getNetID()+".dat"));
			}
		}
  		int winningFlag=1;
  		
  		PrintStream w = null; 
  		try { 
  			w = new PrintStream(new RobocodeFileOutputStream(getDataFile("battle_history.dat").getAbsolutePath(), true)); 
  			//w.println(accumuReward+" \t"+getRoundNum()+" \t"+winningFlag+" \t"+LearningAgent.explorationRate+" \t"+ winningRateArray[(getRoundNum()-numSavedBattle)%numSavedRate] + " \t" + updateBattleHistory (1)); 
  			w.println(accumuReward+" \t"+getRoundNum()+" \t"+winningFlag+" \t"+LearningAgent.explorationRate+" \t"+ chosenState + " \t" + chosenAction + " \t" + Math.abs(errorChosenToPrint));
  			if (w.checkError()) 
  				System.out.println("Could not save the data!");  //setTurnLeft(180 - (target.bearing + 90 - 30));
  				w.close(); 
  		} 
	    catch (IOException e) { 
	    	System.out.println("IOException trying to write: " + e); 
	    } 
	    finally { 
	    	try { 
	    		if (w != null) 
	    			w.close(); 
	    	} 
	    	catch (Exception e) { 
	    		System.out.println("Exception trying to close witer: " + e); 
	    	}
	    } 
  		
  		
    }   
     
	/**
	 *  onDeath: Robot lose the game
	 */
    public void onDeath(DeathEvent event)   
    {   
    	reward+=rewardForDeath;
    	if (!isOnline) {
			saveData(); 
		} 
    	
    	templist = agent.getNeuralNetworks();
    	if (isOnline) {
			for (NeuralNet net : agent.getNeuralNetworks()) {
				net.save_robot(getDataFile("Weight_"+net.getNetID()+".dat"));
			}			
		}
		
		int losingFlag=0;
		PrintStream w = null; 
		try { 
			w = new PrintStream(new RobocodeFileOutputStream(getDataFile("battle_history.dat").getAbsolutePath(), true)); 
			//w.println(accumuReward+" \t"+getRoundNum()+" \t"+losingFlag+" \t"+LearningAgent.explorationRate+" \t"+ winningRateArray[(getRoundNum()-numSavedBattle)%numSavedRate] + " \t" + updateBattleHistory (0)); 
			w.println(accumuReward+" \t"+getRoundNum()+" \t"+losingFlag+" \t"+LearningAgent.explorationRate+" \t" + chosenState + " \t" + chosenAction + " \t" + Math.abs(errorChosenToPrint));
			if (w.checkError()) 
				System.out.println("Could not save the data!"); 
			w.close(); 
		} 
		catch (IOException e) { 
			System.out.println("IOException trying to write: " + e); 
		} 
		finally { 
			try { 
				if (w != null) 
					w.close(); 
			} 
			catch (Exception e) { 
				System.out.println("Exception trying to close witer: " + e); 
			} 
		} 
		
    }	    
    
    
    //======= Load and Save the Look Up Table =========
    public void loadData()   {   
      try   {   
        table.loadData(getDataFile("LUT.dat"));   
      }   
      catch (Exception e)   {
      	out.println("Exception trying to write: " + e); 
      }   
    }   
     
    public void saveData()   {   
      try   {   
        table.saveData(getDataFile("LUT.dat"));   
      }   
      catch (Exception e)   {   
        out.println("Exception trying to write: " + e);   
      }   
    }   
    

    public static double stdev(double[] list){
        double sum = 0.0;
        double mean = 0.0;
        double num = 0.0;
        double numi = 0.0;
        
        for (double i : list) {
            sum+=i;
        }
        mean = sum/list.length;

        for (double i : list) {
            numi = Math.pow( (i - mean), 2);
            num+=numi;
        }

        return Math.sqrt(num/list.length);
    }

}

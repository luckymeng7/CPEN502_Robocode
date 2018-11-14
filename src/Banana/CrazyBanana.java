package Banana;

import java.awt.Color;
import java.awt.geom.*;   
import java.io.IOException;
import java.io.PrintStream;

import robocode.*;

import Learning.*;


// API help : http://robocode.sourceforge.net/docs/robocode/robocode/Robot.html

/**
 * CrazyBanana - a robot by xmli01
 */
public class CrazyBanana extends AdvancedRobot 
{
	
	public static final double PI = Math.PI;   
	private Target target;   
	private QTable table;   
	private LearningAgent agent;   
	private double reward = 0.0;   
	private double firePower;   
	//private int direction = 1;   
	private int isHitWall = 0;   
	private int isHitByBullet = 0;   

	private boolean isSARSA = true;
	
	double rewardForWin=100;
	double rewardForDeath=-10;
	double accumuReward=0.0;
	

    
	/**
	 * run: CrazyBanana's default behavior
	 */
	public void run() {
		// Initialization of the RL
		table = new QTable();  
		loadData();
        agent = new LearningAgent(table);   
        target = new Target();   
        target.distance = 100000;

		// Initialization of the robot
		setAllColors(Color.red);
		setAdjustGunForRobotTurn(true);
		setAdjustRadarForGunTurn(true);
		turnRadarRightRadians(2*PI);
		
		if(getRoundNum()>500){
			LearningAgent.explorationRate=0.3;
	    }

		System.out.println("moveRobot Action ");
		// Robot main loop
		while(true) {
			
			firePower = 400/target.distance;
			if (firePower > 3) {
				firePower = 3;
			}
			
			moveRobot();
			moveRadar();
			moveGun();
			/*
			if (getGunHeat()== 0) {
				setFire(firePower);
			}
			*/
			execute();
		}
		
	}

	
	private void moveRobot() {
		int state = getState();
		int action = agent.selectAction(state);
		if (isSARSA) {
			agent.SARSALearn(state, action, reward);
		} else {
			agent.QLearn(state, action, reward);
		}
		// Move the Robot
		switch (action) {
			case Action.RobotAhead:
				setAhead(Action.RobotMoveDistance);
				break;
			case Action.RobotBack:
				setBack(Action.RobotMoveDistance);
				break;
			case Action.RobotAheadTurnLeft:
				setBack(Action.RobotMoveDistance);
				setTurnLeft(Action.RobotTurnDegree);
				break;
			case Action.RobotAheadTurnRight:
				setBack(Action.RobotMoveDistance);
				setTurnRight(Action.RobotTurnDegree);
				break;
			case Action.RobotBackTurnLeft:
				setBack(Action.RobotMoveDistance);
				setTurnRight(Action.RobotTurnDegree);
				break;
			case Action.RobotBackTurnRight:
				setBack(Action.RobotMoveDistance);
				setTurnLeft(Action.RobotTurnDegree);
				break;
			case Action.RobotFire:
				setFire(1);
				break;
			default:
				System.out.println("Wrong Action Order");
				break;	
		}
		
		// Reset the variables for next learn
		accumuReward += reward;
		reward = 0.0;
		isHitWall = 0;
		isHitByBullet = 0;		
	}
	
	private int getState() {
		int heading = State.getHeading(getHeading());   
		int targetDistance = State.getTargetDistance(target.distance);   
		int targetBearing = State.getTargetBearing(target.bearing);   
		out.println("Stste(" + heading + ", " + targetDistance + ", " + targetBearing + ", " + isHitWall + ", " + isHitByBullet + ")");   
		int state = State.Mapping[heading][targetDistance][targetBearing][isHitWall][isHitByBullet];   
		return state;  
	}


	private void moveRadar() {
		double radarOffset;
		if (getTime() - target.ctime >4) {
			// If haven't seen anyone in a while, rotate the radar to find a target
			radarOffset = 2*PI;
		} else {
			//Scan the target
			radarOffset = getRadarHeadingRadians() - (Math.PI/2 - Math.atan2(target.y - getY(),target.x - getX())); 
			// Normalize Bearing
			radarOffset = NormalizeBearing(radarOffset); 
			// Eliminate the influence of Wobbling
			if (radarOffset < 0) {
				radarOffset -= PI/10;
			} else {
				radarOffset += PI/10;
			}
				
		}
		// Turn the Radar
		setTurnRadarLeftRadians(radarOffset); 
	}
	
	/*
	 * Make sure bearing is within the -pi to pi range
	 */
	private double NormalizeBearing(double radarOffset) {
		if (radarOffset > PI) {
			radarOffset -= 2*PI;
		}
		if (radarOffset < PI) {
			radarOffset += 2*PI;
		}
		return radarOffset;
	}


	private void moveGun() {
		long time;   
		long nextTime;   
		Point2D.Double p;   
		p = new Point2D.Double(target.x, target.y);   
		for (int i = 0; i < 20; i++)   
		{   
		  nextTime = (int)Math.round((getrange(getX(),getY(),p.x,p.y)/(20-(3*firePower))));   
		  time = getTime() + nextTime - 10;   
		  p = target.guessPosition(time);   
		}   
		//offsets the gun by the angle to the next shot based on linear targeting provided by the enemy class   
		double gunOffset = getGunHeadingRadians() - (Math.PI/2 - Math.atan2(p.y - getY(),p.x -  getX()));   
		setTurnGunLeftRadians(NormalizeBearing(gunOffset));   
		
	}

	
	/*
	 * return Distance between two points
	 */
	private double getrange(double x1, double y1, double x2, double y2) {
		double xo = x2-x1;   
        double yo = y2-y1;   
        double h = Math.sqrt( xo*xo + yo*yo );   
        return h;
	}

	/**
	 * onBulletHit: What to do when hit other robots
	 */
	public void onBulletHit(BulletHitEvent e)   
    {  
		if (target.name == e.getName()) {     
		    double change = e.getBullet().getPower() * 9;   
		    System.out.println("Bullet Hit: " + change);   
		    reward += change;   
		}   
    }  
	
	
	/**
	 * onBulletMissed: What to do when miss other robots
	 */
	public void onBulletMissed(BulletMissedEvent e)   
    {   
		double change = -e.getBullet().getPower();   
		System.out.println("Bullet Missed: " + change);   
		reward += change;   
    }  

	/**
	 * onScannedRobot: What to do when you see another robot
	 */
	public void onScannedRobot(ScannedRobotEvent e) {
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
			double change = -(4 * power + 2 * (power - 1));   
			System.out.println("Hit By Bullet: " + change);   
			reward += change;   
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
			reward += change;   
		}   
    }  
	
	/**
	 * onHitWall: What to do when you hit a wall
	 */
	public void onHitWall(HitWallEvent e) {
		double change = -(Math.abs(getVelocity()) * 0.5 - 1);   
		System.out.println("Hit Wall: " + change);   
        reward += change;   
        isHitWall = 1;   
	}	
	
	/**
	 * onRobotDeath: What to do when you are dead
	 */
	public void onRobotDeath(RobotDeathEvent e) {   
		if (e.getName() == target.name) {
			target.distance = 10000; 
		}
    }   
	
	/**
	 *  onWin: Robot win the game
	 */
	public void onWin(WinEvent event)   
    {   
		reward+=rewardForWin;
		moveRobot();
		saveData();   
  		int winningFlag=1;

  		PrintStream w = null; 
  		try { 
  			w = new PrintStream(new RobocodeFileOutputStream(getDataFile("battle_history.dat").getAbsolutePath(), true)); 
  			w.println(accumuReward+" "+getRoundNum()+"\t"+winningFlag+" "+" "+LearningAgent.explorationRate); 
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
    	moveRobot();
		saveData();  
       
		int losingFlag=0;
		PrintStream w = null; 
		try { 
			w = new PrintStream(new RobocodeFileOutputStream(getDataFile("battle_history.dat").getAbsolutePath(), true)); 
			w.println(accumuReward+" "+getRoundNum()+"\t"+losingFlag+" "+" "+LearningAgent.explorationRate); 
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
    
    /*
	public void saveBattleHist(int win) {
//		System.out.print(round);
//		System.out.println("saving hist!!!");
		PrintStream write = null;
		try {
			write = new PrintStream(new RobocodeFileOutputStream(getDataFile("battle_history.dat").getAbsolutePath(), true));
//			write.print(new Double(round));
//			write.print(new String(","));
			write.println(new Double(win));
			if (write.checkError())
				System.out.println("Could not save the data!");
			write.close();

			round++;
		}
		catch (IOException e) {
	   //   System.out.println("IOException trying to write: " + e);
		}
		finally {
			try {
				if (write != null)
					write.close();
			}
			catch (Exception e) {
	  //      System.out.println("Exception trying to close witer: " + e);
			}
		}
	}
	*/
    public void loadData()   
    {   
      try   
      {   
        table.loadData(getDataFile("movement.dat"));   
      }   
      catch (Exception e)   {
      	out.println("Exception trying to write: " + e); 

      }   
    }   
     
    public void saveData()   
    {   
      try   
      {   
        table.saveData(getDataFile("movement.dat"));   
      }   
      catch (Exception e)   
      {   
        out.println("Exception trying to write: " + e);   
      }   
    }   
}

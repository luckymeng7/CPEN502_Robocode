package Learning;

/*
 * Quantization, limite the state number for each dimention
 */
public class State {

	public static final int NumHeading = 4;  
	public static final int NumTargetDistance = 10;  
	public static final int NumTargetBearing = 4;  
	public static final int NumHorizontalDistanceNSafe = 2; 
	public static final int NumVerticalDistanceNSafe = 2; 
	public static final int NumHitByBullet = 2;  
	public static final int NumStates;  
	public static final int Mapping[][][][][][];  
	
	static  {  
		Mapping = new int[NumHeading][NumTargetDistance][NumTargetBearing][NumHorizontalDistanceNSafe][NumVerticalDistanceNSafe][NumHitByBullet];  
		int count = 0;  
		for (int a = 0; a < NumHeading; a++)  
		  for (int b = 0; b < NumTargetDistance; b++)  
		    for (int c = 0; c < NumTargetBearing; c++)  
		      for (int d = 0; d < NumHorizontalDistanceNSafe; d++)  
		    	for (int e = 0; e < NumVerticalDistanceNSafe; e++)  
		          for (int f = 0; f < NumHitByBullet; f++)  
		      Mapping[a][b][c][d][e][f] = count++;  
		  
		NumStates = count;  
	}
	
	
	public static int getHeading(double heading)  {  
		double angle = 360 / NumHeading;  
		double newHeading = heading + angle / 2;  
		if (newHeading > 360.0)  
		  newHeading -= 360.0;  
		return (int)(newHeading / angle);  
	}  
	
	public static int getTargetDistance(double value)  {  
	    int distance = (int)(value / (1000/NumTargetDistance));  
	    if (distance > NumTargetDistance - 1)  
	      distance = NumTargetDistance - 1;  
	    return distance;  
    }  
 
	public static int getTargetBearing(double bearing)  {  
		double PIx2 = Math.PI * 2;  
		if (bearing < 0)  
			bearing = PIx2 + bearing;  
		double angle = PIx2 / NumTargetBearing;  
		double newBearing = bearing + angle / 2;  
		if (newBearing > PIx2)  
			newBearing -= PIx2;  
		return (int)(newBearing / angle);  
	}  
	
	public static int getHorizontalNSafe (double robotX, double BattleFieldX)  {
		int distanceToCenterH;
		if (robotX > 50 && robotX < BattleFieldX-50 ) {
			distanceToCenterH = 0;	// Safe
		} else {
			distanceToCenterH = 1;	// unSafe, too close to wall
		}
		return distanceToCenterH;
	}
	
	public static int getVerticalNSafe (double robotY, double BattleFieldY)  {
		int distanceToCenterV;
		if (robotY > 50 && robotY < BattleFieldY-50 ) {
			distanceToCenterV = 0;	// Safe
		} else {
			distanceToCenterV = 1;	// unSafe, too close to wall
		}
		return distanceToCenterV;
	}
	
	public static int getStateIndex(int heading, int distance, int bearing, int horizontalUnsafe,int verticalUnsafe, int hitbybullet) {
		return Mapping[heading][distance][bearing][horizontalUnsafe][verticalUnsafe][hitbybullet];
	}
	
	public static int[] getStateFromIndex(int index)
	 {
		 int heading = index/(NumTargetDistance*NumTargetBearing*NumHorizontalDistanceNSafe*NumVerticalDistanceNSafe*NumHitByBullet);
		 int remain = index % (NumTargetDistance*NumTargetBearing*NumHorizontalDistanceNSafe*NumVerticalDistanceNSafe*NumHitByBullet);
		 int targetDistances = remain/(NumTargetBearing*NumHorizontalDistanceNSafe*NumVerticalDistanceNSafe*NumHitByBullet);
		 remain = remain % (NumTargetBearing*NumHorizontalDistanceNSafe*NumVerticalDistanceNSafe*NumHitByBullet);
		 int targetBearing = remain/(NumHorizontalDistanceNSafe*NumVerticalDistanceNSafe*NumHitByBullet);
		 remain = remain % (NumHorizontalDistanceNSafe*NumVerticalDistanceNSafe*NumHitByBullet);
		 int horizontalUnsafe = remain/(NumVerticalDistanceNSafe*NumHitByBullet);
		 remain = remain % (NumVerticalDistanceNSafe*NumHitByBullet);
		 int verticalUnsafe = remain/(NumHitByBullet);
		 int hitByBullet = remain % (NumHitByBullet);		 
		 int[] states = new int[6];		 
		 states[0]=heading;
		 states[1]=targetDistances;
		 states[2]=targetBearing;
		 states[3]=horizontalUnsafe;
		 states[4]=verticalUnsafe;
		 states[5]=hitByBullet;
		 
		 return states;
	 }
	  
}

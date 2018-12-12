package Learning;

import java.io.*;

import robocode.RobocodeFileOutputStream;
   
public class QTable {

	private double [][] table;
	
	public QTable() {
		table = new double [State.NumStates][Action.NumRobotActions];
		initializeTable();
		
	}

	private void initializeTable() {
		// TODO Auto-generated method stub
		for (int stateN = 0; stateN < State.NumStates; stateN++)   
		      for (int actionN = 0; actionN < Action.NumRobotActions; actionN++)   
		        //table[stateN][actionN] = Math.random();  
		    	  table[stateN][actionN] = 0.0;  
	}
	
	public double getQValue(int state, int action) {
		return this.table[state][action];
	}
	
	public void setQvalue(int state, int action, double value) {
		this.table[state][action] = value;
	}
	
	/*
	 * Return the max Q value in current state
	 */
	public double getMaxQValue (int currentState) {
		double maxQ = Double.NEGATIVE_INFINITY;
		for(int actionN = 0; actionN < this.table[currentState].length; actionN++) {
			if (table[currentState][actionN] > maxQ)
				maxQ = this.table[currentState][actionN];
		}
		return maxQ;
	}
	
	public double [] [] getTable(){
		return table;
	}
	
	/*
	 * Return the action that has the max Q value
	 */
	public int getBestAction(int currentState) {
		double maxQ = Double.NEGATIVE_INFINITY;
		int bestAct = 0;
		for(int actionN = 0; actionN < this.table[currentState].length; actionN++) {
			if (table[currentState][actionN] > maxQ) {
				maxQ = this.table[currentState][actionN];
				bestAct = actionN;
			}
		}
		return bestAct;
	}

	
	public void loadData(File file)   {   
		BufferedReader r = null;   
	    try   {   
	    	r = new BufferedReader(new FileReader(file));   
	    	for (int i = 0; i < State.NumStates; i++)   
	    		for (int j = 0; j < Action.NumRobotActions; j++)   
	    			table[i][j] = Double.parseDouble(r.readLine());   
	    }   
	    catch (IOException e)   {   
	    	System.out.println("IOException trying to open reader: " + e);   
	    	initializeTable();   
	    }   
	    catch (NumberFormatException e)   {   
	    	initializeTable();   
	    }  
	    finally {   
	    	try {   
		        if (r != null)   
		        	r.close();   
	        }   
	    	catch (IOException e) {   
	    		System.out.println("IOException trying to close reader: " + e);   
	    	}   
	    }   
    }   
	   
	public void saveData(File file)   {   
		PrintStream w = null;   
	    try   {   
	    	w = new PrintStream(new RobocodeFileOutputStream(file));   
  			for (int i = 0; i < State.NumStates; i++)   
  				for (int j = 0; j < Action.NumRobotActions; j++)   
  					w.println(new Double(table[i][j]));  
  			
  			if (w.checkError())   
  				System.out.println("Could not save the data!");   
  			
  			w.close();   
	    }   
	    catch (IOException e)   {   
	    	System.out.println("IOException trying to write: " + e);   
	    }   
	    finally   {   
	    	try   {   
	    		if (w != null)   
	    			w.close();   
	    	}   
	    	catch (Exception e)   {   
	    		System.out.println("Exception trying to close witer: " + e);   
	    	}   
	    }   
	  }
	
	
	public void printTable(String fileName) throws IOException {
		System.out.println("good up till here");
		PrintWriter printWriter = new PrintWriter(new FileWriter(fileName));		
		printWriter.printf("State, Action, QValue \n");
		for (int i = 0; i < State.NumStates; i++)   {
	        for (int j = 0; j < Action.NumRobotActions; j++) {
	        	printWriter.printf("%d, %d, %f, \n", i, j, this.table[i][j]);
	        }
		}
		printWriter.flush();
		printWriter.close();
	}
	
}

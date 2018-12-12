package NeuralNet;

import java.util.ArrayList;
import java.util.Random;
import java.io.*;
import Interface.NeuralNetInterface;

public class NeuralNet implements NeuralNetInterface {
	
	private int netID;
	private int argNumInputs;
	private int argNumHiddens;
	private int argNumOutputs;
	private double argLearningRate;
	private double argMomentumRate;
	private double argQMin;
	private double argQMax;
	/*Keep inputNeuron, hiddenNeuron, outputNeuron separately in arraylists*/
	private ArrayList<Neuron> inputLayerNeurons = new ArrayList<Neuron>();	
	private ArrayList<Neuron> hiddenLayerNeurons = new ArrayList<Neuron>();
	private ArrayList<Neuron> outputLayerNeurons = new ArrayList<Neuron>();
	
	/* Need to keep in mind that, all neurons can connect to the same bias neuron, since the output of bias neuron is not evaluated*/
	private Neuron biasNeuron = new Neuron("bias"); // Neuron id 0 is reserved for bias neuron
	
	//private double inputData[][] = {{0,0},{1,0},{0,1},{1,1}};	
	//private double expectedOutput[][] = {{0},{1},{1},{0}};
	private double epochOutput[][] = {{-1},{-1}, {-1}, {-1}};//Initial value -1 for each output
	private ArrayList<Double> errorInEachEpoch = new ArrayList<>();
	private ArrayList<Integer> epochEachTrail = new ArrayList<>();
	
	public NeuralNet(
					int numInputs, int numHiddens, 
					int numOutputs, double learningRate, 
					double momentumRate, double a, double b,
					int id
					) {
		this.argNumInputs = numInputs;
		this.argNumHiddens = numHiddens;
		this.argNumOutputs = numOutputs;
		this.argLearningRate = learningRate;
		this.argMomentumRate = momentumRate;
		this.argQMin = a;
		this.argQMax = b;
		//this.inputData = inputData;
		//this.expectedOutput = expectedOutput;
		this.setUpNetwork();
		this.initializeWeights();
		this.netID = id;
	}
	
	public void setUpNetwork() {
		/*Set up Input layer first*/
		for(int i = 0; i < this.argNumInputs;i++) {
			String index = "Input"+Integer.toString(i);
			//System.out.println(index);
			Neuron neuron = new Neuron(index);
			inputLayerNeurons.add(neuron);
		}
		biasNeuron.setOutput(1.0); 
		
		for(int j = 0; j < this.argNumHiddens;j++) {
			String index = "Hidden"+Integer.toString(j);
			//System.out.println(index);
			Neuron neuron = new Neuron(index,"Customized",inputLayerNeurons,biasNeuron);
			hiddenLayerNeurons.add(neuron);
		}
		
		for(int k = 0; k < this.argNumOutputs;k++) {
			String index = "Output"+Integer.toString(k);
			//System.out.println(index);
			Neuron neuron = new Neuron(index,"Customized",hiddenLayerNeurons,biasNeuron);
			outputLayerNeurons.add(neuron);
		}
	}
	/**
	 * This method sets input value in each forwarding.
	 * @param X The input vector. An array of doubles.
	 */
	public void setInputData(double [] inputs) {
		for(int i = 0; i < inputLayerNeurons.size(); i++) {
			inputLayerNeurons.get(i).setOutput(inputs[i]);//Input Layer Neurons only have output values.
		}
	}
	
	/**
	 * Get the output values from all output neurons, only one output in our problem
	 * @return a double output[]
	 */
	public double[] getOutputResults() {
		double [] outputs = new double[outputLayerNeurons.size()];
		for(int i = 0; i < outputLayerNeurons.size(); i++) {
			outputs[i] = outputLayerNeurons.get(i).getOutput();
		}
		return outputs;
	}
	
	public int getNetID(){
		return this.netID;
	}
	
	/*
	 * This method calculates the output of the NN based on the input 
	 * vector using forward propagation, calculation is done layer by layer 
	 */
	public void forwardPropagation() {
		for(Neuron hidden: hiddenLayerNeurons) {
			hidden.calculateOutput(argQMin,argQMax);
		}
		
		for (Neuron output: outputLayerNeurons) {
			output.calculateOutput(argQMin, argQMax);
		}
	}
	
	public ArrayList<Neuron> getInputNeurons(){
		return this.inputLayerNeurons;
	}
	
	public ArrayList<Neuron> getHiddenNeurons(){
		return this.hiddenLayerNeurons;
	}
	
	public ArrayList<Neuron> getOutputNeurons(){
		return this.outputLayerNeurons;
	}
	
	public ArrayList<Double> getErrorArray(){
		return this.errorInEachEpoch;
	}
	
	public ArrayList<Integer> getEpocNumArray(){
		return this.epochEachTrail;
	}
	
	/**
	 * 
	 * @return an array of results for each forwarding in a single epoch
	 */
	public double [][] getEpochResults() {
		return epochOutput;
	}
	
	public void setEpochResults(double[][] results){
		for(int i = 0; i < results.length;i++) {
			for(int j = 0; j < results[i].length;j++)
			{
				epochOutput[i][j] = results[i][j];
			}
		}
	}
	//TODO
	private void applyBackpropagation(double expectedOutput[]) {
		int i = 0;
		for(Neuron output : outputLayerNeurons) {
			double yi = output.getOutput();
			double ci = expectedOutput[i];
			
			ArrayList<NeuronConnection> connections = output.getInputConnectionList();
			for(NeuronConnection link : connections) {
				double xi = link.getInput();
				double error = customSigmoidDerivative(yi)*(ci-yi);
				link.setError(error);
				//current link's deltaweight has not be updated yet, so it is previous delta w
				double deltaWeight = argLearningRate*error*xi + argMomentumRate*link.getDeltaWeight();
				double newWeight = link.getWeight() + deltaWeight;
				link.setDeltaWeight(deltaWeight);
				link.setWeight(newWeight);			
			}
			i++;
		}
		
		for(Neuron hidden: hiddenLayerNeurons) {
			ArrayList<NeuronConnection> connections = hidden.getInputConnectionList();
			double yi =hidden.getOutput();
			for(NeuronConnection link : connections) {
				double xi = link.getInput();
				double sumWeightedError= 0;
				for(Neuron output: outputLayerNeurons) {
					double wjh = output.getInputConnection(hidden.getId()).getWeight();
					double errorFromAbove = output.getInputConnection(hidden.getId()).getError();
					sumWeightedError = sumWeightedError + wjh *errorFromAbove;
				}
				
				double error = customSigmoidDerivative(yi)*sumWeightedError;
				link.setError(error);
				double deltaWeight = argLearningRate*error*xi + argMomentumRate * link.getDeltaWeight();
				double newWeight = link.getWeight() + deltaWeight;
				link.setDeltaWeight(deltaWeight);
				link.setWeight(newWeight);							
			}
		}		
	}
	
	@Override
	public double [] outputFor(double[] inputData) {
		setInputData(inputData);
		forwardPropagation();
		double outputs[] = getOutputResults();
		return outputs;
	}

	@Override
	/**
	 * This method performs one epoch of train to the NN.
	 * @return accumulate squared error generated in one epoch.
	 */
	public double train(double [] argInputVector, double [] argTargetOutput) {
			double error = 0.0;
			double output[] = outputFor(argInputVector);
			for (int j = 0; j < argTargetOutput.length; j++) {
				double deltaErr = Math.pow((output[j]-argTargetOutput[j]),2);
				error = error + deltaErr;//sum of error for all  output neurons
			}		
			this.applyBackpropagation(argTargetOutput);
		//errorInEachEpoch.add(0.5*totalError);
		return error;
	}
	
	/*	
	public void tryConverge(int maxStep, double minError) {
		int i;
		double error = 1;
		for(i = 0; i < maxStep && error > minError; i++) {
			error = train();
		}
		System.out.println("Sum of squared error in last epoch = " + error);
		System.out.println("Number of epoch "+ i + "\n");
		if(i == maxStep) {
			System.out.println("Error in training, try again!");
		}
	}
	
	public void printRunResults(ArrayList<Double> errors, String fileName) throws IOException {
		int epoch;
		PrintWriter printWriter = new PrintWriter(new FileWriter(fileName));
		printWriter.printf("Epoch Number, Total Squared Error, \n");
		for(epoch = 0; epoch < errors.size(); epoch++) {
			printWriter.printf("%d, %f, \n", epoch, errors.get(epoch));
		}
		printWriter.flush();
		printWriter.close();
	}
	
	public int averageConverageNum (int trailNum, int maxEpochNum, double minError ) {
		double error;
		int epoch = 0;
		int totalEpoch = 0;
		int pass = 0;
		int fail = 0;
		for(int trail = 0; trail < trailNum; trail++ ) {
			//Reinitialize the trail
			this.initializeWeights();
			error = 1;
			for(epoch = 0; epoch < maxEpochNum && error > minError; epoch++) {
				error = train();
			}
			if (epoch == maxEpochNum) {
				fail = fail + 1;
			} else {
				pass = pass + 1;
			}
			totalEpoch = totalEpoch + epoch;
			epochEachTrail.add(epoch);
		}
		System.out.println("Total number of converged trials within " + maxEpochNum + " steps, out of " +  trailNum + " trials: " + pass);
		return totalEpoch/trailNum ;
	}
	*/
	
	public void printEachTrail(ArrayList<Integer> epochNum, String fileName) throws IOException {
		//int trail;
		PrintWriter printWriter = new PrintWriter(new FileWriter(fileName));
		printWriter.printf("Trail Number, Total epoch each Trail, \n");
		
		for(int trail = 0; trail < epochNum.size(); trail++) {			
			printWriter.printf("%d, %d, \n", trail, epochNum.get(trail));
		}
		printWriter.flush();
		printWriter.close();
	}
	
	@Override
	public void save(File argFile) {
		PrintStream savefile = null;
		try{
			savefile = new PrintStream(new FileOutputStream(argFile) );
			savefile.println(outputLayerNeurons.size());
			savefile.println(hiddenLayerNeurons.size());
			savefile.println(inputLayerNeurons.size());
			for(Neuron output : outputLayerNeurons){
				ArrayList<NeuronConnection> connections = output.getInputConnectionList();
				for(NeuronConnection link : connections){
					savefile.println(link.getWeight());
				}
			}
			for(Neuron hidden: hiddenLayerNeurons) {
				ArrayList<NeuronConnection> connections = hidden.getInputConnectionList();
				for(NeuronConnection link : connections){
					savefile.println(link.getWeight());
				}
			}
			savefile.flush();
			savefile.close();				
		}
		catch(IOException e){
			System.out.println("Cannot save the weight table.");
		}

	}

	@Override
	public void load(File argFileName) throws IOException {
		
		try{
			BufferedReader readfile = new BufferedReader(new FileReader(argFileName));
			int numOutputNeuron = Integer.valueOf(readfile.readLine());
			int numHiddenNeuron = Integer.valueOf(readfile.readLine());
			int numInputNeuron = Integer.valueOf(readfile.readLine());
			if ( numInputNeuron != inputLayerNeurons.size() ) {
				System.out.println ( "*** Number of inputs in file does not match expectation");
				readfile.close();
				throw new IOException();
			}
			if ( numHiddenNeuron != hiddenLayerNeurons.size() ) {
				System.out.println ( "*** Number of hidden in file does not match expectation" );
				readfile.close();
				throw new IOException();
			}
			if ( numOutputNeuron != outputLayerNeurons.size() ) {
				System.out.println ( "*** Number of output in file does not match expectation" );
				readfile.close();
				throw new IOException();
			}			

			for(Neuron output : outputLayerNeurons){
				ArrayList<NeuronConnection> connections = output.getInputConnectionList();
				for(NeuronConnection link : connections){
					link.setWeight(Double.valueOf(readfile.readLine()));
				}
			}
			for(Neuron hidden: hiddenLayerNeurons) {
				ArrayList<NeuronConnection> connections = hidden.getInputConnectionList();
				for(NeuronConnection link : connections){
					link.setWeight(Double.valueOf(readfile.readLine()));
				}
			}
			
			readfile.close();
		}
		catch(IOException e){
			System.out.println("IOException failed to open reader: " + e);
		}
		
	}
	
	
	public double sigmoidDerivative(double yi) {
		double result = yi*(1 - yi);
		return result;
	}
	
	public double bipolarSigmoidDerivative(double yi) {
		double result = 1.0/2.0 * (1-yi) * (1+yi);
		return result;
	}
	 /**
     * This method implements the first derivative of the customized sigmoid
     * @param x The input
     * @return f'(x) = -(1 / (b - a))(customSigmoid - a)(customSigmoid - b)
     */	
	public double customSigmoidDerivative(double yi) {
		double result = -(1.0/(argQMax-argQMin)) * (yi-argQMin) * (yi-argQMax);
		return result;
	}

	@Override
	public void initializeWeights() {
		// TODO Auto-generated method stub
		double upperbound = 0.5;
		double lowerbound = -0.5;
		for(Neuron neuron: hiddenLayerNeurons) {
			ArrayList <NeuronConnection> connections = neuron.getInputConnectionList();
			for(NeuronConnection connect: connections) {
				connect.setWeight(getRandom(lowerbound,upperbound));
			}
		}
		for(Neuron neuron: outputLayerNeurons) {
			ArrayList <NeuronConnection> connections = neuron.getInputConnectionList();
			for(NeuronConnection connect: connections) {
				connect.setWeight(getRandom(lowerbound,upperbound));
			}
		}
	}

	@Override
	public void zeroWeights() {
		for(Neuron neuron: hiddenLayerNeurons) {
			ArrayList <NeuronConnection> connections = neuron.getInputConnectionList();
			for(NeuronConnection connect: connections) {
				connect.setWeight(0);
			}
		}
		for(Neuron neuron:outputLayerNeurons) {
			ArrayList <NeuronConnection> connections = neuron.getInputConnectionList();
			for(NeuronConnection connect: connections) {
				connect.setWeight(0);
			}
		}

	}
	
	public double getRandom(double lowerbound, double upperbound) {
		double random = new Random().nextDouble();
		
		double result = lowerbound+(upperbound-lowerbound)*random;
		return result;
	}
	
	public double getBias() {
		return biasNeuron.getOutput();
	}

}

package NeuralNet;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;

public class Neuron {
	/*Neuron's identifier*/
	final String id;
	/*This arraylist saves all the income connections that are input to this neuron, which can be iterated through*/
	private ArrayList <NeuronConnection> inputConnections = new ArrayList <NeuronConnection> ();
	/*This hashmap saves all the income connections for lookup*/ 
	private HashMap<String,NeuronConnection> inputconnectionMap = new HashMap<String,NeuronConnection>();
	
	private String activationType;
	
	final double bias = 1;
	public double NeuronOutput = 0;
	//private int inputConnectionCount = 0;
	
	/*Class Constructors*/
	public Neuron(String id) {
		this.id = id;
	}
	
	public Neuron(String id, String ActivationFunctionType, List<Neuron> inputNeurons) {
		this.id = id;
		setActivationType(ActivationFunctionType);
		addInputConnections(inputNeurons);
	}
	
	public Neuron(String id, String ActivationFunctionType, List<Neuron> inputNeurons, Neuron bias) {
		this.id = id;
		setActivationType(ActivationFunctionType);
		addInputConnections(inputNeurons);
		addBiasConnection(bias);
	}
	
	/*Utility Functions*/
	public String getId() {
		return this.id;
	}
	
	public void setActivationType(String type) {
		this.activationType = type;
	}
	
	public String getActivationType() {
		return this.activationType;
	}

	/* Output Calculation Functions*/
	public double getOutput() {
		return this.NeuronOutput;
	}
	
	public void setOutput(double output) {
		this.NeuronOutput = output;
	}
	
	
	public void calculateOutput(double ArgA, double ArgB) {
		double weightedSum = inputSummingFunction(this.inputConnections);
		if(this.activationType == "bipolar") {
			this.NeuronOutput = bipolarSigmoid(weightedSum);
		}else if(this.activationType == "Unipolar") {
			this.NeuronOutput = unipolarSigmoid(weightedSum);
		}else if(this.activationType == "Customized") {
			this.NeuronOutput = customizedSigmoid(weightedSum,ArgA,ArgB);
		}
	}
	
	
	private double inputSummingFunction(ArrayList<NeuronConnection> inputConnections) {
		double weightedSum = 0;
		for(NeuronConnection connection : inputConnections)
		{
			double weight = connection.getWeight();
			double input = connection.getInput();
			weightedSum = weightedSum + weight*input;
		}
		
		//Add bias to the weighted sum
		/*if (biasConnection != null) {
			weightedSum = weightedSum + (this.biasConnection.getWeight()*this.bias);
		}
		*/
		return weightedSum;
	}
	
	/* Connection Constructing Functions*/
	/*Add multiple connections*/
	private void addInputConnections(List<Neuron> inputNeurons) {
		for(Neuron neuron : inputNeurons) {
			NeuronConnection connection = new NeuronConnection(neuron,this);//create new connection that connects to this neuron
			inputConnections.add(connection);//Put created connection into the connection array
			inputconnectionMap.put(neuron.getId(), connection);//Put the created connection into the look up map
		}
	}
	/* Add a connection for bias to this neuron, which bias neuron is a fake neuron should always output 1*/
	private void addBiasConnection(Neuron neuron) {
		NeuronConnection connection = new NeuronConnection(neuron,this);
		inputConnections.add(connection); //Add bias connection to the list for the ease of weight updating, it should always output 0
	}
	
	public NeuronConnection getInputConnection(String neuronId) {
		return inputconnectionMap.get(neuronId);
	}
	
	public ArrayList<NeuronConnection> getInputConnectionList() {
		return this.inputConnections;
	}
	
	
	/*Mathematics Functions*/
	public double unipolarSigmoid(double weightedSum){
		// compute the output signal of the binary sigmoid function with input x
		return 1/(1 + Math.exp(-weightedSum));
	}
	public double bipolarSigmoid(double weightedSum) {
		return 2/(1 + Math.exp(-weightedSum))-1;
	}
	
	public double customizedSigmoid(double weightedSum,double a, double b){
		return (b-a)/(1+Math.exp(-weightedSum))+a;
	}
	

}

package NeuralNet;

public class NeuronConnection {
	//Conection's Identifier
	//private final int id;
	
	/*Specify the source of this connection*/
	private Neuron SrcNeuron;
	/*Specify the destination of this connection*/
	private Neuron DestNeuron;
	/*Used to stored the currentInput this connection*/
	private double currentInput = 0;
	/*weight of this connection*/
	private double weight = 0;
	/*For momentum*/
	private double deltaWeight = 0;
	
	private double prevDeltaWeight = 0;	
	
	private double error = 0;
	
	
	/*Construction with random initialization*/
	public NeuronConnection(Neuron src, Neuron dest) {
		//this.id = id;
		this.SrcNeuron = src;
		this.DestNeuron = dest;
	}
	
	public NeuronConnection(Neuron src, Neuron dest, double weight) {
		//this.id = id;
		this.SrcNeuron = src;
		this.DestNeuron = dest;
		this.weight = weight;
	}
	
	public double getWeight() {
		return this.weight;
	}
	

	public void setWeight(double weight) {
		this.weight = weight;
	}
	
	public double getDeltaWeight() {
		return this.deltaWeight;		
	}
	
	public void setDeltaWeight(double delta) {
		this.prevDeltaWeight = this.deltaWeight;//Record previous delta weight
		this.deltaWeight = delta; 
	}
	
	public double getPrevDeltaWeight() {
		return this.prevDeltaWeight;
	}	
	
	public void setError(double value) {
		this.error = value;
	}
	
	public double getError() {
		return this.error;
	}
	
	
	public double getInput() {
		this.currentInput = SrcNeuron.getOutput();
		return this.currentInput;
	}
	
	
	public Neuron getSrcNeuron() {
		return this.SrcNeuron;
	}
	
	public Neuron getDestNeuron() {
		return this.DestNeuron;
	}

}

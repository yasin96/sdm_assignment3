package structures;

import java.io.Serializable;

public class Vector implements Serializable {

	private double[] values;

	private String key;
	
	public Vector(int dimensions) {
		this(null,new double[dimensions]);
	}
	

	public Vector(String key,double[] values){
		this.values = values;
		this.key = key;
	}
	
	public void set(int dimension, double value) {
		values[dimension] = value;
	}

	public double get(int dimension) {
		return values[dimension];
	}
	
	
	public int getDimensions(){
		return values.length;
	}


	public double dot(Vector other) {
		double sum = 0.0;
		for(int i=0; i < getDimensions(); i++) {
			sum += values[i] * other.values[i];
		}
		return sum;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public String getKey() {
		return key;
	}
	
}

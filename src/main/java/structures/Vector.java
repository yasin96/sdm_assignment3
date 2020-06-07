package structures;

import java.io.Serializable;
import java.util.List;
import java.util.stream.IntStream;

public class Vector implements Serializable {

	private final double[] values;

	private String key;

	public Vector(int dimensions) {
		this(null,new double[dimensions]);
	}


	public Vector(String key,double[] values){
		this.values = values;
		this.key = key;
	}

	public Vector(String key, List<Double> values) {
		this.key = key;
		this.values = new double[values.size()];
		IntStream.range(0, values.size()).
				forEach(i -> this.values[i] = values.get(i));
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

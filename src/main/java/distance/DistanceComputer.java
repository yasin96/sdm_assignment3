package distance;

import structures.Vector;

public class DistanceComputer {
    private String distanceFamily;

    public DistanceComputer(String distanceFamily) {
        this.distanceFamily = distanceFamily;
    }

    double compute_distance(Vector vec1, Vector vec2) {
        double distance = 0;

        switch (distanceFamily) {

            case "CityBlock": {
                for (int d = 0; d < vec1.getDimensions(); d++) {
                    distance += Math.abs(vec1.get(d) - vec2.get(d));
                }
                break;
            }

            case "Cosine": {
                double similarity = vec1.dot(vec2) / Math.sqrt(vec1.dot(vec1) * vec2.dot(vec2));
                distance = 1 - similarity;
                break;
            }

            case "Euclidean": {
                double sum = 0.0;
                for (int d = 0; d < vec1.getDimensions(); d++) {
                    double delta = vec1.get(d) - vec2.get(d);
                    sum += Math.pow(delta, 2);
                }
                distance = Math.sqrt(sum);
                break;
            }
        }

        return distance;

    }
}

package distance;

import structures.Vector;

import java.util.HashMap;
import java.util.Objects;

public class DistanceComputer {
    private final String distanceFamily;

    private HashMap<VectorPair, Double> distanceCache = new HashMap<>();

    public DistanceComputer(String distanceFamily) {
        this.distanceFamily = distanceFamily;
    }

    double compute_distance(Vector vec1, Vector vec2) {
        VectorPair vp = new VectorPair(vec1, vec2, distanceFamily);
        if (distanceCache.containsKey(vp)) {
            return distanceCache.get(vp);
        }
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
        distanceCache.put(vp, distance);
        return distance;

    }

    private static class VectorPair {
        Vector first;
        Vector second;
        String distanceFamily;

        public VectorPair(Vector first, Vector second, String distanceFamily) {
            this.first = first;
            this.second = second;
            this.distanceFamily = distanceFamily;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            VectorPair that = (VectorPair) o;
            return Objects.equals(first, that.first) &&
                    Objects.equals(second, that.second) &&
                    Objects.equals(distanceFamily, that.distanceFamily);
        }

        @Override
        public int hashCode() {
            return Objects.hash(first, second, distanceFamily);
        }
    }
}

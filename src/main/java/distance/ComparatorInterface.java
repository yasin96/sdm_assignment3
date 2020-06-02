package distance;

import structures.Vector;

import java.util.Comparator;

/**
 * This class sorts candidate neighbours according to their
 * distance to a query vector.
 */
public class ComparatorInterface implements Comparator<Vector> {

    private final Vector query;
    private final DistanceComputer distance;

    public ComparatorInterface(Vector query, DistanceComputer distance) {
        this.query = query;
        this.distance = distance;
    }

    @Override
    public int compare(Vector one, Vector other) {
        Double oneDistance = distance.compute_distance(query, one);
        Double otherDistance = distance.compute_distance(query, other);
        return oneDistance.compareTo(otherDistance);
    }
}

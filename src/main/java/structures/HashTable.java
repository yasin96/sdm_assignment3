package structures;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class HashTable implements Serializable {

    private final HashMap<String, List<Vector>> hashTable;
    private final List<Vector> randomMatrix;


    public HashTable(List<Vector> randomMatrix) {
        hashTable = new HashMap<>();
        this.randomMatrix = randomMatrix;
    }

    public List<Vector> query(Vector query) {
        String hashValue = hash_value(query);
        if (hashTable.containsKey(hashValue))
            return hashTable.get(hashValue);
        else
            return new ArrayList<>();
    }

    //If the result of the dot product is positive, assign the bit value as 1 else 0
    //hash function used in https://towardsdatascience.com/locality-sensitive-hashing-for-music-search-f2f1940ace23
    //I am not 100% sure if we should use this hash function or use euclidean hash function,cosine etc..
    public String hash_value(Vector vec) {
        StringBuilder hashValue = new StringBuilder();
        for (Vector matrix : randomMatrix) {
            int val = (vec.dot(matrix) > 0) ? 1 : 0;
            hashValue.append(val);
        }
        return hashValue.toString();
    }

    // add vectors to buckets
    public void add(Vector vector) {
        String hashValue = hash_value(vector);
        if (!hashTable.containsKey(hashValue)) {
            hashTable.put(hashValue, new ArrayList<>());
        }
        hashTable.get(hashValue).add(vector);
    }

    public HashMap<String, List<Vector>> getHashTable() {
        return hashTable;
    }
}
	

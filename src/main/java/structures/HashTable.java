package structures;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class HashTable implements Serializable {

    private HashMap<String, List<Vector>> hashTable;
    private List<Vector> randomMatrix;


    public HashTable(List<Vector> randomMatrix) {
        hashTable = new HashMap<String, List<Vector>>();
        this.randomMatrix = randomMatrix;
    }

    public List<Vector> query(Vector query) {
        String hashValue = hash_value(query);
        if (hashTable.containsKey(hashValue))
            return hashTable.get(hashValue);
        else
            return new ArrayList<Vector>();
    }

    //If the result of the dot product is positive, assign the bit value as 1 else 0
    //hash function used in https://towardsdatascience.com/locality-sensitive-hashing-for-music-search-f2f1940ace23
    //I am not 100% sure if we should use this hash function or use euclidean hash function,cosine etc..
    public String hash_value(Vector vec) {
        String hashValue = "";
        for (int j = 0; j < randomMatrix.size(); j++) {
            int val = (vec.dot(randomMatrix.get(j)) > 0) ? 1 : 0;
            hashValue += val;
        }
        return hashValue;
    }

    // add vectors to buckets
    public void add(Vector vector) {
        String hashValue = hash_value(vector);
        if (!hashTable.containsKey(hashValue)) {
            hashTable.put(hashValue, new ArrayList<Vector>());
        }
        hashTable.get(hashValue).add(vector);
    }

    public HashMap<String, List<Vector>> getHashTable() {
        return hashTable;
    }
}
	

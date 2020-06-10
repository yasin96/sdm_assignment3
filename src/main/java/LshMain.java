import distance.ComparatorInterface;
import distance.DistanceComputer;
import initialization.Initializer;
import lombok.extern.slf4j.Slf4j;
import structures.HashTable;
import structures.Vector;
import java.io.*;
import java.util.*;

@Slf4j
public class LshMain {

    private static List<Vector> trainingDataset;
    private static List<Vector> validationDataset;
    private static List<Vector> testDataset;

    //the hashSize,kNeighboursSize,numHashTables are just some random numbers.
    //TODO we should find some values that perform good on the verification data set
    private static final int hashSize = 50;
    private static final int kNeighboursSize = 4;
    private static final int numHashTables = 4;

    public static List<Vector> createRandomMatrix(int l, int d) {
        List<Vector> randomMatrix = new ArrayList<>();
        double value;
        Random rand = new Random();
        double cnst = Math.sqrt(3);
        for (int i = 0; i < l; i++) {
            Vector item = new Vector(d);
            for (int index = 0; index < d; index++) {
                int val = rand.nextInt(6);
                //1 with prob 1/6, -1 with prob 1/6
                if (val == 0)
                    value = -cnst;
                else if (val == 1)
                    value = cnst;
                else //0 with prob 2/3
                    value = 0;
                item.set(index, value);
            }
            randomMatrix.add(item);
        }
        return randomMatrix;
    }


    private static void initializeAndCalculateHash (List<HashTable> hashtables, int numFeatures) {
        for (int n = 0; n < numHashTables; n++) {
            HashTable hashTable = new HashTable(createRandomMatrix(hashSize, numFeatures));
            for (Vector vector : trainingDataset) {
                hashTable.add(vector);
            }
            hashtables.add(hashTable);
        }
    }

    private static void findCandidatesAndCalculateDistance(List<HashTable> tables, String distanceType) {
        for (Vector queryVec : validationDataset) {
        	System.out.println(queryVec.get(0));
            Set<Vector> candidateSet = new HashSet<>();
            for (HashTable table : tables) {
                List<Vector> v = table.query(queryVec);                 
                candidateSet.addAll(v);
            }
           
//            System.out.println("---------------------------------------------");
//            System.out.println("Candidates for vector: " + queryVec.getKey());
//            candidateSet.forEach(c -> System.out.println(c.getKey()));
//            System.out.println("---------------------------------------------");

            //part c) of the algorithm in assignment
            //TODO part of assignment - think about how to treat cases where there are less then k music tracks found as similar ti
            List<Vector> candidates = new ArrayList<>(candidateSet);
            DistanceComputer measure = new DistanceComputer(distanceType);
            ComparatorInterface dc = new ComparatorInterface(queryVec, measure);
            candidates.sort(dc);
            if (candidates.size() > kNeighboursSize) {
                candidates = candidates.subList(0, kNeighboursSize);
            }
        }
    }



    public static void main(String[] args) {
        //Initialize train, validation, test datasets
        final Initializer initializer = new Initializer();
        trainingDataset = initializer.initializeSplitDataSets("fmapreproc\\split\\train.json");
        validationDataset = initializer.initializeSplitDataSets("fmapreproc\\split\\validate.json");
        testDataset = initializer.initializeSplitDataSets("fmapreproc\\split\\test.json");

        final int numValidationData = validationDataset.size();
        final int numObservations = trainingDataset.size();
        final int numFeatures = trainingDataset.get(0).getDimensions();

        //creation of hash tables
        List<HashTable> tables = new ArrayList<>();
        initializeAndCalculateHash(tables, numFeatures);


        findCandidatesAndCalculateDistance(tables, "Euclidean");

    }
}

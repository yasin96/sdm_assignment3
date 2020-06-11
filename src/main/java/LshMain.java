import csv.JsonLoader;
import distance.ComparatorInterface;
import distance.DistanceComputer;
import initialization.Initializer;
import lombok.extern.slf4j.Slf4j;
import structures.HashTable;
import structures.Vector;

import java.io.*;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
public class LshMain {

    private static List<Vector> trainingDataset;
    private static List<Vector> validationDataset;
    private static List<Vector> testDataset;
    private static List<Vector> trainingAndValidationDataset;

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


    private static void initializeAndCalculateHash(List<HashTable> hashtables, int numFeatures, List<Vector> dataset) {
        for (int n = 0; n < numHashTables; n++) {
            HashTable hashTable = new HashTable(createRandomMatrix(hashSize, numFeatures));
            for (Vector vector : dataset) {
                hashTable.add(vector);

            }
            hashtables.add(hashTable);
        }
    }

    private static HashMap<String, List<Vector>> findKNearestNeighbours(List<HashTable> tables, String distanceType, List<Vector> dataset) {
        HashMap<String, List<Vector>> neighbours_map = new HashMap<String, List<Vector>>();
        for (Vector queryVec : dataset) {
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
            List<Vector> neighbours = new ArrayList<>(candidateSet);
            DistanceComputer measure = new DistanceComputer(distanceType);
            ComparatorInterface dc = new ComparatorInterface(queryVec, measure);
            neighbours.sort(dc);
            if (neighbours.size() > kNeighboursSize) {
                neighbours = neighbours.subList(0, kNeighboursSize);
            }
            neighbours_map.put(queryVec.getKey(), neighbours);
        }
        return neighbours_map;
    }


    public static void main(String[] args) {
        //Initialize train, validation, test datasets
        final Initializer initializer = new Initializer();
        trainingDataset = initializer.initializeSplitDataSets("fma_metadata\\split\\train.json");
        validationDataset = initializer.initializeSplitDataSets("fma_metadata\\split\\validate.json");
        testDataset = initializer.initializeSplitDataSets("fma_metadata\\split\\test.json");
        //this is needed for "retrain your algorithm with these parameter choices using the training and validation data set as training data"
        trainingAndValidationDataset = new ArrayList<Vector>(trainingDataset);
        trainingAndValidationDataset.addAll(validationDataset);

        final int numValidationData = validationDataset.size();
        final int numObservations = trainingDataset.size();
        final int numFeatures = trainingDataset.get(0).getDimensions();

        //creation of hash tables
        List<HashTable> tables = new ArrayList<>();
        initializeAndCalculateHash(tables, numFeatures, trainingDataset);
        HashMap<String, List<Vector>> neighbours_map = findKNearestNeighbours(tables, "Euclidean", validationDataset);
        //TODO another function that uses this candidates_map to predict the genre as "the the majority genre of its k-nearestneighbours"
        //TODO evaluation of the classification accurancy
        //TODO call initializeAndCalculateHash for trainingAndValidationDataset and findKNearestNeighbours (together with genre prediction) for testDataset
        System.out.println("Finished finding neighbours");
        JsonLoader jsonLoader = new JsonLoader();
        Map<String, String> genres = jsonLoader.loadGenreJson("fma_metadata\\split\\genres.json");
        Map<String, String> predictedGenres = neighbours_map.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> {
                            List<String> neighbours = e.getValue().stream().map(vector -> genres.get(vector.getKey())).collect(Collectors.toList());
                            HashMap<String, Integer> counts = new HashMap<>();
                            new HashSet<>(neighbours).forEach(s -> counts.put(s, 0));
                            neighbours.forEach(genre -> counts.put(genre, counts.get(genre) + 1));
//                            System.out.println(genres.get(e.getKey()));
//                            System.out.println(neighbours.toString());
                            String genre;
                            if (neighbours.isEmpty()) {
                                genre = "Outlier";
                            } else {
                                genre = Collections.max(counts.entrySet(), Map.Entry.comparingByValue()).getKey();
                            }
//                            System.out.println(genre);
                            return genre;
                        }
                ));
        long datasetSize = predictedGenres.size();
        long correctPredictions = predictedGenres.entrySet().stream().filter(stringStringEntry -> {
            String trueGenre = genres.get(stringStringEntry.getKey());
            return trueGenre.equals(stringStringEntry.getValue());
        }).count();
        System.out.println(datasetSize);
        System.out.println(correctPredictions);
        double accuracy = correctPredictions / (datasetSize * 1.0f);
        System.out.printf("Accuracy: %s%n", accuracy);

    }
}

import csv.JsonLoader;
import initialization.Initializer;
import structures.Vector;

import java.util.*;

public class LshGridSearch {
    private List<Integer> hashSizes;
    private List<Integer> kNeighboursSizes;
    private List<Integer> numHashTables;

    public LshGridSearch(List<Integer> hashSizes, List<Integer> kNeighboursSizes, List<Integer> numHashTables) {
        this.hashSizes = hashSizes;
        this.kNeighboursSizes = kNeighboursSizes;
        this.numHashTables = numHashTables;
    }

    public Properties calculateOptimalProperties(List<Vector> trainingDataset, List<Vector> validationDataset, Map<String, String> genres) {
        Properties bestProperties = new Properties();
        Map<Properties, Double> propertyAccuracy = new HashMap<>();
        for (Integer hashSize : hashSizes) {
            for (Integer kNeighboursSize : kNeighboursSizes) {
                for (Integer numHashTable : numHashTables) {
                    Properties properties = new Properties();
                    properties.put("hashSize", hashSize);
                    properties.put("kNeighboursSize", kNeighboursSize);
                    properties.put("numHashTable", numHashTable);
                    Lsh lsh = new Lsh(hashSize, kNeighboursSize, numHashTable);
                    double accuracy = lsh.trainAndTest(trainingDataset, validationDataset, genres);
                    properties.put("accuracy", accuracy);
                    System.out.printf("Result for: %s%n", properties.toString());
                    propertyAccuracy.put(properties, accuracy);
                }
            }
        }
        bestProperties = Collections.max(propertyAccuracy.entrySet(), Map.Entry.comparingByValue()).getKey();
        return bestProperties;
    }

    public static void main(String[] args) {
        final Initializer initializer = new Initializer();
        List<Vector> trainingDataset = initializer.initializeSplitDataSets("fma_metadata\\split\\train.json");
        List<Vector> validationDataset = initializer.initializeSplitDataSets("fma_metadata\\split\\validate.json");
        List<Vector> testDataset = initializer.initializeSplitDataSets("fma_metadata\\split\\test.json");
        JsonLoader jsonLoader = new JsonLoader();
        Map<String, String> genres = jsonLoader.loadGenreJson("fma_metadata\\split\\genres.json");
        System.out.println("Loaded data");
        LshGridSearch lshGridSearch = new LshGridSearch(
                List.of(40,50,70),
                List.of(4,5,7),
                List.of(3,4,6)
        );
        System.out.println("Starting grid search");
        Properties properties = lshGridSearch.calculateOptimalProperties(trainingDataset, validationDataset, genres);
        System.out.println("Finished grid search");
        System.out.println(properties.toString());
    }
}

import csv.JsonLoader;
import initialization.Initializer;
import structures.Vector;

import java.util.*;

public class LshGridSearch {
    private List<Integer> hashSizes;
    private List<Integer> kNeighboursSizes;
    private List<Integer> numHashTables;
    private List<String> distanceTypes;

    public LshGridSearch(List<Integer> hashSizes, List<Integer> kNeighboursSizes, List<Integer> numHashTables, List<String> distanceTypes) {
        this.hashSizes = hashSizes;
        this.kNeighboursSizes = kNeighboursSizes;
        this.numHashTables = numHashTables;
        this.distanceTypes = distanceTypes;
    }

    public Properties calculateOptimalProperties(List<Vector> trainingDataset, List<Vector> validationDataset, Map<String, String> genres) {
        Properties bestProperties = new Properties();
        Map<Properties, Double> propertyAccuracy = new HashMap<>();
        for (Integer hashSize : hashSizes) {
            for (Integer kNeighboursSize : kNeighboursSizes) {
                for (Integer numHashTable : numHashTables) {
                    for (String distanceType : distanceTypes) {
                        Properties properties = new Properties();
                        properties.put("hashSize", hashSize);
                        properties.put("kNeighboursSize", kNeighboursSize);
                        properties.put("numHashTable", numHashTable);
                        properties.put("distanceType", distanceType);
                        double accuracy = 0.f;
                        int repeat = 3;
                        for (int i = 0; i < repeat; i++) {
                            Lsh lsh = new Lsh(hashSize, kNeighboursSize, numHashTable, distanceType);
                            accuracy += lsh.trainAndTest(trainingDataset, validationDataset, genres);
                        }
                        accuracy /= repeat;
                        properties.put("accuracy", accuracy);
                        System.out.printf("Result for: %s%n", properties.toString());
                        propertyAccuracy.put(properties, accuracy);
                    }
                }
            }
        }
        List<Map.Entry<Properties, Double>> properties = new LinkedList<>(propertyAccuracy.entrySet());
        properties.sort(Map.Entry.<Properties, Double>comparingByValue().reversed());
        System.out.println("Best 10: ");
        properties.subList(0, Math.min(properties.size(), 10)).stream().map(Map.Entry::getKey).forEach(System.out::println);
        bestProperties = properties.get(0).getKey();
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
                List.of(30, 50, 60),
                List.of(15, 20, 25, 30),
                List.of(3, 6, 9),
                List.of("Euclidean", "CityBlock", "Cosine")
        );
        System.out.println("Starting grid search");
        Properties properties = lshGridSearch.calculateOptimalProperties(trainingDataset, validationDataset, genres);
        System.out.println("Finished grid search");
        System.out.println(properties.toString());
    }
}

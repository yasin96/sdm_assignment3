import csv.JsonLoader;
import distance.ComparatorInterface;
import distance.DistanceComputer;
import initialization.Initializer;
import lombok.extern.slf4j.Slf4j;
import structures.HashTable;
import structures.Vector;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
public class Lsh {

    //the hashSize,kNeighboursSize,numHashTables are just some random numbers.

    int hashSize;
    int kNeighboursSize;
    int numHashTables;
    String distanceType;

    public Lsh(int hashSize, int kNeighboursSize, int numHashTables, String distanceType) {
        this.hashSize = hashSize;
        this.kNeighboursSize = kNeighboursSize;
        this.numHashTables = numHashTables;
        this.distanceType = distanceType;
    }

    public List<Vector> createRandomMatrix(int l, int d) {
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


    private void initializeAndCalculateHash(List<HashTable> hashtables, int numFeatures, List<Vector> dataset) {
        for (int n = 0; n < numHashTables; n++) {
            HashTable hashTable = new HashTable(createRandomMatrix(hashSize, numFeatures));
            for (Vector vector : dataset) {
                hashTable.add(vector);

            }
            hashtables.add(hashTable);
        }
    }

    private HashMap<String, List<Vector>> findKNearestNeighbours(List<HashTable> tables, String distanceType, List<Vector> dataset) {
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

    public double getAccuracy(HashMap<String, List<Vector>> neighbours_map, Map<String, String> genres) {
        Map<String, String> predictedGenres = neighbours_map.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> {
                            List<String> neighbours = e.getValue().stream().map(vector -> genres.get(vector.getKey())).collect(Collectors.toList());
                            HashMap<String, Integer> counts = new HashMap<>();
                            new HashSet<>(neighbours).forEach(s -> counts.put(s, 0));
                            neighbours.forEach(genre -> counts.put(genre, counts.get(genre) + 1));
                            String genre;
                            if (neighbours.isEmpty()) {
                                genre = "Outlier";
                            } else {
                                genre = Collections.max(counts.entrySet(), Map.Entry.comparingByValue()).getKey();
                            }
                            return genre;
                        }
                ));
        long datasetSize = predictedGenres.size();
        Map<GenrePair, Integer> confusionMatrix = new HashMap<>();
        Set<String> genresSet = new HashSet<>();
        long correctPredictions = predictedGenres.entrySet().stream().filter(stringStringEntry -> {
            String trueGenre = genres.get(stringStringEntry.getKey());
            genresSet.add(trueGenre);
            String predictedGenre = stringStringEntry.getValue();
            GenrePair gp = new GenrePair(trueGenre, predictedGenre);
            if (!confusionMatrix.containsKey(gp)) {
                confusionMatrix.put(gp, 0);
            }
            confusionMatrix.put(gp, confusionMatrix.get(gp) + 1);
            return trueGenre.equals(predictedGenre);
        }).count();
        List<String> genreList = new ArrayList<>(genresSet);
        List<String> predictedGenreList = new ArrayList<>(genreList);
        predictedGenreList.add("Outlier");
        System.out.printf("%13s ", "");
        for (String genre : predictedGenreList) {
            System.out.printf("%13s ", genre);
        }
        System.out.println();
        for (String genre : genreList) {
            System.out.printf("%13s ", genre);
            for (String pGenre : predictedGenreList) {
                GenrePair gp = new GenrePair(genre, pGenre);
                System.out.printf("%13d ", confusionMatrix.getOrDefault(gp, 0));
            }
            System.out.println();
        }
        return correctPredictions / (datasetSize * 1.0f);
    }

    public double trainAndTest(List<Vector> trainDataset, List<Vector> testDataset, Map<String, String> genres) {
        final int numFeatures = trainDataset.get(0).getDimensions();
        List<HashTable> tables = new ArrayList<>();
        initializeAndCalculateHash(tables, numFeatures, trainDataset);
        HashMap<String, List<Vector>> neighbours_map = findKNearestNeighbours(tables, distanceType, testDataset);
        System.out.println("Finished finding neighbours");
        return getAccuracy(neighbours_map, genres);
    }


    public static void main(String[] args) {
        int hashSize = 40;
        int kNeighboursSize = 25;
        int numHashTables = 6;
        String distanceType = "CityBlock";
        Lsh lsh = new Lsh(hashSize, kNeighboursSize, numHashTables, distanceType);
        //Initialize train, validation, test datasets
        final Initializer initializer = new Initializer();
        List<Vector> trainingDataset = initializer.initializeSplitDataSets("fma_metadata\\split\\train.json");
        List<Vector> validationDataset = initializer.initializeSplitDataSets("fma_metadata\\split\\validate.json");
        List<Vector> testDataset = initializer.initializeSplitDataSets("fma_metadata\\split\\test.json");
        JsonLoader jsonLoader = new JsonLoader();
        Map<String, String> genres = jsonLoader.loadGenreJson("fma_metadata\\split\\genres.json");
        List<Vector> trainAndValidationDataset = new LinkedList<>(trainingDataset);
        trainAndValidationDataset.addAll(validationDataset);
        double accuracy = lsh.trainAndTest(trainAndValidationDataset, testDataset, genres);
        System.out.printf("Accuracy: %.2f%n%%", accuracy * 100);

    }

    private static class GenrePair {
        String trueGenre;
        String predictedGenre;

        public GenrePair(String trueGenre, String predictedGenre) {
            this.trueGenre = trueGenre;
            this.predictedGenre = predictedGenre;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            GenrePair genrePair = (GenrePair) o;
            return Objects.equals(trueGenre, genrePair.trueGenre) &&
                    Objects.equals(predictedGenre, genrePair.predictedGenre);
        }

        @Override
        public int hashCode() {
            return Objects.hash(trueGenre, predictedGenre);
        }
    }
}

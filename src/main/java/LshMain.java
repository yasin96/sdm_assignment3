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
    //private List<String[]> columnNames;


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

    public static List<Vector> readCSVFile(final String fileName, final String separator) {

        final List<String[]> data = new ArrayList<>();
        FileReader fileReader;
        BufferedReader in = null;
        try {
            final File file = new File(fileName);
            if (!file.exists()) {
                throw new IllegalArgumentException("File '" + fileName + "' does not exist");
            }
            fileReader = new FileReader(file);
            in = new BufferedReader(fileReader);
            String inputLine;
            int count = 0;
            //skips header rows
            while (count < 4) {
                in.readLine();
                count++;
            }

            inputLine = in.readLine();
            while (inputLine != null) {
                final String[] row = inputLine.split(separator);
                data.add(row);
                inputLine = in.readLine();
            }
        } catch (final IOException i1) {
            System.out.println("Can't open file:" + fileName);
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    //ignore
                }
            }
        }
        List<Vector> result = new ArrayList<>();
        boolean isFirstColumnKey = true;
        int dimensions = isFirstColumnKey ? data.get(0).length - 1 : data.get(0).length;
        int startIndex = isFirstColumnKey ? 1 : 0;
        for (String[] row : data) {
            Vector item = new Vector(dimensions);
            if (isFirstColumnKey) {
                item.setKey(row[0]);
            }
            for (int d = startIndex; d < row.length; d++) {
                double value = Double.parseDouble(row[d]);
                item.set(d - startIndex, value);
            }
            result.add(item);
        }
        return result;
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
            Set<Vector> candidateSet = new HashSet<>();
            for (HashTable table : tables) {
                List<Vector> v = table.query(queryVec);
                candidateSet.addAll(v);
            }
            //log.info("---------------------------------------------");
            //log.info("Candidates for vector: " + queryVec.getKey());
            //candidateSet.forEach(c ->  log.info(c.getKey()));
            //log.info("---------------------------------------------");

            //part c) of the algorithm in assignment
            //TODO part of assignment - think about how to treat cases where there are less then k music tracks found as similar ti
            List<Vector> candidates = new ArrayList<>(candidateSet);
            DistanceComputer measure = new DistanceComputer(distanceType);
            ComparatorInterface dc = new ComparatorInterface(queryVec, measure);
            candidates.sort(dc);
            if (candidates.size() > kNeighboursSize) {
                candidates = candidates.subList(0, kNeighboursSize);
            }
            log.info(String.valueOf(candidates.size()));
        }
    }



    public static void main(String[] args) {
        //Initialize train, validation, test datasets
        final Initializer initializer = new Initializer();
        trainingDataset = initializer.initializeSplitDataSets("train.json");
        validationDataset = initializer.initializeSplitDataSets("validate.json");
        testDataset = initializer.initializeSplitDataSets("test.json");
        //List<Vector> dataset = readCSVFile("fma_metadata/features.csv", ",");

        final int numValidationData = validationDataset.size();
        final int numObservations = trainingDataset.size();
        final int numFeatures = trainingDataset.get(0).getDimensions();

        //creation of hash tables
        List<HashTable> tables = new ArrayList<>();
        initializeAndCalculateHash(tables, numFeatures);

        //part b) of the algorithm in assignment (this could be done in a separate function not in Main)
        findCandidatesAndCalculateDistance(tables, "Euclidean");

    }
}

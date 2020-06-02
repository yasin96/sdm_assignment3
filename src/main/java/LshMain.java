import distance.ComparatorInterface;
import distance.DistanceComputer;
import structures.HashTable;
import structures.Vector;

import java.io.BufferedReader;
import java.io.File;

import java.io.FileReader;
import java.io.IOException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

public class LshMain {
    //TODO fill these sets
    private List<String[]> trainingSet;
    private List<String[]> validationSet;
    private List<String[]> testSet;
    private List<String[]> columnNames;

    public static List<Vector> createRandomMatrix(int l, int d) {
        List<Vector> randomMatrix = new ArrayList<Vector>();
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

        final List<String[]> data = new ArrayList<String[]>();
        FileReader fileReader = null;
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
        List<Vector> result = new ArrayList<Vector>();
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

    public static void main(String[] args) {
        List<Vector> dataset = readCSVFile("fma_metadata/features.csv", ",");
        //TODO these two rows are not needed when the split of train/validation/test data is done.
        Collections.shuffle(dataset);
        List<Vector> validationDataset = dataset.subList(0, 800);

        int numValidationData = validationDataset.size();
        //the hashSize,kNeighboursSize,numHashTables are just some random numbers.
        //TODO we should find some values that perform good on the verification data set
        int numObservations = dataset.size();
        int numFeatures = dataset.get(0).getDimensions();
        int hashSize = 50;
        int kNeighboursSize = 4;
        int numHashTables = 4;
        //creation of hash tables
        List<Vector> randomMatrix = createRandomMatrix(hashSize, numFeatures);
        List<HashTable> tables = new ArrayList<HashTable>();
        for (int n = 0; n < numHashTables; n++) {
            HashTable hashTable = new HashTable(randomMatrix);
            for (Vector vector : dataset) {
                hashTable.add(vector);
            }
            tables.add(hashTable);
        }

        //part b) of the algorithm in assignment (this could be done in a separate function not in Main)
        for (Vector queryVec : validationDataset) {
            Set<Vector> candidateSet = new HashSet<Vector>();
            for (HashTable table : tables) {
                List<Vector> v = table.query(queryVec);
                candidateSet.addAll(v);
            }
            //part c) of the algorithm in assignment
            //TODO part of assignment - think about how to treat cases where there are less then k music tracks found as similar ti
            List<Vector> candidates = new ArrayList<Vector>(candidateSet);
            DistanceComputer measure = new DistanceComputer("Euclidean");
            ComparatorInterface dc = new ComparatorInterface(queryVec, measure);
            candidates.sort(dc);
            if (candidates.size() > kNeighboursSize) {
                candidates = candidates.subList(0, kNeighboursSize);
            }
            System.out.println(candidates.size());
        }

    }
}

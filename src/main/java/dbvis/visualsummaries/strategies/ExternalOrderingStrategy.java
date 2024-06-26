/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dbvis.visualsummaries.strategies;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import dbvis.visualsummaries.data.DataPoint;

import static java.util.stream.Collectors.*;
import static java.util.Map.Entry.*;

/**
 * Allows to upload orderings generated by external methods. Reads a csv file
 * with one timestep per line. Each line contains the projected positions of the
 * entities in ascending order (id 0...n). The order has to be a numeric,
 * decimal value. The list will be sorted ascending according to the order
 * values.
 *
 *
 * CAUTION! THIS STRATEGY ALLOWS YOU TO LOAD OWN ORDERINGS. THE UPLOADED
 * ORDERINGS NEED TO BE OF THE SAME DATASET AS THE DATASET CHOSEN IN THE TOOL!
 *
 * @author Juri Buchmueller <motionrugs@dbvis.inf.uni-konstanz.de>
 */
public class ExternalOrderingStrategy implements Strategy {

    private String pathToDataFile = "C:\\data\\results";

    @Override
    public String getName() {
        return "External Ordering";
    }

    @Override
    public DataPoint[][] getOrderedValues(DataPoint[][] unsorted, String dsName) {
        String line;

        for (DataPoint dp : unsorted[234]) {
            System.out.println(dp.getId());
        }

        try (BufferedReader br = new BufferedReader(new FileReader(pathToDataFile))) {

            HashMap<String, Double> results = new HashMap<>();

            ArrayList<Map<String, Double>> sortedResults = new ArrayList<>();

            while ((line = br.readLine()) != null) {

                line = line.replace(" 0. ", "");
                line = line.replace(" ", "");
                String[] result = line.split(",");
                for (int i = 0; i < result.length; i++) {
                    results.put(i + "", Double.parseDouble(result[i]));
                }

                Map<String, Double> valuesorted = results
                        .entrySet()
                        .stream()
                        .sorted(comparingByValue())
                        .collect(
                                toMap(e -> e.getKey(), e -> e.getValue(), (e1, e2) -> e2, LinkedHashMap::new));

                sortedResults.add(valuesorted);

                System.out.println("---------------------------------------------------------");
            }

            System.out.println(sortedResults.size());

            DataPoint[][] sorted = new DataPoint[sortedResults.size()][sortedResults.get(0).size()];
            for (int frame = 0; frame < sortedResults.size(); frame++) {

                Map<String, Double> sorteds = sortedResults.get(frame);

                int i = 0;
                for (String key : sorteds.keySet()) {
                    sorted[frame][i] = unsorted[frame][Integer.parseInt(key)];
                    //System.out.println("Sorted index: " + i +": ID" + sorted[frame][i].getId() + " Unsorted index: " + Integer.parseInt(key));
                    i++;
                }
            }
            return sorted;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}

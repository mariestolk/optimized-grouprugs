package dbvis.visualsummaries.grouprugs.visualization;

import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import javax.imageio.ImageIO;

public class SaveResults {

    /**
     * Saves the results of the visualization to a PNG image and a CSV file, in the
     * same folder within the results directory.
     * 
     * @param img           The BufferedImage to save.
     * @param etp           The entity-to-position map to save.
     * @param directoryname The name of the directory to save the image as.
     */
    public static void saveResults(
            BufferedImage img,
            Integer[][] etp,
            String directoryname) {

        // Define the path to save the results to
        String mainResultsPath = "src" + File.separator
                + "main" + File.separator
                + "java" + File.separator
                + "dbvis" + File.separator
                + "visualsummaries" + File.separator
                + "grouprugs" + File.separator
                + "visualization" + File.separator
                + "results" + File.separator;

        File mainResultsDir = new File(mainResultsPath);

        // Create the results directory if it does not exist
        if (!mainResultsDir.exists()) {
            mainResultsDir.mkdirs();
            System.out.println("Results directory created.");
        }

        // Define path to nested directory within results directory
        String resultsPath = mainResultsPath + File.separator + directoryname + File.separator;
        File resultsDir = new File(resultsPath);

        // Create the nested directory if it does not exist
        if (!resultsDir.exists()) {
            resultsDir.mkdirs();
            System.out.println("Results subdirectory created.");

            // Save the image to the nested directory
            File imgFile = new File(resultsPath + "image.png");
            try {
                ImageIO.write(img, "png", imgFile);
                System.out.println("Image saved to " + imgFile.getAbsolutePath());
            } catch (IOException e) {
                System.err.println("Error saving image.");
                e.printStackTrace();
            }

            // Save the entity-to-position map to a CSV file in the nested directory
            File etpFile = new File(resultsPath + "etp.csv");
            try {
                BufferedWriter writer = new BufferedWriter(new FileWriter(etpFile));
                for (int i = 0; i < etp.length; i++) {
                    for (int j = 0; j < etp[0].length; j++) {
                        writer.write(etp[i][j] + ",");
                    }
                    writer.write("\n");
                }
                writer.close();
                System.out.println("Entity-to-position map saved to " + etpFile.getAbsolutePath());
            } catch (IOException e) {
                System.err.println("Error saving entity-to-position map.");
                e.printStackTrace();
            }
        } else {
            System.err.println("Results directory already exists.");
        }

    }

    public static void main(String[] args) {
        // Example usage
        BufferedImage exampleImage = new BufferedImage(100, 100, BufferedImage.TYPE_INT_ARGB); // Placeholder image
        Integer[][] exampleEtpMap = { { 1, 2, 3 }, { 4, 5, 6 }, { 7, 8, 9 } }; // Placeholder ETP map
        String exampleFilename = "example.png";

        saveResults(exampleImage, exampleEtpMap, exampleFilename);
    }

}

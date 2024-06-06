package dbvis.visualsummaries.grouprugs.visualization;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageIO;

import dbvis.visualsummaries.color.TwoDColorMapper;
import dbvis.visualsummaries.data.DataPoint;
import dbvis.visualsummaries.data.DataSet;
import dbvis.visualsummaries.data.SessionData;

/**
 * Class for creating PNG images of the groupRugs.
 */
public class PNGWriter {

    public PNGWriter() {

    }

    /**
     * Function draws the groupRugs based on the given entityToPosition array.
     * 
     * @param etp                2D array of entity positions.
     * @param sd                 SessionData object.
     * @param dsname             Name of the dataset.
     * @param ENLARGEMENT_FACTOR Enlargement factor.
     * 
     * @return BufferedImage object.
     */
    public BufferedImage drawRugs(
        Integer[][] etp,
        SessionData sd, 
        String dsname, 
        int ENLARGEMENT_FACTOR, 
        double eps,
        boolean MR) {

        DataSet current = sd.getDataset(dsname);
        DataPoint[][] data = current.getBaseData();
        TwoDColorMapper twodcolormapper = initializeColorMapper(current);

        BufferedImage img;

        if (MR) {
            img = initializeImage2(etp);
        } else {
            img = initializeImage(etp, ENLARGEMENT_FACTOR);
        }

        for (int frame = 0; frame < etp.length; frame++) {

            for (int entity = 0; entity < etp[0].length; entity++) {

                // System.out.println("frame: " + frame + " entity: " + entity + " location: " +
                // etp[frame][entity]);

                int location = etp[frame][entity];

                img.setRGB(frame, location,
                        twodcolormapper
                                .getColor1((int) Math.floor(data[frame][entity].getX()),
                                        (int) Math.floor(data[frame][entity].getY()))
                                .getRGB());

            }
        }

        // Get image height and width
        int width = img.getWidth();
        int height = img.getHeight();

        // get color white
        int white = 0xFFFFFF;

        for (int i = 50; i < width; i += 50) {
            img.setRGB(i, height - 3, white);
            img.setRGB(i, height - 2, white);
            img.setRGB(i, height - 1, white);
        }

        saveImage(dsname, img, eps);
        return img;

    }

    /**
     * Function initializes the TwoDColorMapper object.
     * 
     * @param current DataSet object.
     * @return TwoDColorMapper object.
     */
    private TwoDColorMapper initializeColorMapper(DataSet current) {
        TwoDColorMapper twodcolormapper = new TwoDColorMapper();
        int maxx = (int) Math.ceil(current.getMax("x"));
        int maxy = (int) Math.ceil(current.getMax("y"));
        twodcolormapper.setScale(maxx, maxy);

        return twodcolormapper;

    }

    /**
     * Function initializes the BufferedImage object.
     * 
     * @param etp                2D array of entity positions.
     * @param ENLARGEMENT_FACTOR Enlargement factor.
     * @return BufferedImage object.
     */
    private BufferedImage initializeImage(Integer[][] etp, int ENLARGEMENT_FACTOR) {
        BufferedImage img = new BufferedImage(etp.length, ENLARGEMENT_FACTOR * etp[0].length,
                BufferedImage.TYPE_INT_RGB);

        // Set background color to white
        for (int i = 0; i < etp.length; i++) {
            for (int j = 0; j < ENLARGEMENT_FACTOR * etp[0].length; j++) {
                img.setRGB(i, j, 0xFFFFFF);
            }
        }

        return img;
    }

    /**
     * Initialize image
     */
    private BufferedImage initializeImage2(Integer[][] etp) {

        int BUFFER = 1;

        // get max and min value in etp
        int max = 0;
        int min = Integer.MAX_VALUE;
        for (int i = 0; i < etp.length; i++) {
            for (int j = 0; j < etp[0].length; j++) {
                if (etp[i][j] > max) {
                    max = etp[i][j];
                } else if (etp[i][j] < min) {
                    min = etp[i][j];
                }
            }
        }

        // create image
        BufferedImage img = new BufferedImage(etp.length, (max + min) + BUFFER, BufferedImage.TYPE_INT_RGB);

        return img;

    }

    /**
     * Function saves the image to the disk.
     * 
     * @param dsname Name of the dataset.
     * @param img    BufferedImage object.
     */
    private void saveImage(String dsname, BufferedImage img, double eps) {

        // TODO: Get todays date and time and save image with that name
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
        String formatDateTime = now.format(formatter);

        // Save image
        String userdir = System.getProperty("user.home");
        File imgfolder = new File(userdir + "/motionrugs/grouprugs");
        if (!imgfolder.exists()) {
            imgfolder.mkdir();
        }

        File outputfile = new File(
                imgfolder + "/new" +
                        dsname + "_"
                        + formatDateTime + "_"
                        + "eps" + eps + ".png");
        try {
            ImageIO.write(img, "png", outputfile);
        } catch (IOException ex) {
            Logger.getLogger(PNGWriter.class.getName()).log(Level.SEVERE, null, ex);
        }

    }
}

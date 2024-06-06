package dbvis.visualsummaries.data;

import java.util.Date;
import java.util.HashMap;
import java.util.Set;

/**
 * One DataPoint represents one mover at a specific point in space and the
 * values of the measured features of the mover (e.g. speed) at the time and
 * place.
 *
 * @author Juri Buchmüller, University of Konstanz
 *         <buchmueller@dbvis.inf.uni-konstanz.de>
 */
public class DataPoint {

    private double x;
    private double y;
    private HashMap<String, Double> values;
    private Date date;
    private int id;

    /**
     * @param x  the movers position in x
     * @param y  the movers position in y
     * @param id id of the mover
     */
    public DataPoint(double x, double y, int id) {
        this.x = x;
        this.y = y;
        values = new HashMap<>();
        this.id = id;
    }

    /**
     * Returns the movers x-coordinate
     *
     * @return
     */
    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    /**
     * Returns the movers y-coordinate
     *
     * @return
     */
    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    /**
     * Returns the value of the queried feature
     * 
     * @param feature the feature for which the value should be returned
     * @return the value of the requested feature
     */
    public double getValue(String feature) {
        return values.get(feature);
    }

    /**
     * Returns a list of available features.
     * 
     * @return
     */
    public Set<String> getAvailableFeatures() {
        return values.keySet();
    }

    /**
     * Adds a feature value to this DataPoint
     * 
     * @param feature the id of the feature to add
     * @param value   the feature value to add
     */
    public void putValue(String feature, double value) {
        values.put(feature, value);
    }

    /**
     * Time of the movement
     * 
     * @return the time the record
     */
    public Date getDate() {
        return date;
    }

    /**
     * Sets the time of the record
     * 
     * @param date time of the record
     */
    public void setDate(Date date) {
        this.date = date;
    }

    /**
     * Returns the movers id
     * 
     * @return the movers id
     */
    public int getId() {
        return id;
    }

    /**
     * Sets the movers id
     * 
     * @param id the movers id
     */
    public void setId(int id) {
        this.id = id;
    }
}

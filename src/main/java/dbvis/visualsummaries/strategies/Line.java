/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dbvis.visualsummaries.strategies;

/**
 *
 * @author Jules Wulms, TU Eindhoven <j.j.h.m.wulms@tue.nl>
 */
public class Line {
    
    // a point on the line
    private Vector point;
    // the direction of the line
    private Vector direction;
    
    public Line(Vector point, Vector direction) {
        this.point =  point;
        this.direction = direction.normalize();
    }
    
    public Vector getPoint() {
        return point;
    }
    
    public Vector getDirection() {
        return direction;
    }
    
    /**
     * Returns projection of {@code point} to this line
     * 
     * @param point point to be projected to this line
     * @return projection of {@code point} to this line
     */
    public Vector projectionVector(Vector point) {
        double scalar = projectionScalar(point);
        // to find the scalar we pretended the line goes through origin
        // we now move the line back so that it goes through this.point
        // then walk along the line to the projected point
        return point.add(direction.scale(scalar));
    }
    
    /**
     * Returns the scale factor for {@code this.direction} to find the projection
     * of {@code point} to this line, when origin of {@code this.direction} is at {@code this.point}
     * 
     * @param point point to be projected to this line
     * @return projection of {@code point} to this line
     */
    public double projectionScalar(Vector point) {
        // we pretend this line is through the origin by subtraction this.point from point
        // direction is a unit vector so we find scalar projection by simply taking dot product
        return direction.dotProduct(point.subtract(this.point));
    }
    
    /**
     * Retruns absolute difference in angle between {@code this.direction} and 
     * {@code otherDir.
     * 
     * @param otherDir vector to find angle difference to
     * @return absolute difference in angle
     */
    public double angleDifference(Vector otherDir) {
        double oldAngle, newAngle, diff;
        oldAngle = Math.atan2(otherDir.getY(), otherDir.getX());
        newAngle = Math.atan2(direction.getY(), direction.getX());
        diff = Math.abs(oldAngle - newAngle);
        
        return diff;
    }
    
    /**
     * In case {@code direction} is flipped ~180 degrees with respect to {@code previousDir}
     * change {@code direction} by exactly 180 degrees.
     * 
     * @param previousDir vector to check {@code direction} against
     */
    public void adjustForFlip(Vector previousDir) {
        double diff = angleDifference(previousDir);
        
        if(diff > Math.PI*0.9 && diff < Math.PI*1.1) {
            direction = new Vector(-direction.getX(), -direction.getY());
            direction.normalize();
        }
    }
    
    /**
     * In case {@code direction} is has an angle >90 degrees with respect to 
     * {@code previousDir} change {@code direction} by exactly 180 degrees.
     * 
     * @param previousDir vector to check {@code direction} against
     */
    public void alignAngle(Vector previousDir) {
        double diff = angleDifference(previousDir);
        
        if(diff > Math.PI/2 && diff < Math.PI*3/2) {
            direction = new Vector(-direction.getX(), -direction.getY());
            direction.normalize();
        }
    }
}

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
public class Vector {
    
    /** x-coordinate of vector */
    private double x;
    /** y-coordinate of vector */
    private double y;
    
    public Vector(double x, double y) {
        this.x = x;
        this.y = y;
    }
    
    public double getX() {
        return x;
    }
    
    public double getY() {
        return y;
    }
    
    /**
     * Returns the dot product between this vector and {@code v}
     * 
     * @param v vector to calculate dot product with
     * @return dot product of this vector and {@code v}
     */
    public double dotProduct(Vector v) {
        return this.x * v.x + this.y * v.y;
    }
    
    /**
     * Returns the length of this vector
     * 
     * @return length of this vector 
     */
    public double length() {
        return Math.sqrt(x * x + y * y);
    }
    
    /**
     * Returns a unit vector in direction of this vector
     * 
     * @return unit vector in direction of this vector
     */
    public Vector normalize() {
        double l = length();
        if (Math.abs(l - 0) < 0.000001) {
            return new Vector(0, 0);
        } else {
            return new Vector(x / l, y / l);
        }
    }
    
    /**
     * Returns a this vector scaled by {@code f}
     * 
     * @param f scaling factor
     * @return this vector scaled by {@code f}
     */
    public Vector scale(double f) {
        return new Vector(f * x, f * y);
    }
    
    /**
     * Returns addition of this vector and {@code v}
     * 
     * @param v vector to add to this vector
     * @return addition of this vector and {@code v}
     */
    public Vector add(Vector v) {
        return new Vector(this.x + v.x, this.y + v.y);
    }
    
    /**
     * Returns subtraction of this vector with {@code v}
     * 
     * @param v vector to subtract from this vector
     * @return addition of this vector with {@code v}
     */
    public Vector subtract(Vector v) {
        return new Vector(this.x - v.x, this.y - v.y);
    }
}

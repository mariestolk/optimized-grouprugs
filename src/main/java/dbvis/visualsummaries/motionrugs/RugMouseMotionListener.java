/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dbvis.visualsummaries.motionrugs;

/**
 *
 * @author Juri Buchmueller <motionrugs@dbvis.inf.uni-konstanz.de>
 */
public interface RugMouseMotionListener {
    
    public void rugXHasChanged(int x);
    
    public void rugStratVisualizable(boolean visualizable, String stratid);
    
}

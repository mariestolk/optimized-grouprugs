package dbvis.visualsummaries.color;

import java.awt.Color;

/**
 * Color mapping interface for different color mappers
 *
 * @author Juri Buchmüller, University of Konstanz
 * <buchmueller@dbvis.inf.uni-konstanz.de>
 */
public interface ColorMapper {
    
    public Color getColorByValue(double value) throws Exception;
    
}

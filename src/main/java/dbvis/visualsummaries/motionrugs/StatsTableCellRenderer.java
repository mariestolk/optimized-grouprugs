/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dbvis.visualsummaries.motionrugs;

import java.awt.Color;
import java.awt.Component;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

import dbvis.visualsummaries.color.StaticLinearHSVColorMapper;
import dbvis.visualsummaries.data.DataSet;
import dbvis.visualsummaries.data.SessionData;

/**
 *
 * @author buchmueller
 */
public class StatsTableCellRenderer extends DefaultTableCellRenderer {

    Color stdcolor;
    boolean isFirst = true;
    String stratid;

    public StatsTableCellRenderer(String stratid) {
        super();
        this.stratid = stratid;
    }

    @Override
    public Component getTableCellRendererComponent(
            JTable aTable, Object aNumberValue, boolean aIsSelected,
            boolean aHasFocus, int aRow, int aColumn) {
        Component renderer = super.getTableCellRendererComponent(
                aTable, aNumberValue, aIsSelected, aHasFocus, aRow, aColumn
        );
        if (isFirst) {
            stdcolor = renderer.getBackground();
            isFirst = false;
        }
        try {
            double maxval = -1;
            double actual = Double.parseDouble((String) aNumberValue);
            double minval = -1;

            String type = aTable.getModel().getValueAt(aRow, 0).toString();
            String stat = aTable.getModel().getColumnName(aColumn);
            String id = null;

            DataSet ds = SessionData.getInstance().getCurrentDataSet();

            if (type.equals("Jumps")) {
                if (stat.equals("Mean")) {
                    id = "jumpsmean";
                } else if (stat.equals("Median")) {
                    id = "jumpsmedian";
                } else if (stat.equals("StDev")) {
                    id = "jumpsstdev";
                }
            } else if (type.equals("Cross")) {
                if (stat.equals("Mean")) {
                    id = "crossmean";
                } else if (stat.equals("Median")) {
                    id = "crossmedian";
                } else if (stat.equals("StDev")) {
                    id = "crossstdev";
                }
            } else if (type.equals("Kendall")) {
                if (stat.equals("Mean")) {
                    id = "kendallsmean";
                } else if (stat.equals("Median")) {
                    id = "kendallsmedian";
                } else if (stat.equals("StDev")) {
                    id = "kendallsstdev";
                }
            } else if (type.equals("Spatial q. (dist)")) {
                if (stat.equals("Mean")) {
                    id = "KSdistmean";
                } else if (stat.equals("Median")) {
                    id = "KSdistmedian";
                } else if (stat.equals("StDev")) {
                    id = "KSdiststdev";
                }
            } else if (type.equals("Spatial q. (rank)")) {
                if (stat.equals("Mean")) {
                    id = "KSrankmean";
                } else if (stat.equals("Median")) {
                    id = "KSrankmedian";
                } else if (stat.equals("StDev")) {
                    id = "KSrankstdev";
                }
            } else if (type.equals("Stability")) {
                if (stat.equals("Mean")) {
                    id = "KSprojmean";
                } else if (stat.equals("Median")) {
                    id = "KSprojmedian";
                } else if (stat.equals("StDev")) {
                    id = "KSprojstdev";
                }
            } else if (type.equals("KSdistInput")) {
                if (stat.equals("Mean")) {
                    id = "KSdistInputmean";
                } else if (stat.equals("Median")) {
                    id = "KSdistInputmedian";
                } else if (stat.equals("StDev")) {
                    id = "KSdistInputstdev";
                }
            } else if (type.equals("KSrankInput")) {
                if (stat.equals("Mean")) {
                    id = "KSrankInputmean";
                } else if (stat.equals("Median")) {
                    id = "KSrankInputmedian";
                } else if (stat.equals("StDev")) {
                    id = "KSrankInputstdev";
                }
            }

            maxval = Math.round(ds.getGlobalStatMax(id) * 1000.0) / 1000.0;
            minval = Math.round(ds.getGlobalStatMin(id) * 1000.0) / 1000.0;

            Color[] colormap = new Color[]{new Color(202, 0, 32),
                new Color(244, 165, 130),
                new Color(247, 247, 247),
                new Color(146, 197, 222),
                new Color(5, 113, 176)};

            //System.out.println(id + ": " + minval + " / " + actual + " / " + maxval);
            Color[] steps;
            if (id.equals("kendallsmean") || id.equals("kendallsmedian")) {
                steps = colormap;
            } else {
                List<Color> list = Arrays.asList(colormap);
                Collections.reverse(list);
                steps = list.toArray(new Color[0]); 
            }

            Color background = stdcolor;
            try {
                background = StaticLinearHSVColorMapper.getColorByValue(actual, minval, maxval, steps, stdcolor);
            } catch (Exception ex) {
                Logger.getLogger(StatsTableCellRenderer.class.getName()).log(Level.SEVERE, null, ex);
            }

            if (id.contains("stdev")) {
                renderer.setBackground(stdcolor);
            } else {
                renderer.setBackground(background);
            }

        } catch (NumberFormatException e) {
            renderer.setBackground(stdcolor);
        }

        return this;
    }

}

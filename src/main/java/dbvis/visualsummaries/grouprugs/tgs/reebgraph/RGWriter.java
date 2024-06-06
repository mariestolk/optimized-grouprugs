package dbvis.visualsummaries.grouprugs.tgs.reebgraph;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class RGWriter {

    /**
     * Writes the Reeb graph to a file.
     * 
     * @param rg       The Reeb graph.
     * @param filename The filename.
     * @throws IOException If an I/O error occurs
     */
    public static void write(ReebGraph rg, String filename) throws IOException {

        // Create folder if it does not exist
        String userdir = System.getProperty("user.home");
        File imgfolder = new File(userdir + "/motionrugs/reebgraphs/");
        if (!imgfolder.exists()) {
            imgfolder.mkdir();
        }
        // Create new file
        File file = new File(imgfolder, filename);
        FileWriter fw = null;

        try {

            fw = new FileWriter(file);
            for (RVertex v : rg.getVertices()) {
                writeVertex((v), fw);
            }
            for (REdge e : rg.getEdges()) {
                writeEdge(e, fw);
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (fw != null) {
                fw.close();
            }
        }

    }

    /**
     * Writes RVertex to file
     * 
     * @param v      The vertex to be written.
     * @param writer The file writer.
     * @throws IOException If an I/O error occurs
     */
    private static void writeVertex(RVertex v, FileWriter writer) throws IOException {

        writer.write("v " + v.getId() + " " + v.getTime() + " " + v.getType());
        writer.write("\n");

    }

    /**
     * Writes REdge to file
     * 
     * @param e      The edge to be written.
     * @param writer The file writer.
     * @throws IOException If an I/O error occurs
     */
    private static void writeEdge(REdge e, FileWriter writer) throws IOException {
        writer.write(e.getSource().getId() + " " + e.getDest().getId() + " " + e.getReebId() + " "
                + e.writeComponent() + " ");
        writer.write("\n");
    }

    /**
     * Reads the Reeb graph from a file.
     * 
     * 
     */
    public static ReebGraph read(String filename) {
        ReebGraph rg = new ReebGraph();

        String userdir = System.getProperty("user.home");
        File imgfolder = new File(userdir + "/motionrugs/reebgraphs/");
        if (!imgfolder.exists()) {
            imgfolder.mkdir();
        }

        // Check if file exists
        File file = new File(imgfolder, filename);
        if (!file.exists()) {
            System.out.println("File does not exist.");
            return null;
        }

        try {
            ArrayList<RVertex> vertices = new ArrayList<>();
            ArrayList<REdge> edges = new ArrayList<>();

            // Read file
            Scanner sc = new Scanner(file);

            while (sc.hasNextLine()) {
                String line = sc.nextLine();
                String[] parts = line.split(" ");

                if (parts[0].equals("v")) {

                    switch (parts[3]) {
                        case "start":
                            RVertex start = new RVertex(Integer.parseInt(parts[1]), Double.parseDouble(parts[2]),
                                    RVertex.START_VERTEX);
                            vertices.add(start);
                            break;
                        case "end":

                            RVertex end = new RVertex(Integer.parseInt(parts[1]), Double.parseDouble(parts[2]),
                                    RVertex.END_VERTEX);
                            vertices.add(end);
                            break;
                        case "merge":
                            RVertex merge = new RVertex(Integer.parseInt(parts[1]), Double.parseDouble(parts[2]),
                                    RVertex.MERGE_VERTEX);
                            vertices.add(merge);
                            break;
                        case "split":
                            RVertex split = new RVertex(Integer.parseInt(parts[1]), Double.parseDouble(parts[2]),
                                    RVertex.SPLIT_VERTEX);
                            vertices.add(split);
                            break;
                        default:
                            System.out.println("Unknown vertex type: " + parts[2]);
                            break;

                    }

                } else {
                    RVertex source = vertices.get(Integer.parseInt(parts[0]));
                    RVertex dest = vertices.get(Integer.parseInt(parts[1]));

                    List<Integer> component = new ArrayList<>();
                    // separate parts by comma
                    String[] comp = parts[3].split(",");

                    // Remove square brackets
                    comp[0] = comp[0].substring(1);
                    comp[comp.length - 1] = comp[comp.length - 1].substring(0, comp[comp.length - 1].length() - 1);

                    for (String s : comp) {
                        component.add(Integer.parseInt(s));
                    }

                    REdge e = new REdge(source, dest, Integer.parseInt(parts[2]), component);
                    edges.add(e);

                }

            }

            rg.setVertices(vertices);
            rg.setEdges(edges);

            sc.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

        return rg;
    }

}
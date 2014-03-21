/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package scouting.summary;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class TSVDoc {

    String[] header;
    String[][] data;

    /**
     * Delimeters: Tabs
     *
     * @param file
     */
    public TSVDoc(String file) {

        String[] lines;
        ArrayList<String> s = new ArrayList<>();
        try {
            BufferedReader r = new BufferedReader(new FileReader(file));
            String u;
            while ((u = r.readLine()) != null) {
                s.add(u);
            }
        } catch (IOException e) {
            e.printStackTrace(System.err);
        }
        lines = s.toArray(new String[s.size()]);

        if (lines.length > 0) {
            header = lines[0].split(" *\t *");
            data = new String[lines.length - 1][];
            for (int i = 1; i < lines.length; i++) {
                data[i - 1] = lines[i].split(" *\t *");
            }
        } else {
            header = new String[0];
            data = new String[0][];
        }
    }

    public TSVDoc() {
        header = new String[0];
        data = new String[0][];
    }
}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package scouting.summary;

public class TeamRecord {

    private static double[] hist(int[] h) {
        double[] o = new double[3];
        if (h.length == 0) {
            return o;
        }

        o[0] = h[h.length - 1];
        int j = 0;
        for (int i = 0; i < h.length; i++) {
            if (i + 3 >= h.length) {
                j++;
                o[1] += h[i];
            }
            o[2] += h[i];
        }
        o[1] /= (double) ((h.length > 3) ? 3 : h.length);
        o[2] /= (double) (h.length);

        return o;
    }

    private final int[][] matches;
    private final String[] matchheader;
    private final String[] scouting;
    private final int team;

    public TeamRecord(int team, String[] matchheader, int[][] matches, String[] scouting, String[] scoutheader) {
        this.matches = matches;
        this.matchheader = matchheader;
        this.scouting = new String[scouting.length];
        for (int i = 0; i < scouting.length; i++) {
            this.scouting[i] = scoutheader[i] + ": " + scouting[i];
        }
        this.team = team;
    }

    public int getTeamNum() {
        return team;
    }

    private int getFieldCombination(SII[] combo, int match) {
        int a = 0;
        for (SII s : combo) {
            a += getField(match, s.key) * s.scale;
        }
        return a;
    }

    private double[] getMetric(SII[] combo) {
        int[] v = new int[getMatchCount()];
        for (int i = 0; i < v.length; i++) {
            v[i] = getFieldCombination(combo, i);
        }
        return hist(v);
    }

    public double[] getSuccesses(GraphRules g) {
        return getMetric(g.pro);
    }

    public double[] getFailures(GraphRules g) {
        return getMetric(g.con);
    }

    public int getMatchCount() {
        return matches.length;
    }

    public int getMaxHeight(GraphRules g) {
        return g.max;
    }

    public int[] getShades(GraphRules g) {
        SII[] p = g.plot;
        int[] d = new int[p.length];
        for (int i = 0; i < d.length; i++) {
            d[i] = p[i].color;
        }
        return d;
    }

    public int[] getScales(GraphRules g) {
        SII[] p = g.plot;
        int[] d = new int[p.length];
        for (int i = 0; i < d.length; i++) {
            d[i] = p[i].scale;
        }
        return d;
    }

    public int[][] getGraphData(GraphRules g) {
        SII[] p = g.plot;
        int[][] d = new int[getMatchCount()][p.length];
        for (int j = 0; j < p.length; j++) {
            int k = keyToInt(p[j].key);
            for (int i = 0; i < d.length; i++) {
                d[i][j] = matches[i][k];
            }
        }
        return d;
    }

    public String[] getScouting() {
        return scouting;
    }

    private int keyToInt(String key) {
        for (int i = 0; i < matchheader.length; i++) {
            if (matchheader[i].equals(key)) {
                return i;
            }
        }
        System.err.format("Can't find key %s\n", key);
        return -1;
    }

    public int getField(int match, String key) {
        int idx = keyToInt(key);
        if (idx == -1) {
            return 0;
        }
        return matches[match][idx];
    }
}

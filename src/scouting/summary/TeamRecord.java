/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package scouting.summary;

public class TeamRecord {

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

    public double[] getSuccesses(GraphRules g) {
        int[] v = new int[getMatchCount()];
        for (int i = 0; i < v.length; i++) {
            v[i] = g.getSuccess(this, i);
        }
        return hist(v);
    }

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

    public double[] getFailures(GraphRules g) {
        int[] v = new int[getMatchCount()];
        for (int i = 0; i < v.length; i++) {
            v[i] = g.getFailure(this, i);
        }
        return hist(v);
    }

    public int getMatchCount() {
        return matches.length;
    }

    public int getMaxHeight(GraphRules g) {
        return g.getGraphMax();
    }

    public int[] getShades(GraphRules g) {
        SKS[] p = g.getGraphRules();
        int[] d = new int[p.length];
        for (int i = 0; i < d.length; i++) {
            d[i] = p[i].intensity;
        }
        return d;
    }

    public int[] getScales(GraphRules g) {
        SKS[] p = g.getGraphRules();
        int[] d = new int[p.length];
        for (int i = 0; i < d.length; i++) {
            d[i] = p[i].scale;
        }
        return d;
    }

    public int[][] getGraphData(GraphRules g) {
        SKS[] p = g.getGraphRules();
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
        return matches[match][keyToInt(key)];
    }

    public static GraphRules makeRules(final String name, final String[] pro,
            final String[] con, final int max, final SKS[] chart) {
        return new GraphRules() {

            @Override
            public int getSuccess(TeamRecord r, int m) {
                int a = 0;
                for (String s : pro) {
                    a += r.getField(m, s);
                }
                return a;
            }

            @Override
            public int getFailure(TeamRecord r, int m) {
                int a = 0;
                for (String s : con) {
                    a += r.getField(m, s);
                }
                return a;
            }

            @Override
            public int getGraphMax() {
                return max;
            }

            @Override
            public SKS[] getGraphRules() {
                return chart;
            }

            @Override
            public String getName() {
                return name;
            }
        };
    }
}

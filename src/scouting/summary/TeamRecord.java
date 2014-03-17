/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package scouting.summary;

public class TeamRecord {

    public static final String[] keys = {
        "Match",
        "AHighHot",
        "AHighCold",
        "AHighMiss",
        "ALowHot",
        "ALowCold",
        "ALowMiss",
        "AMobility",
        "Truss",
        "TrussMiss",
        "Low",
        "LowMiss",
        "High",
        "HighMiss",
        "Catch",
        "CatchMiss",
        "G12",
        "G28",
        "G40",
        "OtherTech"};

    public static String[] sck = {
        "Drive", "Intake", "Shooter", "Loads", "Goals"
    };

    private final int[][] matches;
    private final String[] scouting;
    private final int team;

    public TeamRecord(int team, int[][] matches, String[] scouting) {
        this.matches = matches;
        this.scouting = new String[scouting.length];
        for (int i = 0; i < scouting.length; i++) {
            this.scouting[i] = sck[i] + ": " + scouting[i];
        }
        this.team = team;
    }

    public int getTeamNum() {
        return team;
    }

    public double[] getSuccesses(String key) {
        Subgraph g = getSubgraph(key);

        double[] a = new double[3];
        int l = getMatchCount() - 1;

        switch (l) {
            case -1:
                a[0] = a[1] = a[2] = 0.0;
                break;
            case 0:
                a[0] = a[1] = a[2] = g.getSuccess(0);
                break;
            case 1:
                a[0] = g.getFailure(1);
                a[1] = a[2] = (a[0] + g.getSuccess(0)) / 2.0;
                break;
            default:
                a[0] = g.getSuccess(l);
                a[1] = (g.getSuccess(l) + g.getSuccess(l - 1) + g.getSuccess(l - 2)) / 3.0;
                a[2] = 0.0;
                for (int i = 0; i <= l; i++) {
                    a[2] += g.getSuccess(i);
                }
                a[2] /= l + 1;
                break;
        }

        return a;
    }

    public double[] getFailures(String key) {
        Subgraph g = getSubgraph(key);

        double[] a = new double[3];
        int l = getMatchCount() - 1;

        switch (l) {
            case -1:
                a[0] = a[1] = a[2] = 0.0;
                break;
            case 0:
                a[0] = a[1] = a[2] = g.getFailure(0);
                break;
            case 1:
                a[0] = g.getFailure(1);
                a[1] = a[2] = (a[0] + g.getFailure(0)) / 2.0;
                break;
            default:
                a[0] = g.getFailure(l);
                a[1] = (g.getFailure(l) + g.getFailure(l - 1) + g.getFailure(l - 2)) / 3.0;
                a[2] = 0.0;
                for (int i = 0; i <= l; i++) {
                    a[2] += g.getFailure(i);
                }
                a[2] /= l + 1;
                break;
        }
        return a;
    }

    private Subgraph getSubgraph(String key) {
        switch (key) {
            case "Auto":
                return AutoRules;
            case "Foul":
                return FoulRules;
            case "Truss":
                return TrussRules;
            case "Catch":
                return CatchRules;
            case "High":
                return HighRules;
            case "Low":
                return LowRules;

        }
        return null;
    }

    public int getMatchCount() {
        return matches.length;
    }

    public int getMaxHeight(String key) {
        return getSubgraph(key).getGraphMax();
    }

    public int[] getShades(String key) {
        Subgraph g = getSubgraph(key);
        SKS[] p = g.getGraphRules();
        int[] d = new int[p.length];
        for (int i = 0; i < d.length; i++) {
            d[i] = p[i].intensity;
        }
        return d;
    }

    public int[] getScales(String key) {
        Subgraph g = getSubgraph(key);
        SKS[] p = g.getGraphRules();
        int[] d = new int[p.length];
        for (int i = 0; i < d.length; i++) {
            d[i] = p[i].scale;
        }
        return d;
    }

    public int[][] getGraphData(String key) {
        Subgraph g = getSubgraph(key);
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

    private static int keyToInt(String key) {
        for (int i = 0; i < keys.length; i++) {
            if (keys[i].equals(key)) {
                return i;
            }
        }
        return -1;
    }

    private int getField(int match, String key) {
        return matches[match][keyToInt(key)];
    }

    private interface Subgraph {

        public int getSuccess(int m);

        public int getFailure(int m);

        public int getGraphMax();

        public SKS[] getGraphRules();

    };

    public static class SKS {

        public final int intensity;
        public final String key;
        public final int scale;

        public SKS(String s, int i) {
            this.key = s;
            this.intensity = i;
            this.scale = 1;
        }

        public SKS(String s, int i, int scale) {
            this.key = s;
            this.intensity = i;
            this.scale = scale;
        }
    }

    private final Subgraph AutoRules = new Subgraph() {
        @Override
        public int getSuccess(int m) {
            return getField(m, "AHighHot") * 20 + getField(m, "AHighCold") * 15
                    + getField(m, "ALowHot") * 11 + getField(m, "ALowCold") * 6
                    + getField(m, "AMobility") * 5;
        }

        @Override
        public int getFailure(int m) {
            return getField(m, "ALowMiss") + getField(m, "AHighMiss");
        }

        @Override
        public int getGraphMax() {
            return 65;
        }

        @Override
        public SKS[] getGraphRules() {
            return is;
        }

        private final SKS[] is = new SKS[]{
            new SKS("AMobility", 3, 5),
            new SKS("AHighHot", 4, 20),
            new SKS("AHighCold", 3, 15),
            new SKS("ALowHot", 4, 11),
            new SKS("ALowCold", 3, 6),
            new SKS("AHighMiss", 2, 5),
            new SKS("ALowMiss", 1, 5)
        };
    };

    private final Subgraph FoulRules = new Subgraph() {
        @Override
        public int getSuccess(int m) {
            return getField(m, "G12") + getField(m, "G28")
                    + getField(m, "G40") + getField(m, "OtherTech");
        }

        @Override
        public int getFailure(int m) {
            return 0;
        }

        @Override
        public int getGraphMax() {
            return 4;
        }

        @Override
        public SKS[] getGraphRules() {
            return is;
        }

        private final SKS[] is = new SKS[]{
            new SKS("G12", 4),
            new SKS("G28", 3),
            new SKS("G40", 2),
            new SKS("OtherTech", 1)
        };
    };

    private Subgraph ABGraph(final String good, final String bad, final int max) {
        return new Subgraph() {
            @Override
            public int getSuccess(int m) {
                return getField(m, good);
            }

            @Override
            public int getFailure(int m) {
                return getField(m, bad);
            }

            @Override
            public int getGraphMax() {
                return max;
            }

            @Override
            public SKS[] getGraphRules() {
                return is;
            }

            private final SKS[] is = new SKS[]{
                new SKS(good, 4),
                new SKS(bad, 1)
            };
        };
    }

    private final Subgraph TrussRules = ABGraph("Truss", "TrussMiss", 10);
    private final Subgraph CatchRules = ABGraph("Catch", "CatchMiss", 5);
    private final Subgraph HighRules = ABGraph("High", "HighMiss", 10);
    private final Subgraph LowRules = ABGraph("Low", "LowMiss", 10);
}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package scouting.summary;

public interface GraphRules {

    public static final GraphRules AutoRules = TeamRecord.makeRules("Auto",
            new String[]{"AutoHighHot", "AutoLowHot", "AutoHighCold", "AutoLowCold"},
            new String[]{"AutoMiss"}, 3, new SKS[]{
                new SKS("AutoHighHot", Chart.FULL),
                new SKS("AutoHighCold", Chart.MEDIUM),
                new SKS("AutoLowHot", Chart.MEDIUM),
                new SKS("AutoLowCold", Chart.MEDIUM),
                new SKS("AutoMiss", Chart.CLEAR)
            });
    public static final GraphRules MiscRules = TeamRecord.makeRules("Misc",
            new String[]{"MiscFoul"},
            new String[]{}, 4, new SKS[]{
                new SKS("MiscFoul", Chart.FULL),
                new SKS("MiscStuckBall", Chart.MEDIUM),
                new SKS("MiscDeadBot", Chart.CLEAR)
            });
    public static final GraphRules TrussRules = TeamRecord.makeRules("Truss",
            new String[]{"TrussHP", "TrussField"},
            new String[]{"TrussFail"}, 10,
            new SKS[]{
                new SKS("TrussHP", Chart.FULL),
                new SKS("TrussField", Chart.MEDIUM),
                new SKS("TrussFail", Chart.CLEAR)
            });
    public static final GraphRules PickupRules = TeamRecord.makeRules("Load",
            new String[]{"Pickups"},
            new String[]{}, 10,
            new SKS[]{
                new SKS("Pickups", Chart.FULL)
            });
    public static final GraphRules HighRules = TeamRecord.makeRules("High",
            new String[]{"High"},
            new String[]{"HighMiss"}, 10,
            new SKS[]{
                new SKS("High", Chart.FULL),
                new SKS("HighMiss", Chart.CLEAR)});
    public static final GraphRules LowRules = TeamRecord.makeRules("Low",
            new String[]{"LowGoal"},
            new String[]{}, 10,
            new SKS[]{
                new SKS("LowGoal", Chart.FULL)
            });

    public int getSuccess(TeamRecord r, int m);

    public int getFailure(TeamRecord r, int m);

    public int getGraphMax();

    public SKS[] getGraphRules();

    public String getName();
}

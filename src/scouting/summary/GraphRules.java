/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package scouting.summary;

public class GraphRules {

    public static final GraphRules NullRules = new GraphRules("???",
            new SII[0], new SII[0], 1, new SII[0]);

    public final String name;
    public final int max;

    public final SII[] pro;
    public final SII[] con;
    public final SII[] plot;

    public GraphRules(final String name, final SII[] pro,
            final SII[] con, final int max, final SII[] chart) {
        this.name = name;
        this.pro = pro;
        this.con = con;
        this.max = max;
        this.plot = chart;
    }
}

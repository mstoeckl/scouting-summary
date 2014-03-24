/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package scouting.summary;

public class SII {

    public final int color;
    public final String key;
    public final int scale;

    public SII(String s, int color, int scale) {
        this.key = s;
        this.color = color;
        this.scale = scale;
    }

    public SII(String s, int color) {
        this.key = s;
        this.color = color;
        this.scale = 1;
    }

}

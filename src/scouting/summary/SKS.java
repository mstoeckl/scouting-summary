/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package scouting.summary;

public class SKS {

    public final int intensity;
    public final String key;
    public final int scale;

    public SKS(String s, int i, int scale) {
        this.key = s;
        this.intensity = i;
        this.scale = scale;
    }

    public SKS(String s, int i) {
        this.key = s;
        this.intensity = i;
        this.scale = 1;
    }

}

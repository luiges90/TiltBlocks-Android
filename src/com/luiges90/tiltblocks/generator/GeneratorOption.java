package com.luiges90.tiltblocks.generator;

import java.io.Serializable;

public class GeneratorOption implements Serializable {

    private static final long serialVersionUID = 8526202311648592913L;

    public int stepLo = 4;
    public int stepHi = 6;
    public int colorLo = 1;
    public int colorHi = 3;

    public boolean stone = false;
    public boolean rainbow = false;
    public boolean arrow = false;
    public boolean sticky = false;
    public boolean gate = false;
    public boolean shift = false;
    public boolean nomatch = false;
    public boolean wrap = false;

    public boolean optimal = false;

    public GeneratorOption() {
    }

}

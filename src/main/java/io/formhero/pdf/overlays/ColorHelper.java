package io.formhero.pdf.overlays;

import java.awt.*;
import java.util.StringTokenizer;

/**
 * Created by ryan.kimber on 2018-04-07.
 */
public class ColorHelper {
    public static Color parseColor(String s, Color defClr) {

        if(s.indexOf("#") == -1 && s.indexOf("(") == -1) return getColorByName(s, defClr);

        if (s == null || s.length() < 3) return null;
        if (s.charAt(0) == '#') {
            return parseHashHexa(s, defClr);
        }
        StringTokenizer st = new StringTokenizer(s, " ,()");
        String typ = "rgb";

        if (st.hasMoreTokens()) typ = st.nextToken();

        int r = 0, g = 0, b = 0, a = 255;
        Color c = defClr;
        try {
            if (st.hasMoreTokens()) {
                s = st.nextToken();
                if (s.endsWith("%")) {///**/""
                    s = s.substring(0, s.length() - 1);
                    r = (int) (Integer.parseInt(s) * 255 / 100);
                } else {
                    r = Integer.parseInt(s);
                }

                if (st.hasMoreTokens()) {
                    s = st.nextToken();
                    if (s.endsWith("%")) {
                        s = s.substring(0, s.length() - 1);
                        g = (int) (Integer.parseInt(s) * 255 / 100);
                    } else {
                        g = Integer.parseInt(s);
                    }
                    if (st.hasMoreTokens()) {
                        s = st.nextToken();
                        if (s.endsWith("%")) {
                            s = s.substring(0, s.length() - 1);
                            b = (int) (Integer.parseInt(s) * 255 / 100);
                        } else {
                            b = Integer.parseInt(s);
                        }
                        if (st.hasMoreTokens()) {
                            s = st.nextToken();
                            if (s.endsWith("%")) {
                                s = s.substring(0, s.length() - 1);
                                a = (int) (Integer.parseInt(s) * 255 / 100);
                            } else {
                                a = Integer.parseInt(s);
                            }
                        }
                    }
                }
            }

        } catch (Throwable e) {
            return defClr;
        }
        try {
            c = new Color(r, g, b, a);
        } catch (Throwable e) {
            return defClr;
        }
        return c;
    }

    public static Color getColorByName(String s, Color defaultColor)
    {
        if(s == null) return defaultColor;
        s = s.toUpperCase();
        switch(s)
        {
            case "BLACK":
                return Color.BLACK;
            case "BLUE":
                return Color.BLUE;
            case "CYAN":
                return Color.CYAN;
            case "DARK_GRAY":
                return Color.DARK_GRAY;
            case "GRAY":
                return Color.GRAY;
            case "GREEN":
                return Color.GREEN;
            case "LIGHT_GRAY":
                return Color.LIGHT_GRAY;
            case "MAGENTA":
                return Color.MAGENTA;
            case "ORANGE":
                return Color.ORANGE;
            case "PINK":
                return Color.PINK;
            case "RED":
                return Color.RED;
            case "WHITE":
                return Color.WHITE;
            case "YELLOW":
                return Color.YELLOW;
        }

        return defaultColor;
    }

    public static Color parseHashHexa(String s, Color defClr) {
        int r = 0, g = 0, b = 0, a = 255;
        Color c = defClr;
        try {

            r = Integer.parseInt(s.substring(1, 3), 16);
            g = Integer.parseInt(s.substring(3, 5), 16);
            b = Integer.parseInt(s.substring(5, 7), 16);
            if (s.length() > 7) a = Integer.parseInt(s.substring(7, 9), 16);

        } catch (Throwable e) {
            return defClr;
        } finally {
            try {
                c = new Color(r, g, b, a);
            } catch (Throwable e) {
                return defClr;
            }
        }


        return c;
    }
}

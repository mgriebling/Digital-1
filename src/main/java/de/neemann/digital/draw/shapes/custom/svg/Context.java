/*
 * Copyright (c) 2018 Helmut Neemann.
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.draw.shapes.custom.svg;

import de.neemann.digital.draw.graphics.Transform;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import java.awt.*;
import java.util.HashMap;
import java.util.StringTokenizer;

class Context {

    private static final HashMap<String, AttrParser> PARSER = new HashMap<>();
    static {
        PARSER.put("transform", Context::readTransform);
        PARSER.put("fill", (c, value) -> c.fill = getColorFromString(value));
        PARSER.put("stroke", (c, value) -> c.color = getColorFromString(value));
        PARSER.put("stroke-width", (c, value) -> c.thickness = getFloatFromString(value) + 1);
        PARSER.put("font-size", (c, value) -> c.fontSize = getFloatFromString(value) + 1);
        PARSER.put("style", Context::readStyle);
    }

    private Transform tr;
    private Color fill;
    private Color color;
    private float thickness;
    private float fontSize;

    Context() {
        tr = Transform.IDENTITY;
        thickness = 1;
    }

    private Context(Context parent) {
        tr = parent.tr;
        fill = parent.fill;
        color = parent.color;
        thickness = parent.thickness;
        fontSize = parent.fontSize;
    }

    Context(Context parent, Element element) {
        this(parent);
        final NamedNodeMap attributes = element.getAttributes();
        for (int i = 0; i < attributes.getLength(); i++) {
            final Node item = attributes.item(i);
            AttrParser p = PARSER.get(item.getNodeName());
            if (p != null)
                p.parse(this, item.getNodeValue().trim());
        }
    }

    private static void readStyle(Context context, String style) {
        StringTokenizer st = new StringTokenizer(style, ";");
        while (st.hasMoreTokens()) {
            String[] t = st.nextToken().split(":");
            if (t.length == 2) {
                AttrParser p = PARSER.get(t[0].trim());
                if (p != null)
                    p.parse(context, t[1].trim());
            }
        }
    }

    Transform getTransform() {
        return tr;
    }

    public Color getColor() {
        return color;
    }

    public Color getFilled() {
        return fill;
    }

    public int getThickness() {
        return (int) thickness;
    }


    private interface AttrParser {
        void parse(Context c, String value);
    }


    private static void readTransform(Context c, String value) {
        // ToDo transform is ignored!
    }

    private static Color getColorFromString(String v) {
        if (v.equalsIgnoreCase("none"))
            return null;

        if (v.startsWith("#"))
            return Color.decode(v);

        try {
            return (Color) Color.class.getField(v).get(null);
        } catch (IllegalArgumentException | IllegalAccessException | NoSuchFieldException | SecurityException e) {
            return Color.BLACK;
        }
    }

    private static float getFloatFromString(String inp) {
        inp = inp.replaceAll("[^0-9.]", "");
        if (inp.isEmpty())
            return 1;
        return Float.parseFloat(inp);
    }

}

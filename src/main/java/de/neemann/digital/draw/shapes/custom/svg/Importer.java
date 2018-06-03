/*
 * Copyright (c) 2018 Helmut Neemann.
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.draw.shapes.custom.svg;

import de.neemann.digital.draw.graphics.*;
import de.neemann.digital.draw.shapes.custom.CustomShapeDescription;
import de.neemann.digital.lang.Lang;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * Helper to import an SVG file
 */
public class Importer {
    private final Document svg;
    private CustomShapeDescription shape;

    /**
     * Create a new importer instance
     *
     * @param svgFile the svg file to import
     * @throws IOException IOException
     */
    public Importer(File svgFile) throws IOException {
        if (!svgFile.exists())
            throw new FileNotFoundException(svgFile.getPath());
        try {
            svg = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(svgFile);
        } catch (Exception e) {
            e.printStackTrace();
            throw new IOException(Lang.get("err_parsingSVG"));
        }
    }

    /**
     * Parses and draws the svg file.
     *
     * @param gr the svg is drawn to this instance
     */
    public void draw(Graphic gr) {
        NodeList gList = svg.getElementsByTagName("svg").item(0).getChildNodes();
        Context c = new Context();
        draw(gr, gList, c);
    }

    private void draw(Graphic gr, NodeList gList, Context c) {
        for (int i = 0; i < gList.getLength(); i++) {
            final Node node = gList.item(i);
            if (node instanceof Element)
                draw(gr, (Element) node, c);
        }
    }

    private void draw(Graphic gr, Element element, Context parent) {
        Context c = new Context(parent, element);
        switch (element.getNodeName()) {
            case "a":
            case "g":
                draw(gr, element.getChildNodes(), c);
                break;
            case "line":
                gr.drawLine(
                        vec(element.getAttribute("x1"), element.getAttribute("y1"), c),
                        vec(element.getAttribute("x2"), element.getAttribute("y2"), c),
                        c.getStyle());
                break;
            case "rect":
                drawRect(gr, element, c);
                break;

        }
    }

    private VectorInterface vec(String xStr, String yStr, Context c) {
        if (xStr.isEmpty() && yStr.isEmpty())
            return new Vector(0, 0);
        float x = Float.parseFloat(xStr);
        float y = Float.parseFloat(yStr);
        return c.getTransform().transform(new VectorFloat(x, y));
    }

    private void drawRect(Graphic gr, Element element, Context c) {
        VectorInterface size = vec(element.getAttribute("width"), element.getAttribute("height"), c);
        VectorInterface pos = vec(element.getAttribute("x"), element.getAttribute("y"), c);
        VectorInterface rad = vec(element.getAttribute("rx"), element.getAttribute("ry"), c);

        float x = pos.getXFloat();
        float y = pos.getYFloat();
        float width = size.getXFloat();
        float height = size.getYFloat();
        if (rad.getXFloat() * rad.getYFloat() != 0) {
            float w = size.getXFloat() - 2 * rad.getXFloat();
            float h = size.getYFloat() - 2 * rad.getYFloat();
            float rx = rad.getXFloat();
            float ry = rad.getYFloat();
            gr.drawPolygon(new Polygon(true)
                    .add(new VectorFloat(x + rx, y))
                    .add(new VectorFloat(x + rx + w, y))
                    .add(new VectorFloat(x + width, y + ry))
                    .add(new VectorFloat(x + width, y + ry + h))
                    .add(new VectorFloat(x + rx + w, y + height))
                    .add(new VectorFloat(x + rx, y + height))
                    .add(new VectorFloat(x, y + ry + h))
                    .add(new VectorFloat(x, y + ry)), c.getStyle());
        } else
            gr.drawPolygon(new Polygon(true)
                    .add(new VectorFloat(x, y))
                    .add(new VectorFloat(x + width, y))
                    .add(new VectorFloat(x + width, y + height))
                    .add(new VectorFloat(x, y + height)), c.getStyle());
    }
}

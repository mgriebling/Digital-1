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
public class SvgImporter {
    private final Document svg;

    /**
     * Create a new importer instance
     *
     * @param svgFile the svg file to import
     * @throws IOException IOException
     */
    public SvgImporter(File svgFile) throws IOException {
        if (!svgFile.exists())
            throw new FileNotFoundException(svgFile.getPath());
        try {
            svg = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(svgFile);
        } catch (Exception e) {
            throw new IOException(Lang.get("err_parsingSVG"), e);
        }
    }

    /**
     * Parses and draws the svg file.
     *
     * @throws SvgException SvgException
     */
    public CustomShapeDescription create() throws SvgException {
        NodeList gList = svg.getElementsByTagName("svg").item(0).getChildNodes();
        Context c = new Context();
        try {
            CustomShapeDescription csd = new CustomShapeDescription();
            create(csd, gList, c);
            return csd;
        } catch (RuntimeException e) {
            throw new SvgException(Lang.get("err_parsingSVG"), e);
        }
    }

    private void create(CustomShapeDescription csd, NodeList gList, Context c) {
        for (int i = 0; i < gList.getLength(); i++) {
            final Node node = gList.item(i);
            if (node instanceof Element)
                create(csd, (Element) node, c);
        }
    }

    private void create(CustomShapeDescription csd, Element element, Context parent) {
        Context c = new Context(parent, element);
        switch (element.getNodeName()) {
            case "a":
            case "g":
                create(csd, element.getChildNodes(), c);
                break;
            case "line":
                csd.addLine(
                        vec(element.getAttribute("x1"), element.getAttribute("y1"), c),
                        vec(element.getAttribute("x2"), element.getAttribute("y2"), c),
                        c.getThickness(), c.getColor());
                break;
            case "rect":
                drawRect(csd, element, c);
                break;
            case "path":
                final Polygon polygon = Polygon.createFromPath(element.getAttribute("d"));
                if (c.getFilled() != null)
                    csd.addPolygon(polygon, c.getThickness(), c.getFilled(), true);
                if (c.getColor() != null)
                    csd.addPolygon(polygon, c.getThickness(), c.getColor(), false);
                break;
            case "circle":
            case "ellipse":
                drawCircle(csd, element, c);
                break;
        }
    }

    private Vector vec(String xStr, String yStr, Context c) {
        float x = xStr.isEmpty() ? 0 : Float.parseFloat(xStr);
        float y = yStr.isEmpty() ? 0 : Float.parseFloat(yStr);
        final VectorFloat v = c.getTransform().transform(new VectorFloat(x, y));
        return new Vector(v.getX(), v.getY());
    }

    private void drawRect(CustomShapeDescription csd, Element element, Context c) {
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
            csd.addPolygon(new Polygon(true)
                    .add(new VectorFloat(x + rx, y))
                    .add(new VectorFloat(x + rx + w, y))
                    .add(new VectorFloat(x + width, y + ry))
                    .add(new VectorFloat(x + width, y + ry + h))
                    .add(new VectorFloat(x + rx + w, y + height))
                    .add(new VectorFloat(x + rx, y + height))
                    .add(new VectorFloat(x, y + ry + h))
                    .add(new VectorFloat(x, y + ry)), c.getThickness(), c.getColor(), false);
        } else
            csd.addPolygon(new Polygon(true)
                    .add(new VectorFloat(x, y))
                    .add(new VectorFloat(x + width, y))
                    .add(new VectorFloat(x + width, y + height))
                    .add(new VectorFloat(x, y + height)), c.getThickness(), c.getColor(), false);
    }

    private void drawCircle(CustomShapeDescription csd, Element element, Context c) {
        Vector r = null;
        if (element.hasAttribute("r")) {
            final String rad = element.getAttribute("r");
            r = vec(rad, rad, c);
        }
        if (element.hasAttribute("rx")) {
            r = vec(element.getAttribute("rx"), element.getAttribute("ry"), c);
        }
        if (r != null) {
            Vector pos = vec(element.getAttribute("cx"), element.getAttribute("cy"), c);
            Vector oben = pos.sub(r);
            Vector unten = pos.add(r);

            if (c.getFilled() != null)
                csd.addCircle(oben, unten, c.getThickness(), c.getFilled(), true);
            if (c.getColor() != null)
                csd.addCircle(oben, unten, c.getThickness(), c.getColor(), false);
        }
    }

}

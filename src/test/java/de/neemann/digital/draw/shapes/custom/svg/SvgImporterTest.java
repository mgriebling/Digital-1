package de.neemann.digital.draw.shapes.custom.svg;

import de.neemann.digital.draw.graphics.Graphic;
import de.neemann.digital.draw.graphics.GraphicMinMax;
import de.neemann.digital.draw.graphics.GraphicsImage;
import de.neemann.digital.draw.shapes.custom.CustomShapeDescription;
import de.neemann.digital.integration.FileScanner;
import de.neemann.digital.integration.Resources;
import junit.framework.TestCase;

import java.io.File;
import java.io.FileOutputStream;

public class SvgImporterTest extends TestCase {

    public void testIcons() throws Exception {
        File f = new File(Resources.getRoot(), "../../main/svg");
        new FileScanner(f1 -> {
            CustomShapeDescription csd = new SvgImporter(f1).create();

            assertTrue("empty file",csd.size() > 0);

            File tempFile = File.createTempFile(f1.getName(), ".png");
            try (Graphic gr = new GraphicsImage(new FileOutputStream(tempFile), "png", 2)) {
                GraphicMinMax mm = new GraphicMinMax();
                csd.drawTo(mm, null);
                gr.setBoundingBox(mm.getMin(), mm.getMax());
                csd.drawTo(gr, null);
            }
        }).setSuffix("svg").noOutput().scan(f);
    }

}
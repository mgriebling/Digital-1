package de.neemann.digital.draw.graphics;

import junit.framework.TestCase;

public class PolygonParserTest extends TestCase {

    public void testSimple() {
        PolygonParser pp = new PolygonParser("m 1,2");

        assertEquals(PolygonParser.Token.COMMAND, pp.next());
        assertEquals('m', pp.getCommand());

        assertEquals(PolygonParser.Token.NUMBER, pp.next());
        assertEquals(1.0, pp.getValue(), 1e-6);

        assertEquals(PolygonParser.Token.NUMBER, pp.next());
        assertEquals(2.0, pp.getValue(), 1e-6);

        assertEquals(PolygonParser.Token.EOF, pp.next());
        assertEquals(PolygonParser.Token.EOF, pp.next());
    }

}
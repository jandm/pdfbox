package org.apache.pdfbox.examples.pdmodel;

import java.awt.Color;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class DrawMap
{

    public static void main(String[] args) throws ParserConfigurationException, SAXException,
            IOException, XPathExpressionException
    {
        if (args.length != 1)
        {
            System.err.println("usage: " + DrawMap.class.getName() + " <output-file>");
            System.exit(1);
        }

        String filename = args[0];

        PDDocument doc = new PDDocument();
        try
        {
            PDPage page = new PDPage();
            doc.addPage(page);
            PDPageContentStream contents = new PDPageContentStream(doc, page);

            // parse the countries svg
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setValidating(false);
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(DrawMap.class
                    .getResourceAsStream("/org/apache/pdfbox/examples/svg/countries.svg"));
            
            contents.setLineCapStyle(1);
            contents.setLineJoinStyle(1);
            // use xpath expression to get d-attribute of all path elements
            XPathFactory xPathFactory = XPathFactory.newInstance();
            XPath xpath = xPathFactory.newXPath();
            XPathExpression expression = xpath.compile("//path/@d");
            NodeList svgPaths = (NodeList) expression.evaluate(document, XPathConstants.NODESET);
            for (int p = 0; p < svgPaths.getLength(); p++)
            {
                String d = svgPaths.item(p).getNodeValue();
                String[] elements = d.split("\\s");
                float x = 0, y = 0;
                String command = null;
                for (String element : elements)
                {
                    if (element.startsWith("M"))
                    {
                        command = "M";
                        x = 20 + Float.parseFloat(element.substring(1));
                    }
                    else if (element.startsWith("L"))
                    {
                        command = "L";
                        x = 20 + Float.parseFloat(element.substring(1));
                    }
                    else if (element.startsWith("Z"))
                    {
                        contents.closeAndStroke();
                    }
                    else
                    {
                        // SVG y-axis points downwards, PDF upwards !
                        y = 300 + (277 - Float.parseFloat(element));
                        if ("M".equals(command))
                        {
                            contents.moveTo(x, y);
                        }
                        else if ("L".equals(command))
                        {
                            contents.lineTo(x, y);
                        }
                    }
                }
            }
            contents.close();
            doc.save(filename);
        }
        finally
        {
            doc.close();
        }
    }
}

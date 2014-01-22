/**
 * Copyright 2010 Västra Götalandsregionen
 *
 *   This library is free software; you can redistribute it and/or modify
 *   it under the terms of version 2.1 of the GNU Lesser General Public
 *   License as published by the Free Software Foundation.
 *
 *   This library is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU Lesser General Public License for more details.
 *
 *   You should have received a copy of the GNU Lesser General Public
 *   License along with this library; if not, write to the
 *   Free Software Foundation, Inc., 59 Temple Place, Suite 330,
 *   Boston, MA 02111-1307  USA
 *
 */

package se.vgregion.kivtools.hriv.presentation;

import java.io.IOException;
import java.io.StringReader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import se.vgregion.kivtools.hriv.presentation.types.SigningInformation;

/**
 * Helper-class for parsing a SAML-response.
 * 
 * @author Jonas Liljenfeldt & Joakim Olsson
 */
public class SamlResponseHelper {

  /**
   * Gets signing information from a SAML-response.
   * 
   * @param samlAssertionString The SAML-response as a String.
   * @return A populated SigningInformation object.
   */
  public static SigningInformation getSigningInformation(String samlAssertionString) {

    Document document = getDocument(samlAssertionString);
    String nationalId = getNationalId(document);
    return new SigningInformation(nationalId, samlAssertionString);
  }

  private static String getNationalId(Document document) {
    String nationalId = null;
    NodeList attributes = document.getElementsByTagName("Attribute");
    for (int i = 0; i < attributes.getLength() && nationalId == null; i++) {
      Node node = attributes.item(i);
      NamedNodeMap nodeAttributes = node.getAttributes();
      Node nodeAttributeNode = nodeAttributes.getNamedItem("AttributeName");
      if (nodeAttributeNode != null && "se.persnr".equals(nodeAttributeNode.getNodeValue())) {
        NodeList childNodes = node.getChildNodes();
        for (int j = 0; j < childNodes.getLength(); j++) {
          if ("AttributeValue".equals(childNodes.item(j).getNodeName())) {
            nationalId = childNodes.item(j).getTextContent();
            break;
          }
        }
      }
    }
    return nationalId;
  }

  private static Document getDocument(String samlAssertionString) {
    Document document;
    try {
      DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
      DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
      StringReader stringReader = new StringReader(samlAssertionString);
      InputSource inputSource = new InputSource(stringReader);
      document = documentBuilder.parse(inputSource);
    } catch (ParserConfigurationException e) {
      throw new RuntimeException(e);
    } catch (SAXException e) {
      throw new RuntimeException(e);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    return document;
  }
}

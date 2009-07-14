/**
 * Copyright 2009 Västa Götalandsregionen
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
 */
package se.vgregion.kivtools.search.svc.domain.values;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

public class AddressHelperTest {

  private static final String ZIPCODE_CITY = "412 63 Göteborg";
  private static final String ZIPCODE_FORMATTED = "412 63";
  private static final String ZIPCODE = "41263";
  private static final String STREET = "Storgatan 1";

  @Test
  public void testConvertToStreetAddress() {
    Address address = AddressHelper.convertToStreetAddress(null);
    assertNotNull("An empty address should have been created", address);
    assertEquals("Street should be empty", "", address.getStreet());

    List<String> origAddress = new ArrayList<String>();
    address = AddressHelper.convertToStreetAddress(origAddress);
    assertNotNull("An empty address should have been created", address);
    assertEquals("Street should be empty", "", address.getStreet());

    origAddress.add(STREET);
    address = AddressHelper.convertToStreetAddress(origAddress);
    assertNotNull("An address should have been created", address);
    assertEquals("Unexpected value for street", "", address.getStreet());
    assertEquals("Unexpected value for additional information", STREET, address.getAdditionalInfoToString());

    origAddress.add(ZIPCODE_CITY);
    address = AddressHelper.convertToStreetAddress(origAddress);
    assertNotNull("An address should have been created", address);
    assertEquals("Unexpected value for street", STREET, address.getStreet());
    assertEquals("Unexpected value for zipcode", ZIPCODE, address.getZipCode().getZipCode());
    assertEquals("Unexpected value for zipcode formatted", ZIPCODE_FORMATTED, address.getZipCode().getFormattedZipCode().getZipCode());

    origAddress.add(STREET);
    address = AddressHelper.convertToStreetAddress(origAddress);
    assertNotNull("An address should have been created", address);
    assertEquals("Unexpected value for additional information", "Storgatan 1412 63 GöteborgStorgatan 1", address.getAdditionalInfoToString());

    origAddress.remove(STREET);
    origAddress.remove(STREET);
    origAddress.add("Sverige");
    address = AddressHelper.convertToStreetAddress(origAddress);
    assertNotNull("An address should have been created", address);
    assertEquals("Unexpected value for additional information", "412 63 GöteborgSverige", address.getAdditionalInfoToString());

    origAddress.clear();
    origAddress.add("Storgatan");
    origAddress.add("S-41263");
    address = AddressHelper.convertToStreetAddress(origAddress);
    assertNotNull("An address should have been created", address);
    assertEquals("Unexpected value for additional information", "StorgatanS-41263", address.getAdditionalInfoToString());

    origAddress.remove("S-41263");
    origAddress.add("S1234");
    address = AddressHelper.convertToStreetAddress(origAddress);
    assertNotNull("An address should have been created", address);
    assertEquals("Unexpected value for additional information", "StorgatanS1234", address.getAdditionalInfoToString());

    origAddress.remove("S1234");
    origAddress.add("41263");
    address = AddressHelper.convertToStreetAddress(origAddress);
    assertNotNull("An address should have been created", address);
    assertEquals("Unexpected value for additional information", "Storgatan41263", address.getAdditionalInfoToString());

    origAddress.remove("41263");
    origAddress.add("41263  28 Göteborg");
    address = AddressHelper.convertToStreetAddress(origAddress);
    assertNotNull("An address should have been created", address);
    assertEquals("Unexpected value for zipcode", "41263", address.getZipCode().getZipCode());
    assertEquals("Unexpected value for city", "Göteborg", address.getCity());

    origAddress.remove("41263  28 Göteborg");
    origAddress.add("Göteborg");
    origAddress.add("Sverige");
    address = AddressHelper.convertToStreetAddress(origAddress);
    assertNotNull("An address should have been created", address);
    assertEquals("Unexpected value for additional information", "StorgatanGöteborgSverige", address.getAdditionalInfoToString());

    origAddress.add("");
    address = AddressHelper.convertToStreetAddress(origAddress);
    assertNotNull("An address should have been created", address);
    assertEquals("Unexpected value for additional information", "StorgatanGöteborgSverige", address.getAdditionalInfoToString());
  }
}

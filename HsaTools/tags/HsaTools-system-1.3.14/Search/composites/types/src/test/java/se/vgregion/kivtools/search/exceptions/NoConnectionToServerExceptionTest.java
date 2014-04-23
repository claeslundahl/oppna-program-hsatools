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

package se.vgregion.kivtools.search.exceptions;

import static org.junit.Assert.*;

import org.junit.Test;

public class NoConnectionToServerExceptionTest {

  private static final String ERROR_MESSAGE_1 = "This is an error message";
  private static final String ERROR_MESSAGE_2 = "Ingen anslutning till servern. Var god f\u00F6rs\u00F6k senare.";

  @Test
  public void testErrorMessage(){
    assertEquals(ERROR_MESSAGE_1, new NoConnectionToServerException(ERROR_MESSAGE_1).getMessage());
    assertEquals(ERROR_MESSAGE_2, new NoConnectionToServerException().toString());
  }
}

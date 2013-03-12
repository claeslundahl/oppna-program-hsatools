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

package se.vgregion.kivtools.hriv.presentation.exceptions;


/**
 * Indicates registration exceptions from Vårdval system.
 * 
 * @author Jonas Liljenfeldt & Joakim Olsson
 */
public class VardvalRegistrationException extends VardvalException {

  private static final long serialVersionUID = 1L;

  /**
   * Constructs a new VardvalRegistrationException.
   * 
   * @param message The message to use.
   */
  public VardvalRegistrationException(String message) {
    super(message);
  }


    public VardvalRegistrationException(Throwable cause) {
        super(cause);
    }

    public VardvalRegistrationException(String message, Throwable cause) {
        super(message, cause);
    }
}
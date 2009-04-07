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
/**
 * 
 */
package se.vgregion.kivtools.search.presentation.forms;

import java.io.Serializable;

import javax.faces.model.SelectItem;

/**
 * @author Jonas Liljenfeldt, KnowIT
 * 
 */
@SuppressWarnings("serial")
public class AccessibilityDatabaseFilterForm implements Serializable {
	private Boolean hear = false;
	private Boolean see = false;
	private Boolean move = false;
	private Boolean substances = false;
	private Boolean info = false;
	private Boolean submitted = false;
	private String listType = "attentive";
	private SelectItem[] listTypes = new SelectItem[] {
			new SelectItem("attentive", "Vad bör uppmärksammas"), new SelectItem("available", "Vad är tillgängligt") };

	public SelectItem[] getListTypes() {
		return listTypes;
	}

	public void setListTypes(SelectItem[] listTypes) {
		this.listTypes = listTypes;
	}

	public Boolean getHear() {
		return hear;
	}

	public void setHear(Boolean hear) {
		this.hear = hear;
	}

	public Boolean getSee() {
		return see;
	}

	public void setSee(Boolean see) {
		this.see = see;
	}

	public Boolean getMove() {
		return move;
	}

	public void setMove(Boolean move) {
		this.move = move;
	}

	public Boolean getSubstances() {
		return substances;
	}

	public void setSubstances(Boolean substances) {
		this.substances = substances;
	}

	public Boolean getInfo() {
		return info;
	}

	public void setInfo(Boolean info) {
		this.info = info;
	}

	public Boolean getSubmitted() {
		return submitted;
	}

	public void setSubmitted(Boolean submitted) {
		this.submitted = submitted;
	}
	public String getListType() {
		return listType;
	}
	
	public void setListType(String listType) {
		this.listType = listType;
	}
}

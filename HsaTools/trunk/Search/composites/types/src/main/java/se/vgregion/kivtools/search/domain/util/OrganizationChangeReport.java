package se.vgregion.kivtools.search.domain.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import se.vgregion.kivtools.search.interfaces.UnitComposition;

/**
 * 
 * @author David Bennehult
 * @author Ulf Carlsson
 * 
 * @param <T>
 *            Unit type
 */
public class OrganizationChangeReport <T>  {

    List<UnitComposition<T>> addedUnits;
    List<UnitComposition<T>> removedUnits;
    Map<String,List<UnitComposition<T>>> movedUnits;
    List<UnitComposition<T>> changedUnits;

    public OrganizationChangeReport(List<UnitComposition<T>> addedUnits, List<UnitComposition<T>> removedUnits,
            Map<String,List<UnitComposition<T>>> movedUnits, List<UnitComposition<T>> changedUnits) {
        super();
        this.addedUnits = new ArrayList<UnitComposition<T>>(addedUnits);
        this.removedUnits = new ArrayList<UnitComposition<T>>(removedUnits);
        this.movedUnits = new HashMap<String, List<UnitComposition<T>>>(movedUnits);
        this.changedUnits = new ArrayList<UnitComposition<T>>(changedUnits);
    }

    /**
     * 
     * @return {@link UnitComposition} that has been added to the organization.
     */
    public List<UnitComposition<T>> getAddedOrganizationUnits() {
        return Collections.unmodifiableList(addedUnits);

    }

    /**
     * 
     * @return {@link UnitComposition} that has been removed from the organization.
     */
    public List<UnitComposition<T>> getRemovedOrganizationUnits() {
        return Collections.unmodifiableList(removedUnits);
    }

    /**
     * 
     * @return {@link Map} with {@link UnitComposition} that has been moved in the organization.
     * Map key is the parent units hsaId, Map value is a List of the unit children ({@link UnitComposition}) to the parent. 
     */
    public   Map<String,List<UnitComposition<T>>> getMovedOrganizationUnits() {
        return Collections.unmodifiableMap(movedUnits);
    }

    /**
     * 
     * @return {@link UnitComposition} that unit information has been changed.
     */
    public List<UnitComposition<T>> getChangedOrganizationUnits() {
        return Collections.unmodifiableList(changedUnits);
    }

    /**
     * 
     * @return true if any unit in the organization has been changed in any way.
     */
    public boolean isOrganizationChanged() {
        return addedUnits.size() > 0 || removedUnits.size() > 0 || movedUnits.size() > 0 || getChangedOrganizationUnits().size() > 0;
    }

}
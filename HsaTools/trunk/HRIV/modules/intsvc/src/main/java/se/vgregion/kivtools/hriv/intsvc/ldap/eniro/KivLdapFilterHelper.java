package se.vgregion.kivtools.hriv.intsvc.ldap.eniro;

import java.util.List;
import java.util.Set;
import java.util.Map.Entry;

import org.springframework.ldap.filter.AndFilter;
import org.springframework.ldap.filter.EqualsFilter;
import org.springframework.ldap.filter.Filter;
import org.springframework.ldap.filter.OrFilter;

import se.vgregion.kivtools.search.domain.values.HealthcareType;

/**
 * 
 * @author David Bennehult & Joakim Olsson.
 * 
 */
public class KivLdapFilterHelper {

  /**
   * Create ldap filter from current healthcare type properties.
   * @param healthcareTypes list of healthcaretypes to generate filter from.
   * @return spring ldap filter.
   */
  public static Filter createHealthcareTypeFilter(List<HealthcareType> healthcareTypes) {
    OrFilter orFilter = new OrFilter();
    for (HealthcareType healthcareType : healthcareTypes) {
      orFilter.or(createInnerHealthCareTypeFilter(healthcareType));
    }
    return orFilter;
  }

  /**
   * Creates spring filter of current Healthcaretype object.
   * 
   * @param healthcareType object to use to generate filter.
   * @return spring filter.
   */
  public static Filter createInnerHealthCareTypeFilter(HealthcareType healthcareType) {
    AndFilter andFilter = new AndFilter();
    Set<Entry<String, String>> entrySet = healthcareType.getConditions().entrySet();
    for (Entry<String, String> entry : entrySet) {
      String keyField = entry.getKey();
      String[] values = entry.getValue().split(",");
      andFilter.and(createOrFilter(keyField, values));
    }
    return andFilter;
  }

  /**
   * Creates an OrFilter from with of one or many values.
   * 
   * @param keyField chosen field for the or filter to apply on.
   * @param values one or many values to contain in the or filter.
   * @return spring Orfilter.
   */
  public static Filter createOrFilter(String keyField, String[] values) {
    OrFilter orFilter = new OrFilter();
    for (String value : values) {
      orFilter.or(new EqualsFilter(keyField, value));
    }
    return orFilter;
  }
}

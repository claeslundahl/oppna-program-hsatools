/**
 * Copyright 2009 Västra Götalandsregionen
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
package se.vgregion.kivtools.search.svc.impl.kiv.ldap;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.ldap.filter.AndFilter;
import org.springframework.ldap.filter.EqualsFilter;
import org.springframework.ldap.filter.Filter;
import org.springframework.ldap.filter.LikeFilter;
import org.springframework.ldap.filter.OrFilter;

import se.vgregion.kivtools.search.domain.Unit;
import se.vgregion.kivtools.search.domain.values.CodeTableName;
import se.vgregion.kivtools.search.domain.values.DN;
import se.vgregion.kivtools.search.domain.values.HealthcareType;
import se.vgregion.kivtools.search.domain.values.HealthcareTypeConditionHelper;
import se.vgregion.kivtools.search.exceptions.KivException;
import se.vgregion.kivtools.search.exceptions.NoConnectionToServerException;
import se.vgregion.kivtools.search.exceptions.SikInternalException;
import se.vgregion.kivtools.search.svc.SikSearchResultList;
import se.vgregion.kivtools.search.svc.codetables.CodeTablesService;
import se.vgregion.kivtools.search.svc.comparators.UnitNameComparator;
import se.vgregion.kivtools.search.svc.ldap.LdapConnectionPool;
import se.vgregion.kivtools.search.svc.ldap.criterions.SearchUnitCriterions;
import se.vgregion.kivtools.search.util.Formatter;
import se.vgregion.kivtools.search.util.LdapParse;
import se.vgregion.kivtools.util.StringUtil;

import com.novell.ldap.LDAPAttribute;
import com.novell.ldap.LDAPConnection;
import com.novell.ldap.LDAPEntry;
import com.novell.ldap.LDAPException;
import com.novell.ldap.LDAPSearchConstraints;
import com.novell.ldap.LDAPSearchResults;

/**
 * @author Anders and Hans, Know IT
 * @author Jonas Liljenfeldt, Know IT
 */
public class UnitRepository {
  public static final String KIV_SEARCH_BASE = "ou=Org,o=vgr";
  private static final int POOL_WAIT_TIME_MILLISECONDS = 2000;
  private static final String LDAP_WILD_CARD = "*";
  // an "
  private static final String LDAP_EXACT_CARD = "\"";
  private Log logger = LogFactory.getLog(this.getClass());
  private LdapConnectionPool theConnectionPool;
  private CodeTablesService codeTablesService;
  private UnitFactory unitFactory;

  public void setUnitFactory(UnitFactory unitFactory) {
    this.unitFactory = unitFactory;
  }

  public CodeTablesService getCodeTablesService() {
    return codeTablesService;
  }

  public void setCodeTablesService(CodeTablesService codeTablesService) {
    this.codeTablesService = codeTablesService;
  }

  public void setLdapConnectionPool(LdapConnectionPool lp) {
    this.theConnectionPool = lp;
  }

  /**
   * Search for unit with max result set to 0.
   * 
   * @param searchUnitCriterions The unit to search for.
   * @return List of found units.
   * @throws KivException .
   */
  public SikSearchResultList<Unit> searchUnits(SearchUnitCriterions searchUnitCriterions) throws KivException {
    // Zero means all units
    return searchUnits(searchUnitCriterions, 0);
  }

  /**
   * Search for a unit.
   * 
   * @param unit The unit to search for.
   * @param sortOrder The order the search result data.
   * @return A list of found units.
   * @throws KivException .
   */
  public SikSearchResultList<Unit> searchAdvancedUnits(Unit unit, Comparator<Unit> sortOrder) throws KivException {
    // Zero means all units
    return searchAdvancedUnits(unit, 0, sortOrder, new ArrayList<Integer>());
  }

  /**
   * Advanced means that it also handles healthcareTypeConditions in the search filter.
   * 
   * @param unit - unit to search for.
   * @param maxResult - max result of found units to return.
   * @param sortOrder - sort order for the result list.
   * @param showUnitsWithTheseHsaBussinessClassificationCodes - show units for chosen HsaBussinessClassificationCodes.
   * 
   * @return - a list of found units.
   * @throws KivException If an error occur.
   */
  public SikSearchResultList<Unit> searchAdvancedUnits(Unit unit, int maxResult, Comparator<Unit> sortOrder,
      List<Integer> showUnitsWithTheseHsaBussinessClassificationCodes) throws KivException {
    String searchFilter = createAdvancedSearchFilter(unit, showUnitsWithTheseHsaBussinessClassificationCodes);
    // Perform search without limit since the information will be filtered
    SikSearchResultList<Unit> units = searchUnits(searchFilter, LDAPConnection.SCOPE_SUB, Integer.MAX_VALUE, sortOrder);

    removeUnallowedUnits(units, showUnitsWithTheseHsaBussinessClassificationCodes);

    removeOutdatedUnits(units);

    int numberOfHits = units.size();

    // Make sure that only a subset is returned
    if (numberOfHits > maxResult) {
      units = new SikSearchResultList<Unit>(units.subList(1, maxResult));
    }

    // Set the total number of found items on the returned result set
    units.setTotalNumberOfFoundItems(numberOfHits);

    return units;
  }

  /**
   * Removes units that have passed its end date (hsaEndDate).
   * 
   * @param units
   */
  private void removeOutdatedUnits(SikSearchResultList<Unit> units) {
    Date now = new Date();
    for (Iterator<Unit> iterator = units.iterator(); iterator.hasNext();) {
      Unit unit = iterator.next();
      if (unit.getHsaEndDate() != null && now.after(unit.getHsaEndDate())) {
        iterator.remove();
      }
    }
  }

  /**
   * Remove units that don't have at least one valid hsaBusinessClassificationCode.
   * 
   * @param units
   * @param showUnitsWithTheseHsaBussinessClassificationCodes
   */
  private void removeUnallowedUnits(SikSearchResultList<Unit> units, List<Integer> showUnitsWithTheseHsaBussinessClassificationCodes) {

    // Get all health care types that are unfiltered
    HealthcareTypeConditionHelper htch = new HealthcareTypeConditionHelper();
    List<HealthcareType> allUnfilteredHealthcareTypes = htch.getAllUnfilteredHealthCareTypes();

    for (int j = units.size() - 1; j >= 0; j--) {
      boolean found = false;
      for (String s : units.get(j).getHsaBusinessClassificationCode()) {
        try {
          if (showUnitsWithTheseHsaBussinessClassificationCodes.contains(Integer.parseInt(s))) {
            found = true;
          }
        } catch (NumberFormatException e) {
          logger.debug(e);
          // We simply ignore this. hsaBusinessClassificationCodes
          // should be integers, otherwise something is seriously
          // wrong.
        }
      }

      // The unit might still be valid because of the unfiltered
      // healthcare types
      if (!found) {
        // If this unit does not match any unfiltered health care type,
        // don't include in search result
        unfilteredHealthCareTypesLoop: for (HealthcareType h : allUnfilteredHealthcareTypes) {
          for (Map.Entry<String, String> condition : h.getConditions().entrySet()) {
            String key = condition.getKey();
            key = key.substring(0, 1).toUpperCase() + key.substring(1);
            String[] conditionValues = condition.getValue().split(",");
            for (int i = 0; i < conditionValues.length; i++) {
              Method keyMethod;
              Object value = null;
              try {
                keyMethod = units.get(j).getClass().getMethod("get" + key, null);
                value = keyMethod.invoke(units.get(j), null);
              } catch (SecurityException e) {
                e.printStackTrace();
              } catch (NoSuchMethodException e) {
                e.printStackTrace();
              } catch (IllegalArgumentException e) {
                e.printStackTrace();
              } catch (IllegalAccessException e) {
                e.printStackTrace();
              } catch (InvocationTargetException e) {
                e.printStackTrace();
              }
              boolean conditionFulfilled = false;
              if (value instanceof String) {
                if (value != null) {
                  for (String v : conditionValues) {
                    if (v.equals(value)) {
                      conditionFulfilled = true;
                    }
                  }
                }
              } else if (value instanceof List<?>) {
                for (String v : conditionValues) {
                  if (((List<String>) value).contains(v)) {
                    conditionFulfilled = true;
                  }
                }
              }
              if (conditionFulfilled) {
                found = true;
                break unfilteredHealthCareTypesLoop;
              }
            }
          }
        }
      }

      if (!found) {
        units.remove(units.get(j));
      }
    }
  }

  /**
   * Search for selected unit.
   * 
   * @param searchUnitCriterions The unit to search for.
   * @param maxResult Max number of units to contain in the result.
   * @return List of found units.
   * @throws KivException .
   */
  public SikSearchResultList<Unit> searchUnits(SearchUnitCriterions searchUnitCriterions, int maxResult) throws KivException {
    String searchFilter = createSearchFilter(searchUnitCriterions);
    return searchUnits(searchFilter, LDAPConnection.SCOPE_SUB, maxResult, new UnitNameComparator());
  }

  /**
   * Fetch unit by the unit hsa id.
   * 
   * @param hsaId The hsa id of the unit.
   * @return The unit with the given hsa id.
   * @throws KivException .
   */
  public Unit getUnitByHsaId(String hsaId) throws KivException {
    String searchFilter = "(hsaIdentity=" + hsaId + ")";
    return searchUnit(KIV_SEARCH_BASE, LDAPConnection.SCOPE_SUB, searchFilter);
  }

  /**
   * Fetch unit by the unit dn.
   * 
   * @param dn The dn for the unit.
   * @return The unit with the given dn.
   * @throws KivException .
   */
  public Unit getUnitByDN(DN dn) throws KivException {
    Unit u = null;

    try {
      LDAPConnection lc = getLDAPConnection();
      try {
        u = unitFactory.reconstitute(lc.read(dn.escape().toString()));
      } finally {
        theConnectionPool.freeConnection(lc);
      }
    } catch (LDAPException e) {
      throw new KivException("An error occured in communication with the LDAP server. Message: " + e.getMessage());
    }

    return u;
  }

  /**
   * Get all hsa ids to all units.
   * 
   * @return List of hsa ids.
   * @throws KivException .
   */
  public List<String> getAllUnitsHsaIdentity() throws KivException {
    return getAllUnitsHsaIdentity(new ArrayList<Integer>());
  }

  /**
   * Get all hsa ids for units with chosen bussinessClassification codes.
   * 
   * @param showUnitsWithTheseHsaBussinessClassificationCodes List with codes.
   * @return List of found units.
   * @throws KivException .
   */
  public List<String> getAllUnitsHsaIdentity(List<Integer> showUnitsWithTheseHsaBussinessClassificationCodes) throws KivException {
    LDAPSearchConstraints constraints = new LDAPSearchConstraints();
    constraints.setMaxResults(0);
    String searchFilter = "(|(objectclass=" + Constants.OBJECT_CLASS_UNIT_SPECIFIC + ")(objectclass=" + Constants.OBJECT_CLASS_FUNCTION_SPECIFIC + "))";

    List<String> filterList = new ArrayList<String>();
    String includedBCCSearchString = makeShowUnitsWithTheseHsaBussinessClassificationCodesString(showUnitsWithTheseHsaBussinessClassificationCodes);
    filterList.add(includedBCCSearchString);
    filterList.add(searchFilter);
    // (&(par3=value3)(par4=value4
    searchFilter = makeAnd(filterList);

    String[] attributes = new String[1];
    attributes[0] = "hsaIdentity";
    List<String> result = new ArrayList<String>();

    try {
      LDAPConnection lc = getLDAPConnection();
      try {
        LDAPSearchResults searchResults = lc.search(KIV_SEARCH_BASE, LDAPConnection.SCOPE_SUB, searchFilter, attributes, false, constraints);
        // fill the list from the search result
        while (searchResults.hasMore()) {
          try {
            LDAPEntry nextEntry = searchResults.next();
            LDAPAttribute attribute = nextEntry.getAttribute(attributes[0]);
            if (attribute != null) {
              result.add(attribute.getStringValue());
            }
          } catch (LDAPException e) {
            if (e.getResultCode() == LDAPException.SIZE_LIMIT_EXCEEDED || e.getResultCode() == LDAPException.LDAP_TIMEOUT
                || e.getResultCode() == LDAPException.CONNECT_ERROR) {
              // break;
              throw new NoConnectionToServerException();
            } else {
              // take next Unit
              continue;
            }
          }
        }
      } finally {
        theConnectionPool.freeConnection(lc);
      }
    } catch (LDAPException e) {
      throw new KivException("An error occured in communication with the LDAP server. Message: " + e.getMessage());
    }

    return result;
  }

  protected SikSearchResultList<Unit> searchUnits(String searchFilter, int searchScope, int maxResult, Comparator<Unit> sortOrder) throws KivException {
    LDAPSearchConstraints constraints = new LDAPSearchConstraints();
    constraints.setMaxResults(0);
    // Get all attributes, including operational attribute createTimeStamp
    String[] attributes = { LDAPConnection.ALL_USER_ATTRS, "createTimeStamp" };

    logger.debug("LDAP search filter: " + searchFilter);

    SikSearchResultList<Unit> result = null;

    try {
      LDAPConnection lc = getLDAPConnection();
      try {
        result = extractResult(lc.search(KIV_SEARCH_BASE, searchScope, searchFilter, attributes, false, constraints), maxResult, sortOrder);
      } catch (SikInternalException e) {
        // We have no good connection to LDAP server and should be able to
        // tell the user we have no hope of success.
        throw new NoConnectionToServerException();
      } finally {
        theConnectionPool.freeConnection(lc);
      }
    } catch (LDAPException e) {
      throw new KivException("An error occured in communication with the LDAP server. Message: " + e.getMessage());
    }

    return result;
  }

  private Unit searchUnit(String searchBase, int searchScope, String searchFilter) throws KivException {
    LDAPSearchConstraints constraints = new LDAPSearchConstraints();
    constraints.setMaxResults(0);
    Unit result = new Unit();
    // Get all attributes, including operational attribute createTimeStamp
    String[] attributes = { LDAPConnection.ALL_USER_ATTRS, "createTimeStamp" };

    try {
      LDAPConnection lc = getLDAPConnection();
      try {
        result = extractSingleResult(lc.search(searchBase, searchScope, searchFilter, attributes, false, constraints));
      } finally {
        theConnectionPool.freeConnection(lc);
      }
    } catch (LDAPException e) {
      throw new KivException("An error occured in communication with the LDAP server. Message: " + e.getMessage());
    }

    return result;
  }

  /**
   * Get Ldap connection using a pool.
   * 
   * @return
   * @throws LDAPException
   * @throws SikInternalException
   */
  private LDAPConnection getLDAPConnection() throws LDAPException, NoConnectionToServerException, SikInternalException {
    LDAPConnection lc = theConnectionPool.getConnection(POOL_WAIT_TIME_MILLISECONDS);
    if (lc == null) {
      throw new SikInternalException(this, "getLDAPConnection()", "Could not get a connection after waiting " + POOL_WAIT_TIME_MILLISECONDS + " ms.");
    }
    return lc;
  }

  private Unit extractSingleResult(LDAPSearchResults searchResults) throws KivException {
    try {
      return unitFactory.reconstitute(searchResults.next());
    } catch (LDAPException e) {
      throw new KivException("An error occured in communication with the LDAP server. Message: " + e.getMessage());
    }
  }

  private SikSearchResultList<Unit> extractResult(LDAPSearchResults searchResults, int maxResult, Comparator<Unit> sortOrder) throws KivException {
    Comparator<Unit> sortOrderCurrent = sortOrder;
    SikSearchResultList<Unit> result = new SikSearchResultList<Unit>();
    while (searchResults.hasMore()) {
      try {
        Unit u = unitFactory.reconstitute(searchResults.next());
        result.add(u);
      } catch (LDAPException e) {
        if (e.getResultCode() == LDAPException.LDAP_TIMEOUT || e.getResultCode() == LDAPException.CONNECT_ERROR) {
          // break;
          throw new NoConnectionToServerException();
        } else {
          continue;
        }
      }
    }

    // Make sure we don't return duplicates
    SikSearchResultList<Unit> resultNoDuplicates = new SikSearchResultList<Unit>();
    for (Unit u : result) {
      // Would like to use "contains" which uses equals (where you could
      // test for same hsa-id) but that would break the searching.
      boolean alreadyExists = false;
      for (Unit uND : resultNoDuplicates) {
        if (u.getHsaIdentity().equals(uND.getHsaIdentity())) {
          alreadyExists = true;
          break;
        }
      }
      if (!alreadyExists) {
        resultNoDuplicates.add(u);
      }
    }

    if (sortOrderCurrent == null) {
      // No sort order was supplied, default to sorting on unit name.
      sortOrderCurrent = new UnitNameComparator();
    }

    Collections.sort(resultNoDuplicates, sortOrderCurrent);
    int resultCount = resultNoDuplicates.size();
    if (resultNoDuplicates.size() > maxResult && maxResult != 0) {
      resultNoDuplicates = new SikSearchResultList<Unit>(resultNoDuplicates.subList(0, maxResult));
    }
    resultNoDuplicates.setTotalNumberOfFoundItems(resultCount);

    return resultNoDuplicates;
  }

  /**
   * Creates a search string valid for functions from a search string valid for Units Input.
   * 
   * @param unitSearchString a full search criteria for unitSearch
   * @return A LDAP search string for search among functions
   * @throws Exception
   */
  String makeFunctionSearchStringFromUnitSearchString(String unitSearchString) {
    String functionSearchString = Formatter.replaceStringInString(unitSearchString, Constants.OBJECT_CLASS_UNIT_SPECIFIC,
        Constants.OBJECT_CLASS_FUNCTION_SPECIFIC);
    functionSearchString = Formatter
        .replaceStringInString(functionSearchString, Constants.OBJECT_CLASS_UNIT_STANDARD, Constants.OBJECT_CLASS_FUNCTION_STANDARD);
    functionSearchString = Formatter.replaceStringInString(functionSearchString, Constants.LDAP_PROPERTY_UNIT_NAME + "=", Constants.LDAP_PROPERTY_FUNCTION_NAME
        + "=");
    return functionSearchString;
  }

  /**
   * create search filter that search for both Units (and Functions).
   * 
   * @param searchUnitCriterions
   * @return
   * @throws KivException
   */
  String createSearchFilter(SearchUnitCriterions searchUnitCriterions) throws KivException {
    // create a plain unit search filter
    String unitSearchString = createUnitSearchFilter(searchUnitCriterions);

    // create a plain function search filter
    String functionSearchString = makeFunctionSearchStringFromUnitSearchString(unitSearchString);

    // add both filters together
    List<String> filterList = new ArrayList<String>();
    filterList.add(unitSearchString);
    filterList.add(functionSearchString);
    // (|(par1=value1)(par2=value2))
    String fullSearchString = makeOr(filterList);
    return fullSearchString;
  }

  /**
   * create search filter that search for both Units (and Functions).
   * 
   * @param unit
   * @param showUnitsWithTheseHsaBussinessClassificationCodes
   * @return
   * @throws KivException
   */
  String createAdvancedSearchFilter(Unit unit, List<Integer> showUnitsWithTheseHsaBussinessClassificationCodes) throws KivException {
    // create a plain unit search filter
    String unitSearchString = createAdvancedUnitSearchFilter(unit);

    // create a plain function search filter
    String functionSearchString = makeFunctionSearchStringFromUnitSearchString(unitSearchString);

    // add filters together
    List<String> filterList = new ArrayList<String>();
    if (!"".equals(unitSearchString)) {
      filterList.add(unitSearchString);
    }
    if (!"".equals(functionSearchString)) {
      filterList.add(functionSearchString);
    }
    // (|(par1=value1)(par2=value2))
    String orCriterias = makeOr(filterList);
    return orCriterias;
  }

  /**
   * Filter the units in a way that only units with a valid hsaBusinessClassificationCode or that should be matched by an unfiltered health care type is
   * included.
   * 
   * @param showUnitsWithTheseHsaBussinessClassificationCodes
   * @return
   * @throws KivException
   */
  private String makeShowUnitsWithTheseHsaBussinessClassificationCodesString(List<Integer> showUnitsWithTheseHsaBussinessClassificationCodes)
      throws KivException {
    long startTimeMillis = System.currentTimeMillis();
    List<String> filterList = new ArrayList<String>();

    for (Integer id : showUnitsWithTheseHsaBussinessClassificationCodes) {
      addSearchFilter(filterList, "hsaBusinessClassificationCode", "\"" + String.valueOf(id) + "\"");
    }

    // If there were no hsaBusinessClassificationCode filter, no further
    // conditions
    if (showUnitsWithTheseHsaBussinessClassificationCodes.size() == 0) {
      return "";
    }

    /*
     * Include unfiltered health care conditions without taking showUnitsWithTheseHsaBussinessClassificationCodes into consideration.
     */

    // Get all health care types that are unfiltered
    HealthcareTypeConditionHelper htch = new HealthcareTypeConditionHelper();
    List<HealthcareType> allUnfilteredHealthcareTypes = htch.getAllUnfilteredHealthCareTypes();

    // For every unfiltered health care type, generate a sufficient
    // condition
    addHealthCareTypeConditions(filterList, allUnfilteredHealthcareTypes);

    // (|(par1=value1)(par2=value2))
    String orCriterias = makeOr(filterList);
    long endTimeMillis = System.currentTimeMillis();
    logger.debug("Creating filter for hsaBusinessClassificationCode took: " + (endTimeMillis - startTimeMillis) + " milliseconds.");
    return orCriterias;
  }

  String createUnitSearchFilter(SearchUnitCriterions searchUnitCriterions) throws KivException {
    List<String> filterList = new ArrayList<String>();

    AndFilter andFilter = new AndFilter();
    andFilter.and(new EqualsFilter("objectclass", Constants.OBJECT_CLASS_UNIT_SPECIFIC));
    AndFilter andFilter2 = new AndFilter();

    // Create or hsaIdentity
    if (!StringUtil.isEmpty(searchUnitCriterions.getUnitId())) {
      OrFilter orHsaIdentity = new OrFilter();
      orHsaIdentity.or(createSearchFilter("hsaIdentity", searchUnitCriterions.getUnitId()));
      andFilter2.and(orHsaIdentity);
    }
    if (!StringUtil.isEmpty(searchUnitCriterions.getUnitName())) {
      OrFilter orUnitName = new OrFilter();
      orUnitName.or(createSearchFilter(Constants.LDAP_PROPERTY_UNIT_NAME, searchUnitCriterions.getUnitName()));
      andFilter2.and(orUnitName);
    }
    if (!StringUtil.isEmpty(searchUnitCriterions.getAdministrationName())) {
      Filter orFilter = generateOrFilterFromList(CodeTableName.VGR_AO3_CODE, LDAPUnitAttributes.ADMINISTRATION, searchUnitCriterions.getAdministrationName());
      andFilter2.and(orFilter);
    }
    if (!StringUtil.isEmpty(searchUnitCriterions.getLiableCode())) {
      andFilter2.and(createSearchFilter("vgrAnsvarsnummer", searchUnitCriterions.getLiableCode()));
    }
    if (!StringUtil.isEmpty(searchUnitCriterions.getBusinessClassificationName())) {
      Filter orFilter = generateOrFilterFromList(CodeTableName.HSA_BUSINESSCLASSIFICATION_CODE, LDAPUnitAttributes.BUSINESS_CLASSIFICATION_CODE,
          searchUnitCriterions.getBusinessClassificationName());
      andFilter2.and(orFilter);
    }
    if (!StringUtil.isEmpty(searchUnitCriterions.getCareTypeName())) {
      Filter orFilter = generateOrFilterFromList(CodeTableName.VGR_CARE_TYPE, LDAPUnitAttributes.CARE_TYPE, searchUnitCriterions.getCareTypeName());
      andFilter2.and(orFilter);
    }
    // create or criteria
    if (!StringUtil.isEmpty(searchUnitCriterions.getLocation())) {
      OrFilter orMunicipalityAndAddresses = new OrFilter();
      OrFilter orMunicipalityName = new OrFilter();
      orMunicipalityName.or(createSearchFilter("hsaMunicipalityName", LdapParse.escapeLDAPSearchFilter(searchUnitCriterions.getLocation())));
      // Create or criteria a bit special...
      OrFilter orPostalAddress = (OrFilter) addAddressSearchFilter(filterList, "hsaPostalAddress", LdapParse.escapeLDAPSearchFilter(searchUnitCriterions
          .getLocation()));
      // Create or criteria
      OrFilter orStreetAddress = (OrFilter) addAddressSearchFilter(filterList, "hsaStreetAddress", LdapParse.escapeLDAPSearchFilter(searchUnitCriterions
          .getLocation()));
      orMunicipalityAndAddresses.or(orMunicipalityName).or(orPostalAddress).or(orStreetAddress);
      andFilter2.and(orMunicipalityAndAddresses);
    }
    andFilter.and(andFilter2);
    return andFilter.encode();
  }

  private Filter generateOrFilterFromList(CodeTableName codeTableName, LDAPUnitAttributes criterion, String criterionValue) {
    List<String> codeFromTextValues = codeTablesService.getCodeFromTextValue(codeTableName, criterionValue);
    OrFilter orFilter = new OrFilter();
    for (String value : codeFromTextValues) {
      orFilter.or(new LikeFilter(criterion.toString(), value));
    }
    return orFilter;
  }

  String createAdvancedUnitSearchFilter(Unit unit) throws KivException {
    List<String> filterList = new ArrayList<String>();

    String searchFilter = "(&(objectclass=" + Constants.OBJECT_CLASS_UNIT_SPECIFIC + ")";

    // or criterias
    addSearchFilter(filterList, "hsaMunicipalityName", LdapParse.escapeLDAPSearchFilter(unit.getHsaMunicipalityName()));
    if (unit.getHsaMunicipalityCode() != null) {
      addSearchFilter(filterList, "hsaMunicipalityCode", LdapParse.escapeLDAPSearchFilter(unit.getHsaMunicipalityCode()));
    }

    // a bit special...
    addAddressSearchFilter(filterList, "hsaPostalAddress", LdapParse.escapeLDAPSearchFilter(unit.getHsaMunicipalityName()));

    addAddressSearchFilter(filterList, "hsaStreetAddress", LdapParse.escapeLDAPSearchFilter(unit.getHsaMunicipalityName()));
    // (|(par1=value1)(par2=value2))
    String orCriterias = makeOr(filterList);

    filterList = new ArrayList<String>();
    addSearchFilter(filterList, Constants.LDAP_PROPERTY_UNIT_NAME, unit.getName());

    // should be part of vgr vardval. This filter can only be used if vgrVardval is true.
    if (unit.isVgrVardVal()) {
      addSearchFilter(filterList, "vgrVardval", LdapParse.escapeLDAPSearchFilter(LdapParse.convertBooleanToString(unit.isVgrVardVal())));
    }

    if (!StringUtil.isEmpty(orCriterias)) {
      filterList.add(orCriterias);
    }

    addSearchFilter(filterList, "hsaIdentity", unit.getHsaIdentity());

    // Take all health care type conditions into consideration...
    if (unit.getHealthcareTypes() != null && unit.getHealthcareTypes().size() > 0) {
      addHealthCareTypeConditions(filterList, unit.getHealthcareTypes());
    }

    // (&(par3=value3)(par4=value4))
    String andCriterias = makeAnd(filterList);
    if (StringUtil.isEmpty(andCriterias)) {
      return "";
    }
    searchFilter = searchFilter + andCriterias + ")";
    return searchFilter;
  }

  /**
   * e.g. searchField=givenName searchValue=hans result=(givenName=*hans*)
   * 
   * e.g. searchField=givenName searchValue=hans-erik result=(givenName=*hans*erik*)
   * 
   * e.g. searchField=givenName searchValue="hans-erik" result=(givenName=hans-erik)
   */
  private void addSearchFilter(List<String> filterList, String searchField, String searchValue) {
    String searchValueCurrent = searchValue;
    if (!StringUtil.isEmpty(searchValueCurrent)) {
      searchValueCurrent = searchValueCurrent.trim();
      if (isExactMatchFilter(searchValueCurrent)) {
        // remove "
        searchValueCurrent = Formatter.replaceStringInString(searchValueCurrent, LDAP_EXACT_CARD, "");
        // exact match
        filterList.add("(" + searchField + "=" + searchValueCurrent.trim() + ")");
      } else {
        // change spaces to wildcards
        searchValueCurrent = Formatter.replaceStringInString(searchValueCurrent, " ", LDAP_WILD_CARD);
        searchValueCurrent = Formatter.replaceStringInString(searchValueCurrent, "-", LDAP_WILD_CARD);
        filterList.add("(" + searchField + "=" + LDAP_WILD_CARD + searchValueCurrent + LDAP_WILD_CARD + ")");
      }
    }
  }

  /**
   * e.g. searchField=givenName searchValue=hans result=(givenName=*hans*)
   * 
   * e.g. searchField=givenName searchValue=hans-erik result=(givenName=*hans*erik*)
   * 
   * e.g. searchField=givenName searchValue="hans-erik" result=(givenName=hans-erik)
   */
  private Filter createSearchFilter(String searchField, String searchValue) {
    String currentSearchValue = searchValue;
    Filter filter = null;
    if (!StringUtil.isEmpty(currentSearchValue)) {
      currentSearchValue = currentSearchValue.trim();
      if (isExactMatchFilter(currentSearchValue)) {
        // remove "
        currentSearchValue = Formatter.replaceStringInString(currentSearchValue, LDAP_EXACT_CARD, "");
        filter = new EqualsFilter(searchField, currentSearchValue.trim());
      } else {
        // change spaces to wildcards
        currentSearchValue = Formatter.replaceStringInString(currentSearchValue, " ", LDAP_WILD_CARD);
        currentSearchValue = Formatter.replaceStringInString(currentSearchValue, "-", LDAP_WILD_CARD);
        filter = new LikeFilter(searchField, LDAP_WILD_CARD + currentSearchValue + LDAP_WILD_CARD);
      }
    }
    return filter;
  }

  /**
   * e.g. searchField=hsaPostalAddress searchValue="uddevalla" result= (|(hsaPostalAddress =*uddevalla*$*$*$*$*$*)(hsaPostalAddress=*$*uddevalla*$*$*$*$*)
   * (hsaPostalAddress =*$*$*uddevalla*$*$*$*)(hsaPostalAddress=*$*$*$*uddevalla*$*$*) (hsaPostalAddress
   * =*$*$*$*$*uddevalla*$*)(hsaPostalAddress=*$*$*$*$*$*uddevalla*))
   */
  Filter addAddressSearchFilter(List<String> filterList, String searchField, String searchValue) {
    String searchValueCurrent = searchValue;
    Filter temp = null;
    if (!StringUtil.isEmpty(searchValueCurrent)) {
      searchValueCurrent = searchValueCurrent.trim();
      if (isExactMatchFilter(searchValueCurrent)) {
        // remove "
        searchValueCurrent = Formatter.replaceStringInString(searchValueCurrent, LDAP_EXACT_CARD, "");
        temp = buildAddressSearch(searchField, searchValueCurrent);
        // exact match
        filterList.add(temp.encode());
      } else {
        // change spaces to wildcards
        searchValueCurrent = Formatter.replaceStringInString(searchValueCurrent, " ", LDAP_WILD_CARD);
        searchValueCurrent = Formatter.replaceStringInString(searchValueCurrent, "-", LDAP_WILD_CARD);
        searchValueCurrent = LDAP_WILD_CARD + searchValueCurrent + LDAP_WILD_CARD;
        temp = buildAddressSearch(searchField, searchValueCurrent);
        // wildcard match
        filterList.add(temp.encode());
      }
    }
    return temp;
  }

  Filter buildAddressSearch(String searchField, String searchValue) {
    // StringBuffer buf = new StringBuffer();
    OrFilter orFilter = new OrFilter();
    for (int i = 0; i < 12; i += 2) {
      StringBuffer buf2 = new StringBuffer("*$*$*$*$*$*");
      buf2.replace(i, i + 1, searchValue);
      if (isExactMatchFilter(searchValue)) {
        orFilter.or(new EqualsFilter(searchField, buf2.toString()));
      } else {
        orFilter.or(new LikeFilter(searchField, buf2.toString()));
      }
    }

    return orFilter;
  }

  private boolean isExactMatchFilter(String searchValue) {
    if (StringUtil.isEmpty(searchValue)) {
      return false;
    }
    // it has to be at least one character between the " e.g. "a" for an
    // exact match
    if (searchValue.length() <= 2) {
      return false;
    }
    if (searchValue.startsWith(LDAP_EXACT_CARD) && searchValue.endsWith(LDAP_EXACT_CARD)) {
      return true;
    }
    return false;
  }

  private String makeOr(List<String> orFilterList) {
    String orCriterias = "";
    if (!orFilterList.isEmpty()) {
      orCriterias += "(|";
      for (String s : orFilterList) {
        orCriterias += s;
      }
      orCriterias += ")";
    }
    return orCriterias;
  }

  private String makeAnd(List<String> orFilterList) {
    String orCriterias = "";
    if (!orFilterList.isEmpty()) {
      orCriterias += "(&";
      for (String s : orFilterList) {
        orCriterias += s;
      }
      orCriterias += ")";
    }
    return orCriterias;
  }

  /**
   * Add conditions generated from supplied health care types to (also) supplied filterList.
   * 
   * @param filterList
   * @param allUnfilteredHealthcareTypes
   */
  private void addHealthCareTypeConditions(List<String> filterList, List<HealthcareType> allUnfilteredHealthcareTypes) {
    for (HealthcareType h : allUnfilteredHealthcareTypes) {

      // All conditions for the unfiltered health care type must be taken
      // into consideration
      List<String> conditionComponents = new ArrayList<String>();
      for (Map.Entry<String, String> condition : h.getConditions().entrySet()) {
        List<String> conditionComponentFilter = new ArrayList<String>();
        String[] conditionValues = condition.getValue().split(",");
        for (int i = 0; i < conditionValues.length; i++) {
          conditionComponentFilter.add("(" + condition.getKey() + "=" + conditionValues[i] + ")");
        }
        String conditionComponentsQuery = makeOr(conditionComponentFilter);
        conditionComponents.add(conditionComponentsQuery);
      }
      String healthCareTypeCondition = makeAnd(conditionComponents);

      // Add to top level, should be okay in same way as a valid
      // hsaBusinessClassificationCode
      filterList.add(healthCareTypeCondition);
    }
  }

  /**
   * 
   * @param parentUnit - unit to get subunits for
   * @param maxResult - maximum of unit to be return in the result
   * @return A list of subunits for current unit.
   * @throws KivException If something goes wrong doing search.
   */
  public SikSearchResultList<Unit> getSubUnits(Unit parentUnit, int maxResult) throws KivException {
    SikSearchResultList<Unit> subUnits = null;
    DN parentDn = parentUnit.getDn();

    try {
      LDAPConnection ldapConnection = getLDAPConnection();
      try {
        subUnits = extractResult(ldapConnection.search(parentDn.toString(), LDAPConnection.SCOPE_SUB, "(objectClass=" + Constants.OBJECT_CLASS_UNIT_SPECIFIC
            + ")", null, false), maxResult, null);
        removeUnitParentFromList(parentUnit, subUnits);
      } finally {
        theConnectionPool.freeConnection(ldapConnection);
      }
    } catch (LDAPException e) {
      throw new KivException("An error occured in communication with the LDAP server. Message: " + e.getMessage());
    }

    return subUnits;
  }

  // Remove parent unit from search result list
  private void removeUnitParentFromList(Unit parentUnit, SikSearchResultList<Unit> subUnits) {

    for (Iterator<Unit> iterator = subUnits.iterator(); iterator.hasNext();) {
      Unit unit = iterator.next();
      if (parentUnit.getHsaIdentity().equals(unit.getHsaIdentity())) {
        subUnits.remove(unit);
        break;
      }
    }
  }
}
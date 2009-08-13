package se.vgregion.kivtools.search.presentation;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import se.vgregion.kivtools.search.common.Constants;
import se.vgregion.kivtools.search.exceptions.KivNoDataFoundException;
import se.vgregion.kivtools.search.presentation.forms.PersonSearchSimpleForm;
import se.vgregion.kivtools.search.presentation.types.PagedSearchMetaData;
import se.vgregion.kivtools.search.svc.SearchService;
import se.vgregion.kivtools.search.svc.SikSearchResultList;
import se.vgregion.kivtools.search.svc.domain.Employment;
import se.vgregion.kivtools.search.svc.domain.Person;
import se.vgregion.kivtools.search.svc.domain.Unit;
import se.vgregion.kivtools.search.svc.domain.values.DN;
import se.vgregion.kivtools.search.svc.domain.values.HealthcareType;

public class SearchPersonFlowSupportBeanTest {
  private static final String NULL = "null";
  private static final String DN = "cn=abc";
  private static final String HSA_IDENTITY = "HSA-123";
  private SearchPersonFlowSupportBean bean;
  private PersonSearchSimpleForm form;
  private SearchServiceMock searchService;

  @Before
  public void setUp() throws Exception {
    bean = new SearchPersonFlowSupportBean();
    form = new PersonSearchSimpleForm();
    searchService = new SearchServiceMock();

    bean.setSearchService(searchService);
  }

  @Test
  public void testIsVgrIdSearch() throws KivNoDataFoundException {
    try {
      bean.isVgrIdSearch(null);
      fail("KivNoDataFoundException expected");
    } catch (KivNoDataFoundException e) {
      // Expected exception
    }

    assertFalse(bean.isVgrIdSearch(form));

    form.setSearchType(Constants.PERSON_SEARCH_TYPE_VGRID);
    assertTrue(bean.isVgrIdSearch(form));
  }

  @Test
  public void testDoSearch() throws KivNoDataFoundException {
    SikSearchResultList<Person> result = bean.doSearch(null);
    assertNotNull(result);
    assertEquals(0, result.size());

    try {
      bean.doSearch(form);
      fail("KivNoDataFoundException expected");
    } catch (KivNoDataFoundException e) {
      // Expected exception
    }

    form.setGivenName("a");
    try {
      bean.doSearch(form);
      fail("KivNoDataFoundException expected");
    } catch (KivNoDataFoundException e) {
      // Expected exception
    }

    SikSearchResultList<Person> persons = new SikSearchResultList<Person>();
    Person person = new Person();
    persons.add(person);
    searchService.setPersons(persons);
    result = bean.doSearch(form);
    assertNotNull(result);
    assertEquals(1, result.size());

    person.setDn(DN);
    SikSearchResultList<Employment> employments = new SikSearchResultList<Employment>();
    Employment employment = new Employment();
    employments.add(employment);
    searchService.addEmployment(person.getDn(), employments);
    result = bean.doSearch(form);
    assertNotNull(result);
    assertEquals(1, result.size());
  }

  @Test
  public void testGetPersonsForUnitsRecursive() {
    SikSearchResultList<Person> result = bean.getPersonsForUnitsRecursive(null);
    assertNotNull(result);
    assertEquals(0, result.size());

    result = bean.getPersonsForUnitsRecursive(HSA_IDENTITY);
    assertNotNull(result);
    assertEquals(0, result.size());

    Unit unit = new Unit();
    unit.setHsaIdentity(HSA_IDENTITY);
    searchService.addUnit(unit);
    result = bean.getPersonsForUnitsRecursive(HSA_IDENTITY);
    assertNotNull(result);
    assertEquals(0, result.size());

    bean.setSearchService(null);
    result = bean.getPersonsForUnitsRecursive(null);
    assertNotNull(result);
    assertEquals(0, result.size());
  }

  @Test
  public void testGetOrganisation() throws KivNoDataFoundException {
    try {
      bean.getOrganisation(null, null);
      fail("KivNoDataFoundException expected");
    } catch (KivNoDataFoundException e) {
      // Expected exception
    }

    try {
      bean.getOrganisation(null, DN);
      fail("KivNoDataFoundException expected");
    } catch (KivNoDataFoundException e) {
      // Expected exception
    }

    SikSearchResultList<Person> persons = new SikSearchResultList<Person>();
    Person person = new Person();
    persons.add(person);
    searchService.setPersons(persons);
    SikSearchResultList<Person> result = bean.getOrganisation(null, DN);
    assertNotNull(result);
    assertEquals(1, result.size());

    try {
      bean.getOrganisation(HSA_IDENTITY, null);
      fail("KivNoDataFoundException expected");
    } catch (KivNoDataFoundException e) {
      // Expected exception
    }

    Unit unit = new Unit();
    unit.setHsaIdentity(HSA_IDENTITY);
    searchService.addUnit(unit);
    try {
      bean.getOrganisation(HSA_IDENTITY, null);
      fail("KivNoDataFoundException expected");
    } catch (KivNoDataFoundException e) {
      // Expected exception
    }

    searchService.addPersonsForUnit(unit, persons);
    result = bean.getOrganisation(HSA_IDENTITY, null);
    assertNotNull(result);
    assertEquals(1, result.size());

    bean.setSearchService(null);
    result = bean.getOrganisation(null, DN);
    assertNotNull(result);
    assertEquals(0, result.size());
  }

  @Test
  public void testGetAllPersonsVgrId() throws KivNoDataFoundException {
    List<String> result = bean.getAllPersonsVgrId();
    assertNotNull(result);
    assertEquals(0, result.size());

    searchService.setExceptionToThrow(new KivNoDataFoundException());
    try {
      bean.getAllPersonsVgrId();
      fail("KivNoDataFoundException expected");
    } catch (KivNoDataFoundException e) {
      // Expected exception
    }

    searchService.setExceptionToThrow(new Exception());
    result = bean.getAllPersonsVgrId();
    assertNotNull(result);
    assertEquals(0, result.size());
  }

  @Test
  public void testGetRangePersonsVgrIdPageList() throws KivNoDataFoundException {
    List<String> result = bean.getRangePersonsVgrIdPageList(-1, -1);
    assertNotNull(result);
    assertEquals(0, result.size());

    result = bean.getRangePersonsVgrIdPageList(2, 1);
    assertNotNull(result);
    assertEquals(0, result.size());

    result = bean.getRangePersonsVgrIdPageList(0, 0);
    assertNotNull(result);
    assertEquals(0, result.size());

    List<String> allPersonsId = new ArrayList<String>();
    allPersonsId.add("abc-123");
    searchService.setAllPersonsId(allPersonsId);
    result = bean.getRangePersonsVgrIdPageList(0, 0);
    assertNotNull(result);
    assertEquals(1, result.size());

    searchService.setExceptionToThrow(new KivNoDataFoundException());
    try {
      bean.getRangePersonsVgrIdPageList(0, 0);
      fail("KivNoDataFoundException expected");
    } catch (KivNoDataFoundException e) {
      // Expected exception
    }
  }

  @Test
  public void testGetAllPersonsVgrIdPageList() throws KivNoDataFoundException {
    List<PagedSearchMetaData> result = bean.getAllPersonsVgrIdPageList(null);
    assertNotNull(result);
    assertEquals(0, result.size());

    result = bean.getAllPersonsVgrIdPageList("1");
    assertNotNull(result);
    assertEquals(0, result.size());

    List<String> allPersonsId = new ArrayList<String>();
    allPersonsId.add("abc-123");
    searchService.setAllPersonsId(allPersonsId);
    result = bean.getAllPersonsVgrIdPageList("1");
    assertNotNull(result);
    assertEquals(1, result.size());

    allPersonsId.add("def-456");
    result = bean.getAllPersonsVgrIdPageList("3");
    assertNotNull(result);
    assertEquals(1, result.size());

    searchService.setExceptionToThrow(new KivNoDataFoundException());
    try {
      bean.getAllPersonsVgrIdPageList("1");
      fail("KivNoDataFoundException expected");
    } catch (KivNoDataFoundException e) {
      // Expected exception
    }

    searchService.setExceptionToThrow(new Exception());
    result = bean.getAllPersonsVgrIdPageList("1");
    assertNotNull(result);
    assertEquals(0, result.size());
  }

  @Test
  public void testSetMaxSearchResults() throws KivNoDataFoundException {
    bean.setMaxSearchResult(3);

    form.setGivenName("a");
    try {
      bean.doSearch(form);
      fail("KivNoDataFoundException expected");
    } catch (KivNoDataFoundException e) {
      // Expected exception
    }

    this.searchService.assertMaxSearchResults(3);

    bean.setMaxSearchResult(5);
    try {
      bean.doSearch(form);
      fail("KivNoDataFoundException expected");
    } catch (KivNoDataFoundException e) {
      // Expected exception
    }

    this.searchService.assertMaxSearchResults(5);
  }

  @Test
  public void testSetPageSize() throws KivNoDataFoundException {
    bean.setPageSize(3);

    List<String> allPersonsId = new ArrayList<String>();
    allPersonsId.add("abc-123");
    allPersonsId.add("def-456");
    allPersonsId.add("ghi-789");
    searchService.setAllPersonsId(allPersonsId);
    List<PagedSearchMetaData> result = bean.getAllPersonsVgrIdPageList("1");
    assertNotNull(result);
    // 3 results per page == 1 page metadata
    assertEquals(1, result.size());

    bean.setPageSize(2);
    result = bean.getAllPersonsVgrIdPageList("1");
    assertNotNull(result);
    // 2 results per page == 2 page metadata
    assertEquals(2, result.size());

    bean.setPageSize(0);
    result = bean.getAllPersonsVgrIdPageList("1");
    assertNotNull(result);
    // 1 result per page == 3 page metadata
    assertEquals(3, result.size());
  }

  // Mocks

  class SearchServiceMock implements SearchService {
    private int maxSearchResults = -1;

    private SikSearchResultList<Person> persons = new SikSearchResultList<Person>();
    private Map<String, SikSearchResultList<Employment>> employments = new HashMap<String, SikSearchResultList<Employment>>();
    private Map<String, Unit> units = new HashMap<String, Unit>();
    private Map<String, SikSearchResultList<Person>> personsForUnit = new HashMap<String, SikSearchResultList<Person>>();
    private Exception exceptionToThrow;
    private List<String> allPersonsId = Collections.emptyList();

    public void setPersons(SikSearchResultList<Person> persons) {
      this.persons = persons;
    }

    public void addEmployment(String personDn, SikSearchResultList<Employment> employment) {
      this.employments.put(personDn, employment);
    }

    public void addUnit(Unit unit) {
      units.put(unit.getHsaIdentity(), unit);
    }

    public void addPersonsForUnit(Unit unit, SikSearchResultList<Person> persons) {
      personsForUnit.put(unit.getHsaIdentity(), persons);
    }

    public void setExceptionToThrow(Exception exception) {
      this.exceptionToThrow = exception;
    }

    public void setAllPersonsId(List<String> allPersonsId) {
      this.allPersonsId = allPersonsId;
    }

    @Override
    public SikSearchResultList<Person> searchPersons(String givenName, String familyName, String id, int maxResult) throws Exception {
      this.maxSearchResults = maxResult;
      return persons;
    }

    @Override
    public SikSearchResultList<Employment> getEmployments(String personDn) throws Exception {
      SikSearchResultList<Employment> result;
      if (employments.containsKey(personDn)) {
        result = employments.get(personDn);
      } else {
        result = new SikSearchResultList<Employment>();
      }
      return result;
    }

    @Override
    public Unit getUnitByHsaId(String hsaId) throws Exception {
      return units.get(hsaId);
    }

    @Override
    public SikSearchResultList<Unit> getSubUnits(Unit parentUnit, int maxSearchResult) throws Exception {
      return new SikSearchResultList<Unit>();
    }

    @Override
    public SikSearchResultList<Person> getPersonsForUnits(List<Unit> units, int maxResult) throws Exception {
      SikSearchResultList<Person> result = new SikSearchResultList<Person>();
      if (units != null) {
        for (Unit unit : units) {

          if (personsForUnit.containsKey(unit.getHsaIdentity())) {
            result.addAll(personsForUnit.get(unit.getHsaIdentity()));
          }
        }
      }
      return result;
    }

    @Override
    public SikSearchResultList<Person> searchPersonsByDn(String dn, int maxSearchResult) throws Exception {
      return this.persons;
    }

    @Override
    public SikSearchResultList<Person> searchPersons(String id, int maxSearchResult) throws Exception {
      return this.persons;
    }

    @Override
    public List<String> getAllPersonsId() throws Exception {
      if (this.exceptionToThrow != null) {
        throw this.exceptionToThrow;
      }
      return this.allPersonsId;
    }

    public void assertMaxSearchResults(int expected) {
      assertEquals("Unexpected value for maxSearchResults", expected, this.maxSearchResults);
    }

    // Dummy implementations

    @Override
    public SikSearchResultList<Person> searchPersonsByDn(String dn) throws Exception {
      return null;
    }

    @Override
    public List<String> getAllUnitsHsaIdentity() throws Exception {
      return null;
    }

    @Override
    public List<String> getAllUnitsHsaIdentity(List<Integer> showUnitsWithTheseHsaBussinessClassificationCodes) throws Exception {
      return null;
    }

    @Override
    public List<Employment> getEmploymentsForPerson(Person person) throws Exception {
      return null;
    }

    @Override
    public List<HealthcareType> getHealthcareTypesList() throws Exception {
      return null;
    }

    @Override
    public Person getPersonByDN(DN dn) throws Exception {
      return null;
    }

    @Override
    public Person getPersonById(String id) throws Exception {
      return null;
    }

    @Override
    public Unit getUnitByDN(String dn) throws Exception {
      return null;
    }

    @Override
    public SikSearchResultList<Unit> searchAdvancedUnits(Unit unit, Comparator<Unit> sortOrder) throws Exception {
      return null;
    }

    @Override
    public SikSearchResultList<Unit> searchAdvancedUnits(Unit unit, int maxSearchResult, Comparator<Unit> sortOrder, List<Integer> showUnitsWithTheseHsaBussinessClassificationCodes) throws Exception {
      return null;
    }

    @Override
    public SikSearchResultList<Person> searchPersons(String id) throws Exception {
      return null;
    }

    @Override
    public SikSearchResultList<Person> searchPersons(String givenName, String familyName, String id) throws Exception {
      return null;
    }

    @Override
    public SikSearchResultList<Unit> searchUnits(Unit unit) throws Exception {
      return null;
    }

    @Override
    public SikSearchResultList<Unit> searchUnits(Unit unit, int maxSearchResult) throws Exception {
      return null;
    }
  }
}

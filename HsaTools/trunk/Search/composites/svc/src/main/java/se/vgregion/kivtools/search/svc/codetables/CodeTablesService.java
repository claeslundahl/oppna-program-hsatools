package se.vgregion.kivtools.search.svc.codetables;

import java.util.List;

import se.vgregion.kivtools.search.domain.values.CodeTableName;

/**
 * Provides mapping between codes and descriptions for values.
 * 
 * @author argoyle
 */
public interface CodeTablesService {

  /**
   * Retrieves the description for the provided code in the table identified by the provided code table name.
   * 
   * @param codeTableName The name of the code table to use for the lookup.
   * @param code The code to lookup.
   * @return The description for the provided code.
   */
  String getValueFromCode(CodeTableName codeTableName, String code);

  /**
   * Retrieves a list of codes where the description in the table identified by the provided code table name matches the provided text value.
   * 
   * @param codeTableName The name of the code table to use for the lookup.
   * @param textValue The text value to get matches for.
   * @return A list of codes for the entries where the description matched the provided value.
   */
  List<String> getCodeFromTextValue(CodeTableName codeTableName, String textValue);

  /**
   * Retrieves a list of descriptions that matches the provided text value from the table identified by the provided code table name.
   * 
   * @param codeTableName The name of the code table to use for the lookup.
   * @param textValue The text value to get matches for.
   * @return A list of descriptions for the entries where the description matched the provided value.
   */
  List<String> getValuesFromTextValue(CodeTableName codeTableName, String textValue);

  /**
   * This method will return all item values for a chosen code table.
   * 
   * @param codeTableName Name of the code table to get value items from.
   * @return A list of all value items for chosen code table.
   * @throws IllegalArgumentException If an invalid code table name is used.
   */
  List<String> getAllValuesItemsFromCodeTable(String codeTableName);
}

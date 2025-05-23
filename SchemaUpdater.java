import java.util.*;

public class SchemaUpdater {

    @SuppressWarnings("unchecked")
    public Map<String, Object> updateSchema(Map<String, Object> schemaMap, Map<String, Object> valuesMap) {
        if (!schemaMap.containsKey("data")) return schemaMap;

        List<Map<String, Object>> dataList = (List<Map<String, Object>>) schemaMap.get("data");

        for (Map<String, Object> field : dataList) {
            String alias = (String) field.get("alias");
            
            if (alias == null || !valuesMap.containsKey(alias)) continue;

            Object valueFromMap2 = valuesMap.get(alias);

            List<?> valueListInput;
            if (valueFromMap2 instanceof List<?>) {
                valueListInput = (List<?>) valueFromMap2;
            } else {
                valueListInput = List.of(valueFromMap2); // single value wrapped in a list
            }

            
            
            List<Map<String, Object>> validatedValueList = new ArrayList<>();

            for (Object val : valueListInput) {
                Map<String, Object> valueEntry = new HashMap<>();
                valueEntry.put("value", val);
                valueEntry.put("value_url", null);
                valueEntry.put("dynamic_url", null);
                validatedValueList.add(valueEntry);
            }

            if (!validatedValueList.isEmpty()) {
                field.put("value", validatedValueList);
            }
        }
        return schemaMap;
    }

    
    public static void main(String[] args) {
        Map<String, Object> schemaMap = new HashMap<>();
        schemaMap.put("schema_type", "duediligence");

        List<Map<String, Object>> dataList = new ArrayList<>();

        // Field 1: age (int, not multiple)
        Map<String, Object> field1 = new HashMap<>();
        field1.put("type", "int");
        field1.put("has_multiple", true);
        field1.put("has_href", false);
        field1.put("is_mandatory", false);
        field1.put("description", "Project age");
        field1.put("status", true);
        field1.put("visible", 1);
        field1.put("alias", "age");

        List<Map<String, Object>> field1Values = new ArrayList<>();
        Map<String, Object> field1Value = new HashMap<>();
        field1Value.put("value", 4);
        field1Value.put("value_url", null);
        field1Value.put("dynamic_url", null);
        field1Values.add(field1Value);
        field1.put("value", field1Values);

        field1.put("show_in_grid", false);
        field1.put("show_in_create", false);
        field1.put("order", 2);
        field1.put("field_unique_key", "fn_2");

        // Field 2: field1 (text, multiple allowed)
        Map<String, Object> field2 = new HashMap<>();
        field2.put("type", "text");
        field2.put("has_multiple", true);
        field2.put("has_href", false);
        field2.put("is_mandatory", false);
        field2.put("description", "Sample Custom Field for Testing");
        field2.put("status", true);
        field2.put("visible", 1);
        field2.put("alias", "field1");

        List<Map<String, Object>> field2Values = new ArrayList<>();
        Map<String, Object> field2Value = new HashMap<>();
        field2Value.put("value", "2025-05-20T11:06:37.683361");
        field2Value.put("value_url", null);
        field2Value.put("dynamic_url", null);
        field2Values.add(field2Value);
        field2.put("value", field2Values);

        field2.put("show_in_grid", false);
        field2.put("show_in_create", false);
        field2.put("order", 1);
        field2.put("field_unique_key", "fn_1");

        dataList.add(field1);
        dataList.add(field2);
        schemaMap.put("data", dataList);

        // Input values (map2)
        Map<String, Object> valuesMap = new HashMap<>();
        valuesMap.put("age", List.of(15, 18)); // should fail: multiple values but has_multiple=false
        valuesMap.put("field1", List.of("value1", 12)); // valid
        valuesMap.put("extra_field", List.of("should be ignored")); // no matching alias

        System.out.println("Before Update:");
        printSchema(schemaMap);

        
        Map<String, Object> schemaMappost = new SchemaUpdater().updateSchema(schemaMap, valuesMap);
        System.out.println("\nAfter Update:");
        printSchema(schemaMappost);
    }

    private static void printSchema(Map<String, Object> schemaMap) {
        for (Map.Entry<String, Object> entry : schemaMap.entrySet()) {
            if (entry.getKey().equals("data")) {
                System.out.println("data:");
                List<?> dataList = (List<?>) entry.getValue();
                for (Object item : dataList) {
                    System.out.println("  " + item);
                }
            } else {
                System.out.println(entry.getKey() + ": " + entry.getValue());
            }
        }
    }
}

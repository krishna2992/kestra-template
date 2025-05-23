package io.diligencevault.plugin.core.tasks;
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
}

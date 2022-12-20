package project.inference;

import java.util.HashMap;
import java.util.Map;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class InferenceEngine {
    public static JSONArray resolve(HashMap<String, HashMap<String, HashMap<String, String>>> store, String category, JSONArray filters, HashMap<String, HashMap<String, String>> bundles) {
        HashMap<String, HashMap<String, String>> items = store.get(category);
        JSONArray selectedItems = new JSONArray();
        int requestedQuantity = 1;

        if (items != null) {
            for (HashMap<String, String> item : items.values()) {
                boolean selected = true;
                
                if (Integer.parseInt(item.get("quantity")) == 0) {
                    selected = false;
                } else {
                    for (Object f : filters) {
                        JSONObject filter = (JSONObject) f; 
                        String feature = (String) filter.get("feature");
                        Condition condition = Condition.fromString((String) filter.get("condition"));
                        String value = (String) filter.get("value");

                        if (feature.equals("quantity")) {
                            requestedQuantity = Integer.parseInt(value);
                            if (Integer.parseInt(item.get("quantity")) < requestedQuantity) {
                                selected = false;
                                break;
                            }
                        } else if (!condition.test(item.get(feature), value)) {
                            selected = false;
                            break;
                        }
                    }
                }

                if (selected) {
                    JSONObject itemJSON = new JSONObject();
                    for (Map.Entry<String, String> e : item.entrySet()) {
                        if (e.getKey().equals("bundle")) {
                            JSONObject bundleJSON = new JSONObject(bundles.get(e.getValue()));
                            String bundleItemID = (String) bundleJSON.remove("item id");
                            String bundleItemCategory = (String) bundleJSON.get("category");

                            HashMap<String, String> bundleItem = store.get(bundleItemCategory).get(bundleItemID);
                            bundleJSON.put("item", new JSONObject(bundleItem));

                            itemJSON.put(e.getKey(), bundleJSON);
                        } else {
                            itemJSON.put(e.getKey(), e.getValue());
                        }
                    }
                    int price = Integer.parseInt(item.get("price"));
                    itemJSON.put("total price", price * requestedQuantity);

                    selectedItems.add(itemJSON);
                }
            }
        }
        return selectedItems;
    }
}

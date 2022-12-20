package project;

import java.util.Scanner;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.util.ExtendedProperties;
import jade.util.leap.Properties;
import project.agent.Indexer;

@SpringBootApplication
@RestController
public class App {

    public static Indexer indexer;

    public static void main( String[] args ) {
        Properties prop = new ExtendedProperties();
        prop.setProperty(Profile.AGENTS, "seller1:project.agent.Seller(store1.json);seller2:project.agent.Seller(store2.json);seller3:project.agent.Seller(store3.json);indexer:project.agent.Indexer(seller1,seller2,seller3)");
        ProfileImpl profMain = new ProfileImpl(prop);
        Runtime runtime = Runtime.instance();
        runtime.createMainContainer(profMain);
    }

    @GetMapping("/api/description")
    public String fetchDescription() {
        try {
            ResourceLoader resourceLoader = new DefaultResourceLoader();
            Resource resource = resourceLoader.getResource("classpath:/stores/description.json");
            Scanner scanner = new Scanner(resource.getInputStream());
            StringBuilder description = new StringBuilder();
            while (scanner.hasNextLine()) 
                description.append(scanner.nextLine());
            scanner.close();

            return description.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @PostMapping("/api/items")
    public String fetchItems(@RequestBody String body) throws Exception {
        /**
         * request structure
         * category : string
         * filters : array of filter including (quantity (optional))
         *     filter : feature, condition, value
         * date : month and day (optional)
         * response structure
         * array of :
         *     seller : seller id
         *     name : seller name
         *     items : array of items
         *         item : informations about the item + total price + bundle if any
         *     promotions : if any 
         */

        System.out.println("[ /api/items | request ] : " + body + "\n");
        String response = indexer.getItems(body);
        System.out.println("[ /api/items | response ] : " + response + "\n\n");
        return response;
    }

    @PostMapping("/api/item/purchase")
    public String purchaseItem(@RequestBody String body) throws Exception {
        /**
         * request structure
         * seller : seller name
         * details : category, id, quantity, bundle, date (optional)
         * response structure
         * status : failed or completed 
         */

        System.out.println("[ /api/item/purchase | request ] : " + body + "\n");
        String response = indexer.purchaseItem(body);
        System.out.println("[ /api/item/purchase | response ] : " + response + "\n\n");
        return response;
    }
}

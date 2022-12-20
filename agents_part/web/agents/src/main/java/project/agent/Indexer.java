package project.agent;

import java.util.ArrayList;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.springframework.boot.SpringApplication;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import project.App;

public class Indexer extends Agent {

    private static long conversationID = 0;
    private ArrayList<AID> sellers;

    private class FetchItemsServer extends OneShotBehaviour {
        private String request;
        private long id;
        private ArrayList<AID> sellers;

        public FetchItemsServer(ArrayList<AID> sellers, String request, long id) {
            this.request = request;
            this.id = id;
            this.sellers = sellers;
        }

        @Override
        public void action() {
            ACLMessage message = new ACLMessage(ACLMessage.CFP);
            for (AID seller : sellers) {
                message.addReceiver(seller);
            }
            message.setConversationId(Long.toString(id));
            message.setContent(request);
            send(message);
        }
    }

    private class PurchaseItemServe extends OneShotBehaviour {
        private AID seller;
        private String details;
        private long id;

        public PurchaseItemServe(AID seller, String details, long id) {
            this.seller = seller;
            this.details = details;
            this.id = id;
        }

        @Override
        public void action() {
            ACLMessage message = new ACLMessage(ACLMessage.ACCEPT_PROPOSAL);
            message.addReceiver(seller);
            message.setConversationId(Long.toString(id));
            message.setContent(details);
            send(message);
        }
    }

    protected void setup() {
        System.out.println("[ " + this.getAID().getName() + " ] : Starting");

        this.sellers = new ArrayList<AID>();

        Object[] args = getArguments();
        if (args != null && args.length > 0) {
            for (int i = 0; i < args.length; ++i)
                sellers.add(new AID((String) args[i], AID.ISLOCALNAME));
        }

        App.indexer = this;
        System.getProperties().put("server.port", 8000);
        SpringApplication.run(App.class);
    }
    
    public String getItems(String request) {
        long id = conversationID++;
        addBehaviour(new FetchItemsServer(sellers, request, id));
        
        JSONArray responseJSON = new JSONArray();
        JSONParser parser = new JSONParser();
        MessageTemplate messageTemplate = MessageTemplate.and(
            MessageTemplate.MatchConversationId(Long.toString(id)),
            MessageTemplate.MatchPerformative(ACLMessage.PROPOSE)
        );
        for (int i = 0; i < sellers.size(); ++i) {
            ACLMessage response = blockingReceive(messageTemplate);
            try {
                responseJSON.add((JSONObject) parser.parse(response.getContent()));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return responseJSON.toJSONString();
    }

    public String purchaseItem(String request) {
        long id = conversationID++;
        try {
            JSONParser parser = new JSONParser();
            JSONObject requestJSON = (JSONObject) parser.parse(request);
            addBehaviour(new PurchaseItemServe(
                new AID((String) requestJSON.get("seller"), AID.ISLOCALNAME),
                ((JSONObject) requestJSON.get("details")).toJSONString(),
                id
            ));

            MessageTemplate messageTemplate = MessageTemplate.and(
                MessageTemplate.MatchConversationId(Long.toString(id)),
                MessageTemplate.MatchPerformative(ACLMessage.INFORM)
            );
            ACLMessage response = blockingReceive(messageTemplate);
            return response.getContent();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return "{\"status\" : \"failed\", \"message\" : \"internal error\"}";
    }
}

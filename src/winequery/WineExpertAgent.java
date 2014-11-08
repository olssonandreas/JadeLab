
package winequery;


import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.util.FileManager;
import generated.Wine;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.*;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.SearchConstraints;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.*;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import prologjava.QueryWineDatabase;


public class WineExpertAgent extends Agent {

    String wineName, winery, region, body, flavor, sugar, color;
    Model model;
    Wine allWine;
    Wine loopWines[];
    String prologPredicate = "";
    // Pointer to RDF file to read.
    String inputFileName = "wine.rdf";
    public String targetAgent;
    private Wine Wine;

    protected void setup() {
        
        ServiceDescription sd = new ServiceDescription();
        sd.setType("WineExpert");
        sd.setName(getLocalName());
        register(sd);
        AID agent = getService("QueryWine");
        System.out.println("\nQueryWine: "
                + (agent == null ? "not Found" : agent.getName()));
        AID[] sellers = searchDF("QueryWine");
      
        for (int i = 0; i < sellers.length; i++) {
            System.out.print(sellers[i].getLocalName() + ",  ");
            targetAgent = sellers[i].getLocalName();
        }
        System.out.println();
       
        addBehaviour(new CyclicBehaviour(this) {
            public void action() {
                
                try {
                    querySparql();
                } catch (IOException ex) {
                    Logger.getLogger(WineExpertAgent.class.getName()).log(Level.SEVERE, null, ex);
                }

                ACLMessage msg = receive();
                if (msg != null) {
                    System.out.println("WEA - incoming message: "
                            + msg.getContent());
                    //Sends first query
                    if (msg.getConversationId().contains("1")) {
                        ACLMessage remsg = new ACLMessage(ACLMessage.INFORM);
                        //remsg.setContent("Pong1");
                        String message = "";
                        try {
                            message = runModel(msg.getContent());
                        } catch (Exception ex) {
                            Logger.getLogger(WineExpertAgent.class.getName()).log(Level.SEVERE, null, ex);
                        }
                        remsg.setContent(message);
                        remsg.addReceiver(new AID((targetAgent), AID.ISLOCALNAME));
                        remsg.setConversationId("1");
                        System.out.println("WEA - remsg1: " + remsg.getContent() + " sent with ID " + remsg.getConversationId());
                        send(remsg);

                    }
                    //Sends second query
                    if (msg.getConversationId().contains("2")) {
                        ACLMessage remsg = new ACLMessage(ACLMessage.INFORM);
                        //remsg.setContent("Pong2");
                        String message = "";
                        try {
                            message = runModel(msg.getContent());
                        } catch (Exception ex) {
                            Logger.getLogger(WineExpertAgent.class.getName()).log(Level.SEVERE, null, ex);
                        }
                        remsg.setContent(message);
                        remsg.addReceiver(new AID((targetAgent), AID.ISLOCALNAME));
                        remsg.setConversationId("2");
                        System.out.println("WEA - remsg2: " + remsg.getContent() + " sent with ID " + remsg.getConversationId());
                        send(remsg);

                    }
                    //Sends third query
                    if (msg.getConversationId().contains("3")) {
                        ACLMessage remsg = new ACLMessage(ACLMessage.INFORM);
                        //remsg.setContent("Pong3");
                        String message = "";
                        try {
                            message = runModel(msg.getContent());
                        } catch (Exception ex) {
                            Logger.getLogger(WineExpertAgent.class.getName()).log(Level.SEVERE, null, ex);
                        }
                        remsg.setContent(message);
                        remsg.addReceiver(new AID((targetAgent), AID.ISLOCALNAME));
                        remsg.setConversationId("3");
                        System.out.println("WEA - remsg3: " + remsg.getContent() + " sent with ID " + remsg.getConversationId());
                        send(remsg);

                    }
                }
                block();
            }

        });
        
         

    }

    void register(ServiceDescription sd) //  --------------------------------------
    {
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());

        try {
            DFAgentDescription list[] = DFService.search(this, dfd);
            if (list.length > 0) {
                DFService.deregister(this);
            }

            dfd.addServices(sd);
            DFService.register(this, dfd);
        } catch (FIPAException fe) {
            fe.printStackTrace();
        }
    }

    AID getService(String service) //  ---------------------------------
    {
        DFAgentDescription dfd = new DFAgentDescription();
        ServiceDescription sd = new ServiceDescription();
        sd.setType(service);
        dfd.addServices(sd);
        try {
            DFAgentDescription[] result = DFService.search(this, dfd);
            if (result.length > 0) {
                return result[0].getName();
            }
        } catch (FIPAException fe) {
            fe.printStackTrace();
        }
        return null;
    }

    AID[] searchDF(String service) //  ---------------------------------
    {
        DFAgentDescription dfd = new DFAgentDescription();
        ServiceDescription sd = new ServiceDescription();
        sd.setType(service);
        dfd.addServices(sd);

        SearchConstraints ALL = new SearchConstraints();
        ALL.setMaxResults(new Long(-1));

        try {
            DFAgentDescription[] result = DFService.search(this, dfd, ALL);
            AID[] agents = new AID[result.length];
            for (int i = 0; i < result.length; i++) {
                agents[i] = result[i].getName();
            }
            return agents;

        } catch (FIPAException fe) {
            fe.printStackTrace();
        }

        return null;
    }

    /**
     *
     * This method takes an prolog question as input and gives you a random
     * answer from the arraylist.
     *
     * @param query
     * @throws Exception
     */
    public String runModel(String _query) throws Exception {
        String xmlResult;

        Wine w = new Wine();
       
        String prologPredicatsFile = "PrologPredicates.pro";

        QueryWineDatabase qwd = new QueryWineDatabase(prologPredicatsFile);
        ArrayList<String> qp = qwd.queryProlog(_query);

        // Picks a random from the arraylist
        Random r = new Random();
        int i = r.nextInt(qp.size());
        String s = qp.get(i);
        w.setWineName(s);

        Writer writer = new StringWriter();
        javax.xml.bind.JAXBContext jaxbCtx;
        javax.xml.bind.Marshaller marshaller;
        try {
            jaxbCtx = javax.xml.bind.JAXBContext.newInstance(w.getClass().getPackage().getName());
            marshaller = jaxbCtx.createMarshaller();
            marshaller.setProperty(javax.xml.bind.Marshaller.JAXB_ENCODING, "UTF-8"); //NOI18N
            marshaller.setProperty(javax.xml.bind.Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
            marshaller.marshal(w, writer);
        } catch (javax.xml.bind.JAXBException ex) {
            // XXXTODO Handle exception
            java.util.logging.Logger.getLogger("global").log(java.util.logging.Level.SEVERE, null, ex); //NOI18N
        }

        return writer.toString();

    }

    /**
     *
     * This method writes the prolog predicats to a .pro file
     *
     * @throws IOException
     */
    void predicatesToFile(String predicates) throws IOException {

        File file = new File("PrologPredicates.pro");
        file.createNewFile();
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            writer.write(predicates);
            writer.flush();
        }
    }

    /**
     * This metod queries the ontology for all wines and their resources and
     * returns them as prolog predicates
     *
     * @return all wines in prolog predicates
     */
    public void querySparql() throws IOException {

        String queryString
                = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n"
                + "PREFIX owl: <http://www.w3.org/2002/07/owl#>\n"
                + "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>\n"
                + "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n"
                + "PREFIX wine:<http://www.w3.org/TR/2003/PR-owl-guide-20031209/wine#>\n"
                + "select  distinct  ?wine ?winery ?region ?flavor ?body ?sugar ?color where {\n"
                + "?a ?p ?winename.\n"
                + "?wine ?p ?winery .\n"
                + "?a wine:locatedIn ?region .\n"
                + " { ?a wine:hasMaker ?winery . }\n"
                + "OPTIONAL { ?a wine:hasFlavor ?flavor .}\n"
                + "OPTIONAL { ?a wine:hasBody ?body . }\n"
                + "OPTIONAL { ?a wine:hasSugar ?sugar }\n"
                + "OPTIONAL {?a wine:hasColor ?color}}";

        model = ModelFactory.createDefaultModel();
        InputStream in = FileManager.get().open(inputFileName);
        if (in == null) {
            throw new IllegalArgumentException("File: " + inputFileName + " not found");
        }
        model.read(in, "");

        Query query = QueryFactory.create(
                queryString);

        //Run query 
        try (QueryExecution qexec = QueryExecutionFactory.create(query, model)) {

            // Results from query 
            Iterator<QuerySolution> results = qexec.execSelect();

            while (results.hasNext()) {
                // Parse through the result and add the winery to arraylist
                QuerySolution row = results.next();
                wineName = row.getResource("wine").getLocalName();
                winery = row.getResource("winery").getLocalName();
                region = row.getResource("region").getLocalName();

                // inconsistency in the ontology forces us to check if the
                // resource is empty.
                if (row.getResource("body") != null) {
                    body = row.getResource("body").getLocalName();

                } else {
                    body = "no_bootay";

                }
                if (row.getResource("sugar") != null) {
                    sugar = row.getResource("sugar").getLocalName();

                } else {
                    sugar = "no_sugar";
                }
                if (row.getResource("flavor") != null) {
                    flavor = row.getResource("flavor").getLocalName();

                } else {
                    flavor = "no_flavor";
                }
                if (row.getResource("color") != null) {
                    color = row.getResource("color").getLocalName();
                } else {
                    color = "no_color";
                }

                prologPredicate += "wine(" + wineName.toLowerCase() + "," + winery.toLowerCase() + "," + region.toLowerCase() + "," + body.toLowerCase() + "," + flavor.toLowerCase() + "," + sugar.toLowerCase() + "," + color.toLowerCase() + ").\n";
            }
            qexec.close();

        }
        
   
        predicatesToFile(prologPredicate);
        
       
    }

}

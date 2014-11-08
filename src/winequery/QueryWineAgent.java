package winequery;



import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.*;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.SearchConstraints;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;

public class QueryWineAgent extends Agent {

    public String targetAgent;

    protected void setup() {
        ServiceDescription sd = new ServiceDescription();
        sd.setType("QueryWine");
        sd.setName(getLocalName());
        register(sd);
        AID agent = getService("WineExpert");
        System.out.println("\nWineExpert: "
                + (agent == null ? "not Found" : agent.getName()));
        AID[] buyers = searchDF("WineExpert");

        for (int i = 0; i < buyers.length; i++) {
            System.out.print(buyers[i].getLocalName() + ",  ");
            targetAgent = buyers[i].getLocalName();
        }
        System.out.println();

        addBehaviour(new CyclicBehaviour(this) {

            public void action() {
                ACLMessage remsg = receive();
                if (remsg == null) {
                    System.out.println("--- Question 1 ---");
                    ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
                    msg.setContent("wine(Output,_,naparegion,_,_,_,_)");
                    msg.addReceiver(new AID(targetAgent, AID.ISLOCALNAME));
                    msg.setConversationId("1");
                    send(msg);
                    System.out.println("QWA - msg1: " + msg.getContent() + " sent with ID " + msg.getConversationId());
                }
                if (remsg != null) {
                    if (remsg.getConversationId().contains("1")) {
                        System.out.println("QWA - incoming message: " + remsg.getContent());
                        System.out.println("--- Question 2 ---");
                        ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
                        msg.setContent("wine(Output,_,_,_,_,dry,_)");
                        msg.addReceiver(new AID(targetAgent, AID.ISLOCALNAME));
                        msg.setConversationId("2");
                        send(msg);
                        System.out.println("QWA - msg2: " + msg.getContent() + " sent with ID " + msg.getConversationId());
                    }

                    if (remsg.getConversationId().contains("2")) {
                        System.out.println("QWA - incoming message: " + remsg.getContent());
                        System.out.println("--- Question 3 --");
                        ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
                        msg.setContent("wine(_,Output,_,full,_,_,_)");
                        msg.addReceiver(new AID(targetAgent, AID.ISLOCALNAME));
                        msg.setConversationId("3");
                        System.out.println("QWA - msg3: " + msg.getContent() + " sent with ID " + msg.getConversationId());
                        send(msg);
                    }

                    if (remsg.getConversationId().contains("3")) {
                        System.out.println("QWA - incoming message: " + remsg.getContent());
                        System.out.println("== Answer" + " <- "
                                + remsg.getContent() + " from "
                                + remsg.getSender().getName());

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

}

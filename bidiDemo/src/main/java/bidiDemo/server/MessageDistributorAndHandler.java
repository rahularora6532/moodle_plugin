/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bidiDemo.server;

import bidiDemo.common.Message;
import bidiDemo.common.MessageService.Iface;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.thrift.TException;

/**
 * Class to implement the MessageService interface generated by Thrift and 
 * handling message sending requests sent by server to client
 * 
 * @author rarora
 */
public class MessageDistributorAndHandler{
    //For storing client information
    //Mapping which client send what
    private final List<MessageServiceClient> clients;

    public MessageDistributorAndHandler(){
        //Using synchronized map to avoid ConcurrentModificationException which 
        //may arise when we concurrently try to modify the map in different threads
        clients = Collections.synchronizedList(new ArrayList<>());
    }
    
    public void addClient(MessageServiceClient client){
        clients.add(client);
    }

    
    //Implements MessageService Interface
    public class MessageServiceHandler implements Iface{
        private MessageServiceClient client;
        
        MessageServiceHandler(MessageServiceClient c){
            client = c;
        }
        
        @Override
        public void sendMessage(Message msg) throws TException {
            System.out.println("---------------------");
            System.out.println("Message: " + msg.message);
            System.out.println("-Sent by " + msg.clientName);
            System.out.println("---------------------");
            
            for(MessageServiceClient client: clients){
                if(client.equals(this.client)){
                    this.client.setMessage(msg);
                }
            }
        }
        
    }
    
    //Utility class to send messages to client
    //Implements run function of new thread
    public class Messenger implements Runnable{

        @Override
        public void run() {
            while(true){
                //synchronized block
                synchronized(clients){
                    Iterator<MessageServiceClient> clientItr = clients.iterator();
                    if (clientItr.hasNext()) {
                        MessageServiceClient client = clientItr.next();
                        if(client.getMessage()!=null){
                            try {
                                Message msg = new Message("A","Yes " + client.getMessage().clientName + "! I am there.");
                                //Message msg = new Message("A","Greetings from server!");
                                client.sendMessage(msg);
                            } catch (TException te) {
                                System.out.println(te.getStackTrace());
                            }
                            clientItr.remove();
                        }
                    }
                }
            }
        }
        
    }
    
}

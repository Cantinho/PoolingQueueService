package br.com.example.sqs.impl;

import br.com.example.bean.Message;
import br.com.example.sqs.SimpleMessageQueue;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by jordaoesa on 25/11/16.
 */
@Component
public class SimpleMessageQueueImpl implements SimpleMessageQueue {

    private Queue<Message> centralQueue = new LinkedBlockingQueue<>();
    private Queue<Message> applicationQueue = new LinkedBlockingQueue<>();

    public boolean produceMessageToCentral(Message message) {
        return this.centralQueue.add(message);
    }

    public Message peekMessageOfCentral() {
        return this.centralQueue.peek();
    }

    public Message consumeMessageOfCentral() {
        return this.centralQueue.poll();
    }

    public List<Message> consumeMessageOfCentral(final int amount) {
        List<Message> messages = new LinkedList<>();
        synchronized (this.centralQueue) {
            int counter = amount;
            Iterator<Message> messageIterator = this.centralQueue.iterator();
            while(messageIterator.hasNext()) {
                messages.add(messages.size(), messageIterator.next());
                counter--;
                if(counter == 0) {
                    break;
                }
            }
        }
        return messages;
    }



    public boolean produceMessageToApplication(Message message) {
        return this.applicationQueue.add(message);
    }

    public Message peekMessageOfApplication() {
        return this.applicationQueue.peek();
    }

    public Message consumeMessageOfApplication() {
        return this.applicationQueue.poll();
    }

    public List<Message> consumeMessageOfApplication(final int amount) {
        List<Message> messages = new LinkedList<>();
        synchronized (this.applicationQueue) {
            int counter = amount;
            Iterator<Message> messageIterator = this.applicationQueue.iterator();
            while(messageIterator.hasNext()) {
                messages.add(messages.size(), messageIterator.next());
                counter--;
                if(counter == 0) {
                    break;
                }
            }
        }
        return messages;
    }
}

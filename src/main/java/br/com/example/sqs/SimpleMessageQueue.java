package br.com.example.sqs;

import br.com.example.bean.Message;

import java.util.List;

/**
 * Created by jordaoesa on 25/11/16.
 */
public interface SimpleMessageQueue {

    boolean produceMessageToCentral(Message message);

    Message peekMessageOfCentral();

    Message consumeMessageOfCentral();

    List<Message> consumeMessageOfCentral(final int amount);

    boolean produceMessageToApplication(Message message);

    Message peekMessageOfApplication();

    Message consumeMessageOfApplication();

    List<Message> consumeMessageOfApplication(final int amount);


}

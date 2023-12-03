
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeoutException;


public class Consumer {

  private final static String QUEUE_NAME = "reaction";
  private static final String consumerTag = "reactionConsumer";
  private final static int NUM_OF_CONSUMERS = 30;
  private static Connection connection;
  private static Channel[] channels;
  private static ExecutorService executorService;


  public static void main (String[] args){
    System.out.println("Starting");
    ConnectionFactory rmqFactory = new ConnectionFactory();
    try {
      rmqFactory.setUri("amqps://b-193a2b4-3adb-4fe0-9e8c-b848951c326.mq.us-west-2.amazonaws.com:5671");
      rmqFactory.setUsername("username");
      rmqFactory.setPassword("lyz199991");
    } catch (Exception e) {
      System.err.println(e.getMessage());
    }
    try{
      connection = rmqFactory.newConnection();
    }catch (IOException | TimeoutException e){
      System.err.println(e.getMessage());
    }
    channels = new Channel[NUM_OF_CONSUMERS];
    executorService = Executors.newFixedThreadPool(NUM_OF_CONSUMERS);

    try {
      for(int i = 0; i < NUM_OF_CONSUMERS; i++){
        Channel curChannel = connection.createChannel();
        channels[i] = curChannel;
        executorService.submit(() -> {
          try {
            boolean autoAck = true;
            curChannel.basicConsume(QUEUE_NAME, autoAck, consumerTag, new MessageConsumer(curChannel));
          } catch (IOException e) {
            System.err.println(e.getMessage());
          }
        });
      }
    } catch (IOException e) {
      System.err.println(e.getMessage());
    }

    Runtime.getRuntime().addShutdownHook(new Thread(() -> {

      try {
        for(int i = 0; i < channels.length; i++){
          if (channels[i] != null && channels[i].isOpen()) {
            channels[i].basicCancel(consumerTag);
            channels[i].close();
          }
        }
        if (connection != null && connection.isOpen()) {
          connection.close();
        }
      } catch (IOException | TimeoutException e) {
        System.err.println(e.getMessage());
      }
    }));
    executorService.shutdown();
  }
}

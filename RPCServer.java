import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.rabbitmq.client.*;

public class RPCServer {

    private static final String RPC_QUEUE_NAME = "rpc_queue";
    private final static String HOST = "localhost";
    private final static String USER = "admin";
    private final static String PASSWD = "admin";

    public static void main(String[] argv) throws Exception {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(HOST);
        factory.setUsername(USER);
        factory.setPassword(PASSWD);

        try (Connection connection = factory.newConnection();
             Channel channel = connection.createChannel()) {
            channel.queueDeclare(RPC_QUEUE_NAME, false, false, false, null);
            channel.queuePurge(RPC_QUEUE_NAME);

            channel.basicQos(1);

            System.out.println(" [x] Awaiting RPC requests");

            Object monitor = new Object();
            DeliverCallback deliverCallback = (consumerTag, delivery) -> {
                AMQP.BasicProperties replyProps = new AMQP.BasicProperties
                        .Builder()
                        .correlationId(delivery.getProperties().getCorrelationId())
                        .build();

                String response = "";

                try {
                    CRUD crud = new CRUD();
                    String message = new String(delivery.getBody(), "UTF-8");
                    Gson gson = new GsonBuilder().create();
                    Request request = gson.fromJson(message, Request.class);
                    String type = request.getType();
                    if((type.compareTo("GET")==0)||(type.compareTo("get")==0)){
                        response = crud.getLocalPage(request.getURL(),request.getQuery());
                    }
                    else if((type.compareTo("POST")==0)||(type.compareTo("post")==0)){
                        response = crud.CreateLocalPage(request.getURL(),request.getQuery());
                    }
                    else if((type.compareTo("PUT")==0)||(type.compareTo("put")==0)){
                        response = crud.UpdateLocalPage(request.getURL(),request.getQuery());
                    }
                    else if((type.compareTo("DELETE")==0)||(type.compareTo("delete")==0)){
                        response = crud.DeleteLocalPage(request.getURL(),request.getQuery());
                    }
                    else{
                        response = "not a valid operation";
                    }
                    System.out.println(" [.] received request for " + request.getURL());
                } catch (RuntimeException e) {
                    System.out.println(" [.] " + e.toString());
                } finally {
                    channel.basicPublish("", delivery.getProperties().getReplyTo(), replyProps, response.getBytes("UTF-8"));
                    channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
                    // RabbitMq consumer worker thread notifies the RPC server owner thread
                    synchronized (monitor) {
                        monitor.notify();
                    }
                }
            };

            channel.basicConsume(RPC_QUEUE_NAME, false, deliverCallback, (consumerTag -> { }));
            // Wait and be prepared to consume the message from RPC client.
            while (true) {
                synchronized (monitor) {
                    try {
                        monitor.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}

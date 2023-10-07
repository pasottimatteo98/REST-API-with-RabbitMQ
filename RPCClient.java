import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeoutException;
import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.google.gson.*;

public class RPCClient implements AutoCloseable, HttpHandler{

    private Connection connection;
    private Channel channel;
    private String requestQueueName = "rpc_queue";
    private final static String HOST = "localhost";
    private final static String USER = "admin";
    private final static String PASSWD = "admin";

    public RPCClient() throws IOException, TimeoutException {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(HOST);
        factory.setUsername(USER);
        factory.setPassword(PASSWD);

        connection = factory.newConnection();
        channel = connection.createChannel();
    }

	@Override
	public void handle(HttpExchange exchange) throws IOException {
		URI requestURI = exchange.getRequestURI();
		String response;

		String requestMethod = exchange.getRequestMethod();
        List<String> strlist = new ArrayList<String>();
        strlist.add("text/json");
		String query = requestURI.getQuery();
        Request request = new Request(requestMethod,requestURI,query);
        Gson gson = new GsonBuilder().create();
        String Json = gson.toJson(request);
		try {
            response = call(Json);
            exchange.getResponseHeaders().put("content-type", strlist);
			exchange.sendResponseHeaders(200, response.getBytes().length);
			OutputStream os = exchange.getResponseBody();
			os.write(response.getBytes());
			os.close();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }


	}


    public String call(String message) throws IOException, InterruptedException {
        final String corrId = UUID.randomUUID().toString();
        String replyQueueName = channel.queueDeclare().getQueue();
        AMQP.BasicProperties props = new AMQP.BasicProperties
                .Builder()
                .correlationId(corrId)
                .replyTo(replyQueueName)
                .build();

        channel.basicPublish("", requestQueueName, props, message.getBytes("UTF-8"));

        final BlockingQueue<String> response = new ArrayBlockingQueue<>(1);

        String ctag = channel.basicConsume(replyQueueName, true, (consumerTag, delivery) -> {
            if (delivery.getProperties().getCorrelationId().equals(corrId)) {
                response.offer(new String(delivery.getBody(), "UTF-8"));
            }
        }, consumerTag -> {
        });

        String result = response.take();
        channel.basicCancel(ctag);
        return result;
    }


    public void close() throws IOException {
        connection.close();
    }
}

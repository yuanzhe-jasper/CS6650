package Servlet;

import com.google.gson.Gson;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.concurrent.TimeoutException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.Channel;

@WebServlet(urlPatterns = "/review/*")
public class FeedbackServlet extends HttpServlet {
  private final static String QUEUE_NAME = "reaction";
  private Gson gson;
  private ConnectionFactory rqFactory;
  private Connection connection;

  @Override
  public void init() throws ServletException {
    this.rqFactory = new ConnectionFactory();

    try {
      this.rqFactory.setUri("amqps://b-193a2bb4-3adb-4fe0-9e8c-b8489b51c326.mq.us-west-2.amazonaws.com:5671");
      this.rqFactory.setUsername("username");
      this.rqFactory.setPassword("lyz199991");
    } catch (URISyntaxException | NoSuchAlgorithmException | KeyManagementException e) {
      System.err.println(e.getMessage());
    }

    this.gson = new Gson();
    try{
      this.connection = rqFactory.newConnection();
    }catch (IOException | TimeoutException e){
      System.err.println(e.getMessage());
    }
  }

  @Override
  public void destroy() {
    super.destroy();
    try {
      if (this.connection != null && this.connection.isOpen()) {
        this.connection.close();
      }
    } catch (IOException e) {
      System.err.println(e.getMessage());
    }
  }

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    resp.setContentType("application/json");
    resp.setCharacterEncoding("UTF-8");
    String pathInfo = req.getPathInfo();
    PrintWriter out = resp.getWriter();
    if (pathInfo == null || pathInfo.isEmpty()) {
      out.print("Invalid Request");
      return;
    }
    String[] parts = pathInfo.substring(1).split("/");
    if (parts.length > 2) {
      out.print("Invalid Request");
      return;
    }
    HashMap<String, String> messageMap = new HashMap<>();
    messageMap.put("albumId", parts[1]);
    messageMap.put("reaction", parts[0]);
    publishToQueue(gson.toJson(messageMap));
    out.print("Added reaction");
  }

  private void publishToQueue(String message){
    try(Channel channel = this.connection.createChannel();){
      channel.queueDeclare(QUEUE_NAME, true, false, false, null);
      channel.basicPublish("", QUEUE_NAME, null, message.getBytes("UTF-8"));
    } catch (IOException | TimeoutException e) {
      System.out.println(e);
    }
  }

}

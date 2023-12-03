import com.google.gson.Gson;
import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Map;
import org.apache.commons.dbcp2.BasicDataSource;

public class MessageConsumer extends DefaultConsumer{

  private final String insert_sql = "INSERT INTO album_reactions (albumID, reaction) VALUES (?, ?)";

  private Gson gson = new Gson();

  public MessageConsumer(Channel channel) {
    super(channel);
  }
  @Override
  public void handleDelivery(String consumerTag, Envelope envelope, BasicProperties properties,
      byte[] body) throws IOException {
    String messageJson = new String(body, "UTF-8");
    Map<String, String> messageMap = gson.fromJson(messageJson, Map.class);
    String albumId = messageMap.get("albumId");
    String reaction = messageMap.get("reaction");
    saveToDB(albumId, reaction);
  }

  private void saveToDB(String albumID, String reaction){
    BasicDataSource dataSource = DatabaseConfig.getDataSource();
    try (Connection conn = dataSource.getConnection();
        PreparedStatement stmt = conn.prepareStatement(insert_sql)){
      stmt.setString(1, albumID);
      stmt.setString(2, reaction);
      stmt.executeUpdate();
    }catch(SQLException e){
      System.err.println("ERROR INSERTING TO Database:" + e.getMessage());
    }
  }
}

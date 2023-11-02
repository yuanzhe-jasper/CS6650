package Servlet;

import Entities.Album;
import Entities.ImageData;
import Entities.Profile;
import java.io.ByteArrayOutputStream;
import java.util.UUID;
import java.nio.charset.StandardCharsets;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.annotation.*;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import com.google.gson.Gson;

@WebServlet(name = "MusicServlet", value = "/MusicServlet")
@MultipartConfig(fileSizeThreshold = 1024 * 1024 * 10, maxFileSize = 1024 * 1024 * 20, maxRequestSize = 1024 * 1024 * 20)
public class MusicServlet extends HttpServlet {

  private HikariDataSource dataSource;

  @Override
  public void init() throws ServletException {
    super.init();
    String jdbcUrl = "jdbc:mysql://musiccopy.crfxivsgbiov.us-west-2.rds.amazonaws.com:3306/Music";
    //String jdbcUrl = "jdbc:mysql://localhost:3306/Music";
    String username = "root";
    String driverName = "com.mysql.cj.jdbc.Driver";
    String password = "lyz199991";
    //String password = "123456";
    HikariConfig config = new HikariConfig();
    config.setJdbcUrl(jdbcUrl);
    config.setUsername(username);
    config.setPassword(password);
    config.setMaximumPoolSize(10); // Maximum pool size
    config.setDriverClassName(driverName);
    dataSource = new HikariDataSource(config);
  }

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
    PrintWriter out = res.getWriter();
    res.setContentType("application/json");
    res.setCharacterEncoding("UTF-8");

    Gson gson = new Gson();
    String url = req.getPathInfo();
    if (url == null || url.isEmpty()) {
      res.setStatus(HttpServletResponse.SC_NOT_FOUND);
      return;
    }
    Connection conn = null;
    try {
      conn = dataSource.getConnection();
      String query = "SELECT Artist, Title, ReleaseYear FROM Albums WHERE AlbumId=1;";
      PreparedStatement statement = conn.prepareStatement(query);
      ResultSet results = statement.executeQuery();
      if(results.next()){
        String artist = results.getString("Artist");
        String title = results.getString("Title");
        String year = results.getString("ReleaseYear");
        Album album = Album.builder()
            .artist(artist)
            .title(title)
            .year(year)
            .build();
        res.setStatus(HttpServletResponse.SC_OK);
        out.print(gson.toJson(album));
      }
      out.flush();
      results.close();
      statement.close();
    } catch (SQLException e) {
      e.printStackTrace();
    } finally {
      try {
        conn.close();
      } catch (SQLException e) {
        e.printStackTrace();
      }
    }
  }


  @Override
  protected void doPost(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
      PrintWriter out = response.getWriter();
      response.setContentType("application/json");
      response.setCharacterEncoding("UTF-8");

      Gson gson = new Gson();
      response.setStatus(HttpServletResponse.SC_OK);
      // Get uploaded image
      Part image = request.getPart("image");
      long imageSize = image.getSize();
      String size = String.valueOf(imageSize);
      // Get profile
      Part profile = request.getPart("profile");
      InputStream profileInputStream = profile.getInputStream();
      InputStream imageInputStream = image.getInputStream();
      StringBuilder profileData = new StringBuilder();

      // Buffer for reading from the input stream
      byte[] buffer = new byte[1024], imageBuffer = new byte[1024];
      int bytesReadProfile, bytesReadImage;
      while ((bytesReadProfile = profileInputStream.read(buffer)) != -1) {
        String chunk = new String(buffer, 0, bytesReadProfile, StandardCharsets.UTF_8);
        profileData.append(chunk);
      }

      String profileString = profileData.toString();
      Profile curProfile = processString(profileString);

      ByteArrayOutputStream bufferStream = new ByteArrayOutputStream();
      while ((bytesReadImage = imageInputStream.read(imageBuffer, 0, imageBuffer.length)) != -1) {
        bufferStream.write(imageBuffer, 0, bytesReadImage);
      }

      bufferStream.flush();

      Connection conn = null;
      try {
        conn = dataSource.getConnection();
        String insertQuery = "INSERT INTO Albums (Artist, Title, ReleaseYear, imageSize, imageData) VALUES (?, ?, ?, ?, ?)";
        PreparedStatement statement = conn.prepareStatement(insertQuery,
            PreparedStatement.RETURN_GENERATED_KEYS);
        statement.setString(1, curProfile.getArtist());
        statement.setString(2, curProfile.getTitle());
        statement.setString(3, curProfile.getYear());
        statement.setString(4, size);
        statement.setBytes(5, bufferStream.toByteArray());
        statement.executeUpdate();
        statement.close();
      } catch (SQLException e) {
        e.printStackTrace();
      } finally {
        try {
          conn.close();
        } catch (SQLException e) {
          e.printStackTrace();
        }
      }

    try {
      UUID randomUUID = UUID.randomUUID();
      // Convert the UUID to a string
      String randomId = randomUUID.toString();
      ImageData imageMetaData = ImageData.builder()
          .albumID(randomId)
          .imageSize(String.valueOf(imageSize))
          .build();
      out.print(gson.toJson(imageMetaData));
      out.flush();
    } catch (Exception e) {
      out.print("Json Syntax Exception");
    }
  }

  private Profile processString(String profileString){
      int artistIndex = profileString.indexOf("artist:");
      int titleIndex = profileString.indexOf("title:");
      int yearIndex = profileString.indexOf("year:");
      int endIndex = profileString.indexOf("}");

      String artist = profileString.substring(artistIndex + 7, titleIndex);
      String title = profileString.substring(titleIndex + 6, yearIndex);
      String year = profileString.substring(yearIndex + 5, endIndex);
      return Profile.builder()
          .artist(artist)
          .title(title)
          .year(year)
          .build();
  }
}


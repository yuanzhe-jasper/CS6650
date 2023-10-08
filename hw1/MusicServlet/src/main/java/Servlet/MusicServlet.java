package Servlet;

import Entities.Album;
import Entities.ImageData;
import java.util.UUID;
import java.nio.charset.StandardCharsets;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.annotation.*;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;

import com.google.gson.Gson;

@WebServlet(name = "MusicServlet", value = "/MusicServlet")
@MultipartConfig(fileSizeThreshold = 1024 * 1024 * 10, maxFileSize = 1024 * 1024 * 20, maxRequestSize = 1024 * 1024 * 20)
public class MusicServlet extends HttpServlet {

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
    Album album = Album.builder()
        .artist("Jack")
        .title("singer")
        .year("2000")
        .build();
    res.setStatus(HttpServletResponse.SC_OK);
    out.print(gson.toJson(album));
    out.flush();
  }


  @Override
  protected void doPost(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    PrintWriter out = response.getWriter();
    response.setContentType("application/json");
    response.setCharacterEncoding("UTF-8");

    Gson gson = new Gson();
    String url = request.getPathInfo();


      response.setStatus(HttpServletResponse.SC_OK);

      // Get uploaded image
      Part image = request.getPart("image");
      long imageSize = image.getSize();

      // Get profile
      Part profile = request.getPart("profile");
      InputStream profileInputStream = profile.getInputStream();
      StringBuilder profileData = new StringBuilder();

      // Buffer for reading from the input stream
      byte[] buffer = new byte[1024];
      int bytesRead;
      while ((bytesRead = profileInputStream.read(buffer)) != -1) {
        String chunk = new String(buffer, 0, bytesRead, StandardCharsets.UTF_8);
        profileData.append(chunk);
      }

      String profileString = profileData.toString();

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
}


import io.swagger.client.ApiClient;
import io.swagger.client.ApiException;
import io.swagger.client.ApiResponse;
import io.swagger.client.api.LikeApi;
import io.swagger.client.api.DefaultApi;
import io.swagger.client.model.AlbumInfo;
import io.swagger.client.model.AlbumsProfile;
import io.swagger.client.model.ImageMetaData;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import org.apache.commons.csv.CSVPrinter;


public class SingleThread implements Runnable{

  private final int MAX_RETRY = 5;
  private final String FILE_PATH = "/Users/apple/Desktop/Course/6650/hw1/CS6650-A1-Client2/src/main/java/images/example.png";
  private final String POST_REQUEST = "POST";
  private final String GET_REQUEST = "GET";
  private final String LIKE = "like";
  private final String DISLIKE  = "dislike";

  private String IP;
  private Integer requestRounds;

  private CSVPrinter csvFileWriter;
  private CountDownLatch countDownLatch;
  private RequestCounter requestCounter;

  public SingleThread(RequestCounter requestCounter,
      Integer requestRounds, String ipAddress, CSVPrinter csvFileWriter, CountDownLatch countDownLatch) {
    this.requestCounter = requestCounter;
    this.requestRounds = requestRounds;
    this.IP = ipAddress;
    this.csvFileWriter = csvFileWriter;
    this.countDownLatch = countDownLatch;
  }

  @Override
  public void run(){
    ApiClient client = new ApiClient();
    DefaultApi apiObj = new DefaultApi();
    LikeApi likeApiObj = new LikeApi();
    apiObj.getApiClient().setBasePath(this.IP);
    likeApiObj.getApiClient().setBasePath(this.IP);
    File imageFile = new File(FILE_PATH);

    for (int i = 0; i < requestRounds; i++) {
      try {
        String id = SendPostRequest(apiObj, imageFile);
        if(!id.isEmpty()){
          SendFeedbackRequest(likeApiObj, id, LIKE);
          SendFeedbackRequest(likeApiObj, id, LIKE);
          SendFeedbackRequest(likeApiObj, id, DISLIKE);
        }

      } catch (Exception e) {
        e.printStackTrace();
      }
    }
    this.countDownLatch.countDown();
  }

  private String SendPostRequest(final DefaultApi apiObj, final File imageFile) {
    int maxTryTimes = MAX_RETRY;
    long startTime = System.currentTimeMillis();
    String id = "";
    while (maxTryTimes > 0) {
      try {
        AlbumsProfile albumsProfile = new AlbumsProfile();
        albumsProfile.setArtist("Jack");
        albumsProfile.setTitle("New Moon");
        albumsProfile.setYear("2024");
        ApiResponse<ImageMetaData> response = apiObj.newAlbumWithHttpInfo(
            imageFile, albumsProfile);
        id = response.getData().getAlbumID();

        if (response.getStatusCode() >= 200 && response.getStatusCode() < 300) {
          requestCounter.increaseSuccess();
          long endTime = System.currentTimeMillis();
          long latency = endTime - startTime;
          RecordInfo recordInfo = new RecordInfo(startTime, POST_REQUEST, latency, response.getStatusCode());
          synchronized("") {
            csvFileWriter.printRecord(recordInfo.getStartTime(), recordInfo.getRequestType(),
                recordInfo.getLatency(),
                recordInfo.getStatusCode());
          }
          return id;
        }else{
          requestCounter.increaseFailure();
        }
        maxTryTimes--;
      } catch (ApiException e) {
        maxTryTimes--;
        e.printStackTrace();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
    return "";
  }

  private void SendFeedbackRequest(LikeApi api, String albumID, String reaction) {
    int maxTryTimes = MAX_RETRY;
    long startTime = System.currentTimeMillis();
    while (maxTryTimes > 0) {
      try {
        ApiResponse<Void> response = api.reviewWithHttpInfo(reaction, albumID);
        if (response.getStatusCode() >= 200 && response.getStatusCode() < 300) {
          requestCounter.increaseSuccess();
          long endTime = System.currentTimeMillis();
          long latency = endTime - startTime;
          RecordInfo recordInfo = new RecordInfo(startTime, POST_REQUEST, latency, response.getStatusCode());
          synchronized("") {
            csvFileWriter.printRecord(recordInfo.getStartTime(), recordInfo.getRequestType(),
                recordInfo.getLatency(),
                recordInfo.getStatusCode());
          }
          return;
        }else{
          requestCounter.increaseFailure();
        }
        maxTryTimes--;
      } catch (ApiException e) {
        maxTryTimes--;
        e.printStackTrace();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }

  private void SendGetRequest(final DefaultApi apiObj)  {
    int maxTryTimes = MAX_RETRY;
    while (maxTryTimes > 0) {
      try {
        long startTime = System.currentTimeMillis();
        ApiResponse<AlbumInfo> response = apiObj.getAlbumByKeyWithHttpInfo("1");

        if (response.getStatusCode() >= 200 && response.getStatusCode() < 300) {
          requestCounter.increaseSuccess();
          long endTime = System.currentTimeMillis();
          long latency = endTime - startTime;
          RecordInfo recordInfo = new RecordInfo(startTime, GET_REQUEST, latency, response.getStatusCode());
          synchronized("") {
            csvFileWriter.printRecord(recordInfo.getStartTime(), recordInfo.getRequestType(),
                recordInfo.getLatency(),
                recordInfo.getStatusCode());
          }
          return;
        }else{
          requestCounter.increaseFailure();
        }
        maxTryTimes--;
      } catch (Exception e) {
        maxTryTimes--;
        e.printStackTrace();
      }
    }
  }
}

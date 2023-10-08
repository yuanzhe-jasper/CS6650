import io.swagger.client.ApiException;
import io.swagger.client.ApiResponse;
import io.swagger.client.api.DefaultApi;
import io.swagger.client.model.AlbumInfo;
import io.swagger.client.model.AlbumsProfile;
import io.swagger.client.model.ImageMetaData;
import java.io.File;
import java.util.concurrent.CountDownLatch;


public class SingleThread implements Runnable{

  private final int MAX_RETRY = 5;
  private final String FILE_PATH = "/Users/apple/Desktop/Course/6650/hw1/CS6650-A1-Client1/src/main/java/file/example.png";

  private String ipAddress;
  private Integer count;

  private CountDownLatch countDownLatch;

  public SingleThread(
      Integer count, String ipAddress, CountDownLatch countDownLatch) {
    this.count = count;
    this.ipAddress = ipAddress;
    this.countDownLatch = countDownLatch;
  }

  @Override
  public void run(){
    DefaultApi apiObj = new DefaultApi();
    apiObj.getApiClient().setBasePath(this.ipAddress);
    File imageFile = new File(FILE_PATH);

    for (int i = 0; i < count; i++) {
      try {
        SendPostRequest(apiObj, imageFile);
        SendGetRequest(apiObj);
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
    this.countDownLatch.countDown();
  }

  private void SendPostRequest(final DefaultApi apiObj, final File imageFile) {
    int maxTryTimes = MAX_RETRY;
    while (maxTryTimes > 0) {
      try {
        ApiResponse<ImageMetaData> response = apiObj.newAlbumWithHttpInfo(
            imageFile, new AlbumsProfile());
        if (response.getStatusCode() >= 200 && response.getStatusCode() < 300) {
          return;
        }
        maxTryTimes--;
      } catch (ApiException e) {
        maxTryTimes--;
        e.printStackTrace();
      }
    }
  }

  private void SendGetRequest(final DefaultApi apiObj) {
    int maxTryTimes = MAX_RETRY;
    while (maxTryTimes > 0) {
      try {
        ApiResponse<AlbumInfo> res = apiObj.getAlbumByKeyWithHttpInfo("1");
        if (res.getStatusCode() >= 200 && res.getStatusCode() < 300) {
          return;
        }
        maxTryTimes--;
      } catch (Exception e) {
        maxTryTimes--;
        e.printStackTrace();
      }
    }
  }
}

import io.swagger.client.ApiException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;

public class MultiThreadsProcessor {

  private final String REQUEST_CSV_PATH = "/Users/apple/Desktop/Course/6650/hw1/CS6650-A1-Client2/src/main/java/records/requestInfo.csv";
  private final String THROUGHPUT_CSV_PATH = "/Users/apple/Desktop/Course/6650/hw1/CS6650-A1-Client2/src/main/java/records/throughput.csv";

  private Integer threadGroupSize;
  private Integer numThreadGroups;
  private Integer delay;
  private String IP;

  private FileWriter fileWriter;
  private CSVPrinter csvFileWriter;

  public MultiThreadsProcessor(Integer threadGroupSize, Integer numThreadGroups,
      Integer delay, String IP) {
    this.threadGroupSize = threadGroupSize;
    this.numThreadGroups = numThreadGroups;
    this.delay = delay;
    this.IP = IP;
  }

  public void execute() throws InterruptedException, IOException {
    CountDownLatch countDownLatch = new CountDownLatch(10);

    try {
      fileWriter = new FileWriter(REQUEST_CSV_PATH, false);
      csvFileWriter = new CSVPrinter(fileWriter, CSVFormat.DEFAULT);
      csvFileWriter.printRecord("StartTime", "RequestType", "Latency", "ResponseCode");
    } catch (IOException e) {
      e.printStackTrace();
      return;
    }

    for (int i = 0; i < 10; i++) {
      SingleThread singleThread = new SingleThread(100, this.IP, csvFileWriter, countDownLatch);
      Thread thread = new Thread(singleThread);
      thread.start();
    }

    //Wait until first ten threads finish tasks
    countDownLatch.await();

    //Start the group threads tasks
    System.out.println("PROCESS START");
    long startTime = System.currentTimeMillis();
    countDownLatch = new CountDownLatch(this.threadGroupSize * this.numThreadGroups);
    for (int i = 0; i < this.numThreadGroups; i++) {
      for (int j = 0; j < this.threadGroupSize; j++) {
        SingleThread singleThread = new SingleThread(1000, this.IP, csvFileWriter, countDownLatch);
        Thread thread = new Thread(singleThread);
        thread.start();
      }
      //wait
      int waitTime = delay * 1000;
      Thread.sleep(waitTime);
    }
    countDownLatch.await();

    long endTime = System.currentTimeMillis();
    double totalTime = (endTime - startTime) / 1000d;

    //Start to read the CSV to retrieve data and calculate statistics
    List<RecordInfo> records = new ArrayList<>();
    try (Reader reader = new FileReader(REQUEST_CSV_PATH);
        CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT.withFirstRecordAsHeader())) {
      for (CSVRecord record : csvParser) {
        if (record.size() < 4) continue;
        long start = Long.parseLong(record.get(0));
        String requestType = record.get(1);
        long latency = Long.parseLong(record.get(2));
        int statusCode = Integer.parseInt(record.get(3));
        records.add(new RecordInfo(start, requestType, latency, statusCode));
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    //Find the mean, median, min and max Latency
    Collections.sort(records, (a, b) -> (int)(a.getLatency() - b.getLatency()));
    long minLatency = Long.MAX_VALUE, maxLatency = Long.MIN_VALUE, meanLatency = -1,
        medianLatency = -1, p99Latency = -1;
    int recordSize = records.size();

    // get statistics
    if (recordSize > 0) {
      long sumLatency = 0;
      for(RecordInfo record : records){
        minLatency = Math.min(minLatency, (long)record.getLatency());
        maxLatency = Math.max(maxLatency, (long)record.getLatency());
        sumLatency += (long) record.getLatency();
      }
      p99Latency = records.get(recordSize / 100 * 99).getLatency();
      medianLatency = records.get(recordSize / 2).getLatency();
      meanLatency = sumLatency / recordSize;
    }

    // Calculate throughput per second
    Collections.sort(records, (a, b) -> (int)(a.getStartTime() - b.getStartTime()));
    try {
      fileWriter = new FileWriter(THROUGHPUT_CSV_PATH, false);
      csvFileWriter = new CSVPrinter(fileWriter, CSVFormat.DEFAULT);
      //Start to write into csv file
      csvFileWriter.printRecord("Timeline(Seconds)", "Throughput");
      long startTimeOfCurInterval = records.get(0).getStartTime(), throughput = 0;
      int seconds = 1;

      for (RecordInfo record : records) {
        long curRequestStartTime = record.getStartTime();
        if (curRequestStartTime < startTime) {
          startTimeOfCurInterval = curRequestStartTime;
          continue;
        }
        if (curRequestStartTime - startTimeOfCurInterval < 1000) {
          throughput++;
        } else {
          csvFileWriter.printRecord(seconds, throughput);
          seconds++;
          throughput = 1;
          startTimeOfCurInterval = curRequestStartTime;
        }
      }
    } catch (IOException e) {
      e.printStackTrace();
      return;
    }

    csvFileWriter.close();

    System.out.println("Wall Time: " + totalTime + " seconds");
    System.out.println("Throughput: " + ((long)this.numThreadGroups * this.threadGroupSize * 1000) / totalTime);
    System.out.println("Mean Latency: " + meanLatency + " ms");
    System.out.println("Median Latency: " + medianLatency + " ms");
    System.out.println("P99 Latency: " + p99Latency + " ms");
    System.out.println("Minimum Latency: " + minLatency + " ms");
    System.out.println("Maximum Latency: " + maxLatency + " ms");
    System.out.println("END");
  }

  public static void main(String[] args) throws IOException, InterruptedException {
    // java Local
//    MultiThreadsProcessor processor = new MultiThreadsProcessor(10, 30,
//        2, "http://localhost:8080/MusicServlet_war_exploded");
//    processor.execute();

    //Go local
//  MultiThreadsProcessor processor = new MultiThreadsProcessor(10, 30,
//        2, "http://localhost:8080");
//    processor.execute();

    //java ec2
//    MultiThreadsProcessor processor = new MultiThreadsProcessor(10, 30,
//        2, "http://52.89.195.203:8080/MusicServlet_war");
//    processor.execute();

    //go ec2
//    MultiThreadsProcessor processor = new MultiThreadsProcessor(10, 30,
//        2, "http://52.89.195.203:8080");
//    processor.execute();
    }
}
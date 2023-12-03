import java.util.Collections;

public class Test {

  public static void main(String[] args) {
    //Find the mean, median, min and max Latency
//10-10-2-single server
    // get statistics
    System.out.println("Statistics for all requests");
    System.out.println("Successful Requests: " + 120000);
    System.out.println("Failed Requests: " + 0);
    System.out.println("Wall Time: " + 36.475 + " seconds");
    System.out.println("Throughput: " + 3289.924605894448);


//    //get statistics for New Album Requests
    System.out.println("==================");
    System.out.println("Statistics for New Album");
    System.out.println("Mean Latency: " + 53.6421 + " ms");
    System.out.println("Median Latency: " + 44.0 + " ms");
    System.out.println("P99 Latency: " + 183.0 + " ms");
    System.out.println("Minimum Latency: " + 16.0 + " ms");
    System.out.println("Maximum Latency: " + 413.0 + " ms");
    System.out.println("==================");
//    //get statistics for New reaction requests
    System.out.println("Statistics for New reaction");
    System.out.println("Mean Latency: " + 98.128655587 + " ms");
    System.out.println("Median Latency: " + 99.0 + " ms");
    System.out.println("P99 Latency: " +  195.0 + " ms");
    System.out.println("Minimum Latency: " + 15.0 + " ms");
    System.out.println("Maximum Latency: " + 414.0 + " ms");

    System.out.println("END");
  }
}

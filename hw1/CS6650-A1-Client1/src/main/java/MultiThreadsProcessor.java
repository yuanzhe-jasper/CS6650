import java.util.concurrent.CountDownLatch;


public class MultiThreadsProcessor {

  private Integer threadGroupSize;
  private Integer numThreadGroups;
  private Integer delay;
  private String IP;

  public MultiThreadsProcessor(Integer threadGroupSize, Integer numThreadGroups,
      Integer delay, String IP) {
    this.threadGroupSize = threadGroupSize;
    this.numThreadGroups = numThreadGroups;
    this.delay = delay;
    this.IP = IP;
  }

  public void execute() throws InterruptedException {
    CountDownLatch countDownLatch = new CountDownLatch(10);
    for (int i = 0; i < 10; i++) {
      SingleThread singleThread = new SingleThread(100, this.IP,
          countDownLatch);
      Thread thread = new Thread(singleThread);
      thread.start();
    }

    //Wait until first ten threads finish tasks
    countDownLatch.await();

    //Start the group threads tasks
    System.out.println("START");
    long startTime = System.currentTimeMillis();
    countDownLatch = new CountDownLatch(this.threadGroupSize * this.numThreadGroups);
    for (int i = 0; i < this.numThreadGroups; i++) {
      for (int j = 0; j < this.threadGroupSize; j++) {
        SingleThread singleThread = new SingleThread(1000, this.IP,
            countDownLatch);
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

    System.out.println("Wall Time: " + totalTime + "seconds");
    System.out.println("Throughput: " +
        ((long)this.numThreadGroups * this.threadGroupSize * 1000) / totalTime);
  }


  public static void main(String[] args) throws InterruptedException {
    // java ec2
//    MultiThreadsProcessor processor = new MultiThreadsProcessor(10, 30,
//        2, "http://52.89.195.203:8080/MusicServlet_war");
//    processor.execute();

  }
}
public class RequestCounter {
    private static int numOfSuccess = 0;
    private static int numOfFailure = 0;

    public synchronized void increaseSuccess() {
      numOfSuccess++;
    }

    public synchronized void increaseFailure() {
      numOfFailure++;
    }

    public synchronized int getNumOfSuccess() {
      return numOfSuccess;
    }

    public synchronized int getNumOfFailure() {
      return numOfFailure;
    }
}

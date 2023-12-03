import org.apache.commons.dbcp2.BasicDataSource;


public class DatabaseConfig {
  private static BasicDataSource dataSource = new BasicDataSource();

  private static void init() {
    dataSource.setDriverClassName("com.mysql.cj.jdbc.Driver");
    dataSource.setUrl("jdbc:mysql://musiccopy.crfxivsgbiov.us-west-2.rds.amazonaws.com:3306/Music");
    dataSource.setUsername("root");
    dataSource.setPassword("lyz199991");

    dataSource.setInitialSize(15);
    dataSource.setMaxTotal(45);
    dataSource.setMaxIdle(30);
    dataSource.setMinIdle(15);
    dataSource.setMaxWaitMillis(30000);
  }

  public static BasicDataSource getDataSource(){
    return dataSource;
  }
}

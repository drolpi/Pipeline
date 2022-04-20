package de.natrox.pipeline.test;

import de.natrox.pipeline.mysql.MySqlConfig;
import de.natrox.pipeline.mysql.MySqlEndpoint;
import de.natrox.pipeline.mysql.MySqlProvider;

public class MySqlTest {

    public static void main(String[] args) throws Exception{
        MySqlConfig mySqlConfig = MySqlConfig
            .builder()
            .endpoints(
                MySqlEndpoint
                    .builder()
                    .host("0.0.0.0")
                    .port(3306)
                    .database("database")
                    .useSsl(true)
                    .build(),
                MySqlEndpoint
                    .builder()
                    .host("0.0.0.1")
                    .port(3306)
                    .database("database")
                    .useSsl(true)
                    .build()
            )
            .username("username")
            .password("password")
            .build();
        MySqlProvider mySqlProvider = mySqlConfig.createProvider();
    }

}

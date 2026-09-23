/*
 UniExchangeApplication.java

 Spring Boot entry point. Sits at the base package root (za.ac.cput) so that
 component scan, entity scan and Spring Data repository scan all root here and
 pick up domain/, repository/, service/, controller/, security/ and config/
 automatically - no scanBasePackages, @EntityScan or @EnableJpaRepositories needed.

 Author: Mogamat Yaseen Kannemeyer 240453182
 Date: 04 September 2026
*/

package za.ac.cput;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class UniExchangeApplication {

    public static void main(String[] args) {
        SpringApplication.run(UniExchangeApplication.class, args);
    }

}

package top.wjc.bookmallbackend;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@MapperScan("top.wjc.bookmallbackend.mapper")
@SpringBootApplication
public class BookMallBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(BookMallBackendApplication.class, args);
    }

}

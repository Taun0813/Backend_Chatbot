package vn.tt.practice.warrantyservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class WarrantyServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(WarrantyServiceApplication.class, args);
    }

}

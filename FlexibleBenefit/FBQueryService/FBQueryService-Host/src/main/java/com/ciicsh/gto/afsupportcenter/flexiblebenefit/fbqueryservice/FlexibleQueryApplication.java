package com.ciicsh.gto.afsupportcenter.flexiblebenefit.fbqueryservice;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.support.SpringBootServletInitializer;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * @author xwz
 * @SpringBootApplication(scanBasePackages = {"com.ciicsh.gto.afsupportcenter.flexiblebenefit.fbqueryservice"})
 */
@SpringBootApplication
@EnableDiscoveryClient
@MapperScan("com.ciicsh.gto.afsupportcenter.flexiblebenefit.fbqueryservice.dao")
public class FlexibleQueryApplication extends SpringBootServletInitializer {

    public static void main(String[] args) {
        SpringApplication.run(FlexibleQueryApplication.class);
    }
}
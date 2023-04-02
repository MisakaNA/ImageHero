package com.project.imgcrawler;

import com.project.imgcrawler.services.PixivDownloadServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import javax.sql.DataSource;


@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class })
public class ImgcrawlerApplication {

    public static void main(String[] args) {
        SpringApplication.run(ImgcrawlerApplication.class, args);
    }

    @Bean
    public DataSource dataSource() {
        return DataSourceBuilder
                .create()
                .driverClassName("org.postgresql.Driver")
                .url("jdbc:postgresql://192.168.4.90:5432/mypixivartworks")
                .username("postgres")
                .password("1234567890")
                .build();
    }

    @Bean
    @Autowired
    public JdbcTemplate jdbcTemplate(DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }
}

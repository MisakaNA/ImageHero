package com.project.imgcrawler;

import com.project.imgcrawler.services.PixivDownloadServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import javax.sql.DataSource;


@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class })
public class ImgcrawlerApplication {

    public static void main(String[] args) {
        SpringApplication.run(ImgcrawlerApplication.class, args);
    }

    @Bean(name = "artworks")
    @Qualifier("artworksDataSource")
    @ConfigurationProperties("artworks.datasource")
    public DataSource artworksDataSource() {
        return DataSourceBuilder
                .create()
                .driverClassName("org.postgresql.Driver")
                .url("jdbc:postgresql://192.168.4.90:5432/mypixivartworks")
                .username("postgres")
                .password("1234567890")
                .build();
    }

    @Bean(name = "accounts")
    @Qualifier("accountsDataSource")
    @ConfigurationProperties(prefix="accounts.datasource")
    public DataSource accountsDataSource() {
        return DataSourceBuilder
                .create()
                .driverClassName("org.postgresql.Driver")
                .url("jdbc:postgresql://192.168.4.90:5432/imagehero_accounts")
                .username("postgres")
                .password("1234567890")
                .build();
    }

    @Bean
    @Autowired
    public JdbcTemplate artworksJdbcTemplate(@Qualifier("artworksDataSource") DataSource artworksDataSource) {
        return new JdbcTemplate(artworksDataSource);
    }

    @Bean
    @Autowired
    public JdbcTemplate accountsJdbcTemplate(@Qualifier("accountsDataSource") DataSource accountsDataSource) {
        return new JdbcTemplate(accountsDataSource);
    }
}

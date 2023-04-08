package com.project.imgcrawler.controller;

import com.project.imgcrawler.services.Account;
import com.project.imgcrawler.services.AccountResponse;
import com.project.imgcrawler.services.PixivImage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

@Controller
@CrossOrigin
@ResponseBody
@RequestMapping("/account")
public class AccountController {

    @Autowired
    JdbcTemplate accountsJdbcTemplate;

    @Autowired
    JdbcTemplate artworksJdbcTemplate;

    @PostMapping("/login")
    public Account login(@RequestBody Map<String, String> accountJson) {
        String uname = accountJson.get("uname");
        String password = accountJson.get("password");
        AccountResponse response;
        if(uname == null || password == null || uname.equals("") || password.equals("")) {
            return new AccountResponse(true, "Invalid Login Credentials!");
        }

        String queryString = "SELECT uname, password, time_join FROM accounts WHERE uname = ? AND password = ?";
        try {
            accountsJdbcTemplate.queryForObject(queryString, rowMapper(), uname, password);
            response = new AccountResponse(false, "");
            response.setUname(uname);
        } catch (EmptyResultDataAccessException e) {
            response = new AccountResponse(true, "Account '" + uname + "' does not exist or password is incorrect");
        }

        return response;
    }

    @PostMapping("/register")
    public AccountResponse register(@RequestBody Map<String, String> accountJson) {
        String uname = accountJson.get("uname");
        String password = accountJson.get("password");
        AccountResponse response;
        if(uname == null || password == null || uname.equals("") || password.equals("")) {
            return new AccountResponse(true, "Invalid Login Credentials!");
        }

        String queryString = "SELECT uname, password, time_join FROM accounts WHERE uname = ?";
        try {
            accountsJdbcTemplate.queryForObject(queryString, rowMapper(), uname);
            response = new AccountResponse(true, "Account '" + uname + "' exists!");
        } catch (EmptyResultDataAccessException e) {
            String insertString = "INSERT INTO accounts(uname, password, time_join) VALUES (?, ?, ?)";
            if(accountsJdbcTemplate.update(insertString, uname, password, LocalDateTime.now().withNano(0)) == 1){

                try {
                    String createTable = "CREATE TABLE " + uname + "_favorite_artworks (" +
                            "PID NUMERIC PRIMARY KEY, " +
                            "TITLE VARCHAR NOT NULL, " +
                            "AUTHOR VARCHAR NOT NULL, " +
                            "IMAGE_FORMAT TEXT NOT NULL, " +
                            "IMAGE_BASE64_STRING VARCHAR NOT NULL, " +
                            "DOWNLOAD_TIME TIMESTAMP NOT NULL, " +
                            "IMAGE_URL VARCHAR NOT NULL)";
                    artworksJdbcTemplate.update(createTable);
                    response = new AccountResponse(false, "");
                    response.setUname(uname);
                } catch (Exception e1) {
                    response = new AccountResponse(true, e.getMessage());
                }

            } else {
                response = new AccountResponse(true, "Unable to create account");
            }
        }
        return response;
    }

    private RowMapper<Account> rowMapper() {
        return ((rs, rowNum) ->
                new Account (
                        rs.getString("uname"),
                        rs.getString("password"),
                        rs.getTimestamp("time_join").toLocalDateTime()

                ));
    }

}

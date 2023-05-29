package com.kolya.tecs.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("test")
public class TestController {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @GetMapping
    @ResponseBody
    public byte[] test(){
        byte[] b = getData();
        System.out.println(b);
        return b;
    }

    private byte[] getData() {
        byte[] result = jdbcTemplate.queryForObject(
                "SELECT attribute_bytes FROM spring_session_attributes where attribute_name = 'org.springframework.security.web.csrf.HttpSessionCsrfTokenRepository.CSRF_TOKEN'", (rs, rowNum) -> rs.getBytes(1));
        return result;
    }
}

package com.redis.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.configurationprocessor.json.JSONObject;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
public class RedisController {
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private JdbcTemplate jdbcTemplate;
    @GetMapping("/tssj")
    public String tssj(String serial){
        setDataBase(1);
        for (int i = 0 ;i<100;i++){
            serial = serial.substring(0,6)+String.valueOf(i);
            jdbcTemplate.update("insert into payment (id,serial) values(null,?)",serial);
            List<Map<String, Object>> maps = jdbcTemplate.queryForList("select * from payment where serial = ?",serial);
            redisTemplate.opsForValue().set(serial, JSON.toJSONString(maps));
        }
        return "推送成功";
    }

    private void setDataBase(int num) {
        LettuceConnectionFactory connectionFactory = (LettuceConnectionFactory) redisTemplate.getConnectionFactory();
        if (connectionFactory != null && num != connectionFactory.getDatabase()) {
            connectionFactory.setDatabase(num);
            this.redisTemplate.setConnectionFactory(connectionFactory);
            connectionFactory.resetConnection();
        }
    }

}

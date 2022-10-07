package com.hacktool.controller;

import com.hacktool.pojo.User;
import com.hacktool.service.UserService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

@RestController()
@RequestMapping("/user")
public class UserController {

    @Resource
    private UserService userService;

    @GetMapping("/all")
    public List<User> all(){
        return userService.list();
    }
}

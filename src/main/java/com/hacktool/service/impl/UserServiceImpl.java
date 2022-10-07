package com.hacktool.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hacktool.mapper.UserMapper;
import com.hacktool.pojo.User;
import com.hacktool.service.UserService;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {
}

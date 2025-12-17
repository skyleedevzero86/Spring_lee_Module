package com.sleekydz86.sever.model.application.service;

import com.sleekydz86.sever.model.domain.User;
import com.sleekydz86.sever.model.infrastructure.persistence.UserMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
public class UserService {

    private final UserMapper userMapper;

    public UserService(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    public User findByUsername(String username) {
        return userMapper.findByUsername(username);
    }

    @Transactional
    public void register(String username, String password, String authority) {
        userMapper.executeUserProcedure("C", null, username, password, true, authority);
    }

    @Transactional
    public void updateUser(Long id, String username, String password, Boolean enabled, String authority) {
        userMapper.executeUserProcedure("U", id, username, password, enabled, authority);
    }

    @Transactional
    public void deleteUser(Long id) {
        userMapper.executeUserProcedure("D", id, null, null, null, null);
    }

    public User getUserById(Long id) {
        return userMapper.findUserById(id);
    }

    public User getUserByUsername(String username) {
        return userMapper.findUserByIdOrUsername(null, username);
    }

    public List<Map<String, Object>> getUserList() {
        return userMapper.findAllUsersFromView();
    }

    public List<Map<String, Object>> searchUsers(String keyword) {
        return userMapper.searchUsersFromView(keyword);
    }
}

package com.baomidou.springboot.service.impl;

import org.springframework.stereotype.Service;

import com.baomidou.springboot.entity.User;
import com.baomidou.springboot.mapper.UserMapper;
import com.baomidou.springboot.service.BaseServiceImpl;
import com.baomidou.springboot.service.IUserService;

/**
 * <p>
 * 사용자 테이블 서비스 구현 클래스
 * </p>
 *
 * @author CuiCan
 * @since 2017-06-10
 */
@Service
public class UserServiceImpl extends BaseServiceImpl<UserMapper, User> implements IUserService {

}

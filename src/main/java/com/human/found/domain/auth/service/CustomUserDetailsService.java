package com.human.found.domain.auth.service;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.human.found.domain.user.mapper.UserMapper;
import com.human.found.domain.user.vo.UserVO;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService{
    
    private final UserMapper userMapper;

    @Override
    public UserDetails loadUserByUsername(String id) throws UsernameNotFoundException {
        
        UserVO user = userMapper.findById(id);

        if(user == null) {
            throw new UsernameNotFoundException("회원없음");
        }

        return User.builder()
                .username(user.getId())
                .password(user.getPw())
                .roles(user.getRole())
                .build();
    }


    
}


package com.kolya.tecs.service;

import com.kolya.tecs.model.Role;
import com.kolya.tecs.model.User;
import com.kolya.tecs.repos.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
public class UserService implements UserDetailsService {

    @Autowired
    private UserRepo userRepo;
    @Autowired
    private MailService mailService;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepo.findByEmail(email);
        if (user==null){
            throw new UsernameNotFoundException("User doesn't exist!");
        }
        return user;
    }


    public boolean addUser(User user) {
        user.setRoles(Collections.singleton(Role.USER));
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        if (userRepo.findByEmail(user.getEmail())!=null){
            return false;
        }
        userRepo.save(user);
/*
        if (!Strings.isEmpty(user.getEmail())){
            String message = String.format("Hello, %s!",user.getUsername());
            mailService.send(user.getEmail(),"Hello!", message);
        }
*/
        return true;
    }

    public void save(User user) {
        userRepo.save(user);
    }
}

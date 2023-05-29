package com.kolya.tecs.repos;

import com.kolya.tecs.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepo extends JpaRepository<User,Long> {
    User findByEmail(String email);

    User findUserById(Long id);
}

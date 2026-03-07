package com.microdevice.supervisor.repository;

import com.microdevice.supervisor.model.User;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class UserRepository {

    private final Map<String, User> users = new ConcurrentHashMap<>();

    public UserRepository(PasswordEncoder passwordEncoder) {
        User admin = new User(
            UUID.randomUUID().toString(),
            "admin",
            passwordEncoder.encode("admin123"),
            List.of("ADMIN", "USER")
        );
        users.put(admin.getUsername(), admin);
    }

    public Optional<User> findByUsername(String username) {
        return Optional.ofNullable(users.get(username));
    }

    public User save(User user) {
        users.put(user.getUsername(), user);
        return user;
    }

    public List<User> findAll() {
        return new ArrayList<>(users.values());
    }
}

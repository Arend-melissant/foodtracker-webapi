package com.foodtracker.application.port.outbound;

import com.foodtracker.domain.model.User;
import java.util.Optional;

public interface UserRepositoryPort {
    Optional<User> findById(String id);
    Optional<User> findByGoogleSubjectId(String googleSubjectId);
    Optional<User> findByEmail(String email);
    User save(User user);
}


package com.heladeria.tpv.repository;

import com.heladeria.tpv.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
}

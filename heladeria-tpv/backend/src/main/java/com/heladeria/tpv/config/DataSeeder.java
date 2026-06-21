package com.heladeria.tpv.config;

import com.heladeria.tpv.model.Role;
import com.heladeria.tpv.model.User;
import com.heladeria.tpv.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * La app no tiene pantalla de login. Se usa un unico usuario de sistema
 * fijo para registrar ventas y turnos, ya que la base de datos sigue
 * necesitando un user_id en esas tablas.
 */
@Component
public class DataSeeder implements CommandLineRunner {

    public static final Long SYSTEM_USER_ID = 1L;

    private final UserRepository userRepository;

    public DataSeeder(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public void run(String... args) {
        if (userRepository.count() == 0) {
            User systemUser = new User("Heladeria", "SYSTEM", Role.ADMIN);
            userRepository.save(systemUser);
        }
    }
}

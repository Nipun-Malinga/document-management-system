package com.nipun.system.seeder;

import com.nipun.system.user.Role;
import com.nipun.system.user.UserService;
import com.nipun.system.user.dtos.RegisterUserRequest;
import com.nipun.system.user.exceptions.UserNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.logging.Level;
import java.util.logging.Logger;

@RequiredArgsConstructor
@Component
public class DatabaseSeeder implements CommandLineRunner {

    @Value("${security.admin.email}")
    private String adminEmail;
    @Value("${security.admin.password}")
    private String adminPassword;

    private final UserService userService;
    private static final Logger logger = Logger.getLogger(DatabaseSeeder.class.getName());

    @Override
    public void run(String... args){
        try {
            var user = userService.findUser(adminEmail);
            if(user != null)
                logger.log(Level.INFO, "Database seeding: admin user already exists, skipping admin seed.");
        } catch (UserNotFoundException e) {
            logger.log(Level.INFO, "Database seeding: admin user not present, initializing default admin account.");
            userService.registerUser(
                    new RegisterUserRequest("Head", "Admin", "head_admin", adminEmail, adminPassword),
                    Role.ADMIN
            );
        }
    }
}

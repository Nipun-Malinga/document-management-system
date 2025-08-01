package com.nipun.system.user;

import com.nipun.system.auth.exceptions.BadCredentialsException;
import lombok.AllArgsConstructor;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

@AllArgsConstructor
@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        var user =  userRepository.findByEmail(email).orElseThrow(BadCredentialsException::new);

        return new User(
                user.getEmail(),
                user.getPassword(),
                Collections.emptyList()
        );
    }
}

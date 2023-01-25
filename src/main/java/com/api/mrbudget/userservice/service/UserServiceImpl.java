package com.api.mrbudget.userservice.service;

import com.api.mrbudget.userservice.dto.mapper.UserMapper;
import com.api.mrbudget.userservice.dto.model.UserDto;
import com.api.mrbudget.userservice.dto.response.JwtResponse;
import com.api.mrbudget.userservice.exception.UserException;
import  com.api.mrbudget.userservice.exception.EntityType;
import  com.api.mrbudget.userservice.exception.ExceptionType;
import com.api.mrbudget.userservice.model.User;
import com.api.mrbudget.userservice.repository.UserRepository;
import com.api.mrbudget.userservice.security.JwtUtil;
import com.api.mrbudget.userservice.security.UserDetailsImpl;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static com.api.mrbudget.userservice.exception.EntityType.USER;
import static com.api.mrbudget.userservice.exception.ExceptionType.DUPLICATE_ENTITY;
import static com.api.mrbudget.userservice.exception.ExceptionType.ENTITY_NOT_FOUND;


/**
 * Author: Daniel Lim
 *
 * Implementation class for UserService interface
 */
@Component
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private UserException userException;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * Handles the signup request.
     * Checks whether the user already exists.
     * If it exists, throw a duplicate error, otherwise, create a new user.
     *
     * @param userDto
     * @return UserDto
     */
    @Override
    public UserDto signup(UserDto userDto) {
        User user = userRepository.findByEmail(userDto.getEmail());

        if(user == null) {
            user = new User()
                    .setFirstName(userDto.getFirstName())
                    .setLastName(userDto.getLastName())
                    .setEmail(userDto.getEmail())
                    .setPassword(passwordEncoder.encode(userDto.getPassword()));

            return UserMapper.toUserDto(userRepository.save(user));
        }

        throw exception(USER, DUPLICATE_ENTITY, userDto.getEmail());
    }

    @Override
    public JwtResponse login(UserDto userDto) {

        // Control flows to Provider Manager, then, Authentication Provider
        Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(userDto.getEmail(), userDto.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Convert User object to UserDetails object
        UserDetailsImpl userPrincipal = (UserDetailsImpl) authentication.getPrincipal();

        // Generate a token
        String token = jwtUtil.generateToken(userPrincipal.getEmail());

        return new JwtResponse(
                token,
                userPrincipal.getId(),
                userPrincipal.getEmail(),
                userPrincipal.getFirstName(),
                userPrincipal.getLastName()
        );
    }

    /**
     * Handles requests finding a user by email.
     *
     * @param email
     * @return UserDto
     */
    @Transactional
    public UserDto findByEmail(String email) {
        Optional<User> user = Optional.ofNullable(userRepository.findByEmail(email));

        // Converts a user object to a userDto object
        if(user.isPresent()) return modelMapper.map(user.get(), UserDto.class);

        throw exception(USER, ENTITY_NOT_FOUND, email);
    }

    private RuntimeException exception(EntityType entityType, ExceptionType exceptionType, String... args) {
        return userException.throwException(entityType, exceptionType, args);
    }
}

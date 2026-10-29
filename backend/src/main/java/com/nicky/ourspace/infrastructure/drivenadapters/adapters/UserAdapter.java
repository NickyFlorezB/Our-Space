package com.nicky.ourspace.infrastructure.drivenadapters.adapters;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.nicky.ourspace.domain.model.User;
import com.nicky.ourspace.domain.port.UserPort;
import com.nicky.ourspace.infrastructure.drivenadapters.mapper.UserMapper;
import com.nicky.ourspace.infrastructure.drivenadapters.persistence.dbo.UserDbo;
import com.nicky.ourspace.infrastructure.drivenadapters.persistence.repository.JpaUserRepository;

@Service
public class UserAdapter implements UserPort {

    private final JpaUserRepository jpaUserRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    public UserAdapter(JpaUserRepository jpaUserRepository, UserMapper userMapper, PasswordEncoder passwordEncoder) {
        this.jpaUserRepository = jpaUserRepository;
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public User saveUser(User user) {
        user = new User(
                user.id(),
                user.username(),
                this.passwordEncoder.encode(user.password()),
                user.cellphone(),
                user.email());
        UserDbo userDbo = jpaUserRepository.save(
                this.userMapper.toDbo(user));
        return this.userMapper.toDomain(userDbo);
    }

    @Override
    public Optional<User> findUserById(UUID id) {
        Optional<UserDbo> userDbo = jpaUserRepository.findById(id);
        return userDbo.map(this.userMapper::toDomain);
    }

    @Override
    public List<User> findAllUsers() {
        List<UserDbo> userDbos = jpaUserRepository.findAll();
        return userDbos.stream()
                .map(this.userMapper::toDomain)
                .toList();
    }

    @Override
    public Optional<User> findUserByUsername(String username) {
        return jpaUserRepository.findByUsername(username).map(this.userMapper::toDomain);
    }

    @Override
    public Optional<User> findUserByEmail(String email) {
        return jpaUserRepository.findByEmail(email).map(this.userMapper::toDomain);
    }

    @Override
    public User updateUser(User user, UUID id) {
        Optional<UserDbo> existingUserDbo = jpaUserRepository.findById(id);
        if (existingUserDbo.isPresent()) {
            UserDbo userDboToUpdate = existingUserDbo.get();
            userDboToUpdate.setUsername(user.username());
            userDboToUpdate.setEmail(user.email());
            userDboToUpdate.setCellphone(user.cellphone());
            userDboToUpdate.setPassword(this.passwordEncoder.encode(user.password()));
            UserDbo updatedUserDbo = jpaUserRepository.save(userDboToUpdate);
            return this.userMapper.toDomain(updatedUserDbo);
        } else {
            throw new RuntimeException("User not found with id: " + id);
        }
    }

    @Override
    public void deleteUser(UUID id) {
        jpaUserRepository.deleteById(id);
    }

}

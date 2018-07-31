package com.reljicd.service.impl;

import com.reljicd.model.Role;
import com.reljicd.model.User;
import com.reljicd.repository.RoleRepository;
import com.reljicd.repository.UserRepository;
import com.reljicd.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Optional;

@Service
public class UserServiceImp implements UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    private static final String USER_ROLE = "ROLE_USER";

    @Autowired
    public UserServiceImp(UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Override
    public User save(User user) {
        // Encode plaintext password
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setActive(1);
        if (user.getRoles() == null || user.getRoles().isEmpty()) {
            user.setRoles(Collections.singletonList(roleRepository.findByRole(USER_ROLE)));
        }
        if (user.getId() != null) {
            userRepository.updatePasswordById(user.getId(), user.getPassword());
            updateRole(user);
            return user;
        }
        return userRepository.saveAndFlush(user);
    }

    public void updateRole(User user) {
        userRepository.deleteRoleUserByUserId(user.getId());
        if (null != user.getRoles() && user.getRoles().size() > 0) {
            for (Role role : user.getRoles()) {
                userRepository.AddRoleUser(user.getId(), role.getId());
            }
        }
    }

    public Role findByRole(String role) {
        return roleRepository.findByRole(role);
    }

    @Override
    public Page<User> findAllOrderedByIdPageable(int page) {
        Sort sort = new Sort(Sort.Direction.DESC, "id");
        return userRepository.findAllByOrderByIdDesc(new PageRequest(subtractPageByOne(page), 5, sort));
    }

    @Override
    public Optional<User> findForId(Long id) {
        return userRepository.findById(id);
    }

    private int subtractPageByOne(int page) {
        return (page < 1) ? 0 : page - 1;
    }
}
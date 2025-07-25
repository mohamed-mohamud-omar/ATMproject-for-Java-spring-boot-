package com.atm.ATMSystem.service;

import com.atm.ATMSystem.model.User;
import com.atm.ATMSystem.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    @Autowired private UserRepository userRepository;
    @Autowired private BCryptPasswordEncoder passwordEncoder;

    /* ---------------- Register (self or admin add) ---------------- */
    public User register(User user) {
        normalizeRole(user);          // ensure ROLE_USER / ROLE_ADMIN
        encodePasswordIfPlain(user);  // hash password if not already hashed
        return userRepository.save(user);
    }

    /* ---------------- CRUD ---------------- */
    public List<User> getAllUsers() { return userRepository.findAll(); }

    public User findById(Long id) { return userRepository.findById(id).orElse(null); }

    public Optional<User> findByEmail(String email) { return userRepository.findByEmail(email); }

    public User updateUser(User user) {
        encodePasswordIfPlain(user);
        return userRepository.save(user);
    }

    public void deleteUser(Long id) { userRepository.deleteById(id); }

    /* ---------------- Search (for admin dashboard) ---------------- */
    public List<User> searchUsers(String keyword) {
        return userRepository.findByFullNameContainingIgnoreCaseOrEmailContainingIgnoreCase(keyword, keyword);
    }

    /* ---------------- Admin edit helper ---------------- */
    public void updateUserDetails(Long id, User formUser) {
        User existing = findById(id);
        if (existing == null) return;

        if (StringUtils.hasText(formUser.getFullName())) existing.setFullName(formUser.getFullName());
        if (StringUtils.hasText(formUser.getEmail()))     existing.setEmail(formUser.getEmail());
        if (formUser.getBalance() != null)                existing.setBalance(formUser.getBalance());
        if (StringUtils.hasText(formUser.getPin()))       existing.setPin(formUser.getPin());
        if (StringUtils.hasText(formUser.getRole()))      existing.setRole(fixRolePrefix(formUser.getRole()));

        if (StringUtils.hasText(formUser.getPassword())) {
            existing.setPassword(formUser.getPassword());
            encodePasswordIfPlain(existing);
        }

        userRepository.save(existing);
    }

    /* ---------------- CURRENT AUTH USER ---------------- */
    public User getCurrentLoggedInUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) return null;
        String email = auth.getName();          // Spring Security username = email
        return findByEmail(email).orElse(null);
    }

    /* ---------------- PIN CHECK ---------------- */
    public boolean isPinCorrect(User user, String inputPin) {
        return user != null
                && user.getPin() != null
                && user.getPin().equals(inputPin);   // plaintext compare (your current design)
    }

    /* ---------------- Helpers ---------------- */
    private void encodePasswordIfPlain(User user) {
        String pw = user.getPassword();
        if (!StringUtils.hasText(pw)) return;
        if (isBCryptHash(pw)) return;
        user.setPassword(passwordEncoder.encode(pw));
    }

    private boolean isBCryptHash(String value) {
        return value.startsWith("$2a$") || value.startsWith("$2b$") || value.startsWith("$2y$");
    }

    private void normalizeRole(User user) {
        if (!StringUtils.hasText(user.getRole())) {
            user.setRole("ROLE_USER");
        } else {
            user.setRole(fixRolePrefix(user.getRole()));
        }
    }

    private String fixRolePrefix(String role) {
        return role.startsWith("ROLE_") ? role : "ROLE_" + role.toUpperCase();
    }
}

package com.homeprotectors.backend.service;

import com.homeprotectors.backend.entity.User;
import com.homeprotectors.backend.exception.ApiException;
import com.homeprotectors.backend.repository.UserRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class GuestRegisterService {

    private final UserRepository userRepository;

    public GuestRegisterService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional
    public UUID registerGuest(UUID installId) {
        var existing = userRepository.findByInstallId(installId);
        if (existing.isPresent()) {
            return existing.get().getPublicId();
        }

        try {
            User user = new User(UUID.randomUUID(), installId);
            userRepository.saveAndFlush(user);
            return user.getPublicId();
        } catch (DataIntegrityViolationException e) {
            return userRepository.findByInstallId(installId)
                    .map(User::getPublicId)
                    .orElseThrow(() -> new ApiException("USER_REGISTER_FAILED", "Guest registration failed unexpectedly."));
        }
    }
}

package com.healthapp.user.service;

import com.healthapp.user.dto.request.ChangePasswordRequest;
import com.healthapp.user.dto.request.UpdateUserRequest;
import com.healthapp.user.dto.request.UserSearchRequest;
import com.healthapp.user.dto.response.PageResponse;
import com.healthapp.user.dto.response.UserResponse;
import com.healthapp.user.entity.User;
import com.healthapp.user.enums.UserRole;
import com.healthapp.user.exception.InvalidPasswordException;
import com.healthapp.user.exception.UserAlreadyExistsException;
import com.healthapp.user.exception.UserNotFoundException;
import com.healthapp.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class UserService {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    
    public UserResponse getUserById(String userId) {
        log.info("Fetching user by ID: {}", userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + userId));
        return mapToUserResponse(user);
    }
    
    public UserResponse getUserByEmail(String email) {
        log.info("Fetching user by email: {}", email);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found with email: " + email));
        return mapToUserResponse(user);
    }
    
    public List<UserResponse> getAllUsers() {
        log.info("Fetching all users");
        return userRepository.findAll().stream()
                .map(this::mapToUserResponse)
                .collect(Collectors.toList());
    }
    
    public PageResponse<UserResponse> searchUsers(UserSearchRequest request) {
        log.info("Searching users with criteria: {}", request);
        
        Pageable pageable = PageRequest.of(
                request.getPage(), 
                request.getSize(), 
                Sort.by("createdAt").descending()
        );
        
        Page<User> userPage;
        
        if (request.getEmail() != null || request.getFirstName() != null || request.getLastName() != null) {
            String keyword = request.getEmail() != null ? request.getEmail() : 
                           request.getFirstName() != null ? request.getFirstName() : 
                           request.getLastName();
            userPage = userRepository.searchUsers(keyword, pageable);
        } else if (request.getRole() != null) {
            List<User> users = userRepository.findByRolesContaining(request.getRole());
            userPage = new org.springframework.data.domain.PageImpl<>(users, pageable, users.size());
        } else {
            userPage = userRepository.findAll(pageable);
        }
        
        return PageResponse.<UserResponse>builder()
                .content(userPage.getContent().stream()
                        .map(this::mapToUserResponse)
                        .collect(Collectors.toList()))
                .page(userPage.getNumber())
                .size(userPage.getSize())
                .totalElements(userPage.getTotalElements())
                .totalPages(userPage.getTotalPages())
                .first(userPage.isFirst())
                .last(userPage.isLast())
                .build();
    }
    
    public UserResponse updateUser(String userId, UpdateUserRequest request) {
        log.info("Updating user: {}", userId);
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + userId));
        
        // Check email uniqueness if changed
        if (request.getEmail() != null && !request.getEmail().equals(user.getEmail())) {
            if (userRepository.existsByEmail(request.getEmail())) {
                throw new UserAlreadyExistsException("Email already in use: " + request.getEmail());
            }
            user.setEmail(request.getEmail());
        }
        
        if (request.getFirstName() != null) {
            user.setFirstName(request.getFirstName());
        }
        
        if (request.getLastName() != null) {
            user.setLastName(request.getLastName());
        }
        
        if (request.getPhoneNumber() != null) {
            user.setPhoneNumber(request.getPhoneNumber());
        }
        
        if (request.getProfilePictureUrl() != null) {
            user.setProfilePictureUrl(request.getProfilePictureUrl());
        }
        
        User updatedUser = userRepository.save(user);
        log.info("User updated successfully: {}", userId);
        
        return mapToUserResponse(updatedUser);
    }
    
    public void deleteUser(String userId) {
        log.info("Deleting user: {}", userId);
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + userId));
        
        userRepository.delete(user);
        log.info("User deleted successfully: {}", userId);
    }
    
    public List<UserResponse> getUsersByRole(UserRole role) {
        log.info("Fetching users by role: {}", role);
        return userRepository.findByRolesContaining(role).stream()
                .map(this::mapToUserResponse)
                .collect(Collectors.toList());
    }
    
    public long countUsersByRole(UserRole role) {
        return userRepository.countByRolesContaining(role);
    }
    
    public UserResponse mapToUserResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .fullName(user.getFullName())
                .birthDate(user.getBirthDate())
                .gender(user.getGender())
                .phoneNumber(user.getPhoneNumber())
                .profilePictureUrl(user.getProfilePictureUrl())
                .roles(user.getRoles())
                .accountStatus(user.getAccountStatus())
                .isEmailVerified(user.getIsEmailVerified())
                .isActivated(user.getIsActivated())
                .lastLoginAt(user.getLastLoginAt())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .medicalLicenseNumber(user.getMedicalLicenseNumber())
                .specialization(user.getSpecialization())
                .hospitalAffiliation(user.getHospitalAffiliation())
                .yearsOfExperience(user.getYearsOfExperience())
                .activationDate(user.getActivationDate())
                .build();
    }
}
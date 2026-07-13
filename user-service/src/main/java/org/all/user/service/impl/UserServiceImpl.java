package org.all.user.service.impl;

import org.all.common.exception.BusinessException;
import org.all.common.model.PageResponse;
import org.all.user.dto.UserRequest;
import org.all.user.dto.UserResponse;
import org.all.user.entity.User;
import org.all.user.repository.UserRepository;
import org.all.user.service.UserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserResponse createInternalUser(String keycloakId, String email, String username, String phone) {
        if (userRepository.existsByUsername(username)) {
            throw new BusinessException(409, "用户名已存在");
        }
        if (email != null && userRepository.existsByEmail(email)) {
            throw new BusinessException(409, "邮箱已存在");
        }

        User user = User.builder()
                .keycloakId(keycloakId)
                .username(username)
                .email(email)
                .phone(phone)
                .build();

        User savedUser = userRepository.save(user);
        return toUserResponse(savedUser);
    }

    @Override
    public UserResponse getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new BusinessException(404, "用户不存在"));
        return toUserResponse(user);
    }

    @Override
    public UserResponse getUserByKeycloakId(String keycloakId) {
        User user = userRepository.findByKeycloakId(keycloakId)
                .orElseThrow(() -> new BusinessException(404, "用户不存在"));
        return toUserResponse(user);
    }

    @Override
    public PageResponse<UserResponse> getAllUsers(int page, int size) {
        Page<User> userPage = userRepository.findAll(PageRequest.of(page, size, Sort.by("id")));
        return PageResponse.<UserResponse>builder()
                .content(userPage.getContent().stream().map(this::toUserResponse).collect(Collectors.toList()))
                .page(userPage.getNumber())
                .size(userPage.getSize())
                .totalElements(userPage.getTotalElements())
                .totalPages(userPage.getTotalPages())
                .build();
    }

    @Override
    public UserResponse updateUser(Long id, UserRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new BusinessException(404, "用户不存在"));

        if (!user.getUsername().equals(request.getUsername())
                && userRepository.existsByUsername(request.getUsername())) {
            throw new BusinessException(409, "用户名已存在");
        }
        if (request.getEmail() != null && !request.getEmail().equals(user.getEmail())
                && userRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException(409, "邮箱已存在");
        }

        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());

        if (request.getKeycloakId() != null) {
            user.setKeycloakId(request.getKeycloakId());
        }

        User updatedUser = userRepository.save(user);
        return toUserResponse(updatedUser);
    }

    @Override
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new BusinessException(404, "用户不存在");
        }
        userRepository.deleteById(id);
    }

    @Override
    public void deleteByKeycloakId(String keycloakId) {
        User user = userRepository.findByKeycloakId(keycloakId)
                .orElseThrow(() -> new BusinessException(404, "用户不存在"));
        userRepository.delete(user);
    }

    private UserResponse toUserResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .keycloakId(user.getKeycloakId())
                .username(user.getUsername())
                .email(user.getEmail())
                .phone(user.getPhone())
                .createdTime(user.getCreatedTime())
                .updatedTime(user.getUpdatedTime())
                .build();
    }
}

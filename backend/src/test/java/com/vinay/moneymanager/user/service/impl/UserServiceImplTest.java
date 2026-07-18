package com.vinay.moneymanager.user.service.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.vinay.moneymanager.common.exception.DuplicateResourceException;
import com.vinay.moneymanager.user.dto.request.RegisterRequest;
import com.vinay.moneymanager.user.dto.response.RegisterResponse;
import com.vinay.moneymanager.user.entity.User;
import com.vinay.moneymanager.user.repository.UserRepository;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

  @Mock private UserRepository userRepository;

  @Mock private PasswordEncoder passwordEncoder;

  @InjectMocks private UserServiceImpl userServiceImpl;

  @Captor private ArgumentCaptor<User> userCaptor;

  @Test
  void shouldRegisterUserSuccessfully() {

    RegisterRequest request = createRegisterRequest();

    User savedUser = new User();

    savedUser.setId(UUID.randomUUID());
    savedUser.setEmail(request.getEmail());
    savedUser.setFirstName(request.getFirstName());
    savedUser.setLastName(request.getLastName());
    savedUser.setPassword("encodedPassword");

    when(userRepository.existsByEmail(request.getEmail())).thenReturn(false);
    when(passwordEncoder.encode(request.getPassword())).thenReturn("encodedPassword");
    when(userRepository.save(any(User.class))).thenReturn(savedUser);

    RegisterResponse response = userServiceImpl.register(request);

    assertNotNull(response);
    assertEquals(savedUser.getEmail(), response.getEmail());
    assertEquals(savedUser.getFirstName(), response.getFirstName());
    assertEquals(savedUser.getLastName(), response.getLastName());

    verify(userRepository).existsByEmail(request.getEmail());
    verify(passwordEncoder).encode(request.getPassword());
    verify(userRepository).save(any(User.class));
  }

  @Test
  void shouldThrowExceptionWhenEmailAlreadyExists() {

    RegisterRequest request = createRegisterRequest();

    when(userRepository.existsByEmail(request.getEmail())).thenReturn(true);

    DuplicateResourceException exception =
        assertThrows(DuplicateResourceException.class, () -> userServiceImpl.register(request));
    assertEquals(
        "User with email " + request.getEmail() + " already exists", exception.getMessage());

    verify(passwordEncoder, never()).encode(anyString());
    verify(userRepository, never()).save(any(User.class));
  }

  @Test
  void shouldEncodePasswordBeforeSaving() {

    RegisterRequest request = createRegisterRequest();

    when(userRepository.existsByEmail(request.getEmail())).thenReturn(false);
    when(passwordEncoder.encode(request.getPassword())).thenReturn("encodedPassword");

    User savedUser = new User();
    savedUser.setId(UUID.randomUUID());

    when(userRepository.save(any(User.class))).thenReturn(savedUser);

    userServiceImpl.register(request);

    verify(userRepository).save(userCaptor.capture());
    verify(passwordEncoder).encode(request.getPassword());

    User capturedUser = userCaptor.getValue();

    assertEquals(request.getEmail(), capturedUser.getEmail());
    assertEquals(request.getFirstName(), capturedUser.getFirstName());
    assertEquals(request.getLastName(), capturedUser.getLastName());
    assertNotEquals(request.getPassword(), capturedUser.getPassword());
  }

  private RegisterRequest createRegisterRequest() {
    RegisterRequest request = new RegisterRequest();
    request.setEmail("vinay@test.com");
    request.setPassword("Password@123");
    request.setFirstName("Vinay");
    request.setLastName("R");
    return request;
  }
}

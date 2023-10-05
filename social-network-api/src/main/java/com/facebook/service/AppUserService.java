package com.facebook.service;

import com.facebook.dto.appuser.AppUserEditRequest;
import com.facebook.exception.UserNotFoundException;
import com.facebook.model.AppUser;
import com.facebook.repository.AppUserRepository;

import java.util.List;
import java.util.Optional;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Log4j2
@Service
@RequiredArgsConstructor
public class AppUserService {

    private static final String USER_NOT_FOUND_ERROR_MSG = "User not found with id: ";

    private final AppUserRepository repo;

    private final PasswordEncoder passwordEncoder;


    //Тільки для генерації.
    public List<AppUser> findAll() {
        return repo.findAll();
    }

    public Page<AppUser> findAllAppUsers(Pageable pageable) {
        return repo.findAll(pageable);
    }

    public String[] findRolesById(Long id) {
        return repo.findById(id)
                .map(AppUser::getRoles)
                .orElseThrow(() -> new UsernameNotFoundException(USER_NOT_FOUND_ERROR_MSG + id));
    }

    public String findUsernameById(Long id) {
        return repo.findById(id)
                .map(AppUser::getUsername)
                .orElseThrow(() -> new UsernameNotFoundException(USER_NOT_FOUND_ERROR_MSG + id));
    }

    public String findPasswordById(Long id) {
        return repo.findById(id)
                .map(AppUser::getPassword)
                .orElseThrow(() -> new UsernameNotFoundException(USER_NOT_FOUND_ERROR_MSG + id));
    }

    public Optional<AppUser> findByUsername(String username) {
        Optional<AppUser> appUser = repo.findByUsername(username);
        log.info(appUser);
        return appUser;

    }

    public Optional<AppUser> findById(Long id) {
        return repo.findById(id);

    }

    public Optional<AppUser> save(AppUser appUser)
            throws DataIntegrityViolationException {
        return Optional.of(repo.save(appUser));

    }

    public Optional<AppUser> findByEmail(String email) {
        return repo.findByEmail(email);
    }

    public void updatePassword(String email, String pass) {
        findByEmail(email).ifPresentOrElse(u -> {
                    u.setPassword(passwordEncoder.encode(pass));
                    save(u);
                },
                () -> {
                    throw new UserNotFoundException();
                }
        );
    }

    public Optional<AppUser> editUser(Long id, AppUserEditRequest userEditReq) {
        Optional<AppUser> user = findById(id);
        if(user.isPresent()) {
            user.map(u -> {
                u.setName(userEditReq.getName());
                u.setSurname(userEditReq.getSurname());
                u.setUsername(userEditReq.getUsername());
                u.setEmail(userEditReq.getEmail());
                u.setAddress(userEditReq.getAddress());
                u.setAvatar(userEditReq.getAvatar());
                u.setHeaderPhoto(userEditReq.getHeaderPhoto());
                u.setDateOfBirth(userEditReq.getDateOfBirth());
                return save(u);
            });
            return user;
        } else {
            return Optional.empty();
        }
    }

}

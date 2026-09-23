/*
 UserServiceImpl.java

 Business logic for User. Implements the generic CRUD contract
 IService<User, Long> plus the User-specific operations.

 Author: Mogamat Yaseen Kannemeyer 240453182
 Date: 04 September 2026
*/

package za.ac.cput.service.identity;

import java.util.List;

import org.springframework.stereotype.Service;

import za.ac.cput.domain.enums.AccountStatus;
import za.ac.cput.domain.identity.User;
import za.ac.cput.repository.identity.UserRepository;

@Service
public class UserServiceImpl implements IUserService {

    private final UserRepository repository;

    public UserServiceImpl(UserRepository repository) {
        this.repository = repository;
    }

    @Override
    public User create(User user) {
        return this.repository.save(user);
    }

    @Override
    public User read(Long id) {
        return id == null ? null : this.repository.findById(id).orElse(null);
    }

    @Override
    public User update(User user) {
        return this.repository.save(user);
    }

    @Override
    public boolean delete(Long id) {
        if (id == null || !this.repository.existsById(id)) {
            return false;
        }
        this.repository.deleteById(id);
        return true;
    }

    @Override
    public List<User> getAll() {
        return this.repository.findAll();
    }

    @Override
    public User findByEmail(String email) {
        return this.repository.findByEmail(email).orElse(null);
    }

    @Override
    public boolean existsByEmail(String email) {
        return this.repository.existsByEmail(email);
    }

    @Override
    public List<User> findByAccountStatus(AccountStatus accountStatus) {
        return this.repository.findByAccountStatus(accountStatus);
    }

}

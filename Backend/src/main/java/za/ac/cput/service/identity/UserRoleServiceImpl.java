/*
 UserRoleServiceImpl.java

 Business logic for UserRole. Implements the generic CRUD contract
 IService<UserRole, Long> plus the UserRole-specific operations.

 Author: Mogamat Yaseen Kannemeyer 240453182
 Date: 04 September 2026
*/

package za.ac.cput.service.identity;

import java.util.List;

import org.springframework.stereotype.Service;

import za.ac.cput.domain.identity.UserRole;
import za.ac.cput.factory.identity.UserRoleFactory;
import za.ac.cput.repository.identity.UserRoleRepository;

@Service
public class UserRoleServiceImpl implements IUserRoleService {

    private final UserRoleRepository repository;

    public UserRoleServiceImpl(UserRoleRepository repository) {
        this.repository = repository;
    }

    @Override
    public UserRole create(UserRole userRole) {
        return this.repository.save(userRole);
    }

    @Override
    public UserRole read(Long id) {
        return id == null ? null : this.repository.findById(id).orElse(null);
    }

    @Override
    public UserRole update(UserRole userRole) {
        return this.repository.save(userRole);
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
    public List<UserRole> getAll() {
        return this.repository.findAll();
    }

    @Override
    public List<UserRole> findByUserId(long userId) {
        return this.repository.findByUserId(userId);
    }

    @Override
    public UserRole assignRole(long userId, long roleId) {
        if (this.repository.existsByUserIdAndRoleId(userId, roleId)) {
            return this.repository.findByUserId(userId).stream()
                    .filter(ur -> ur.getRoleId() == roleId)
                    .findFirst()
                    .orElse(null);
        }
        return this.repository.save(UserRoleFactory.createUserRole(userId, roleId));
    }

}

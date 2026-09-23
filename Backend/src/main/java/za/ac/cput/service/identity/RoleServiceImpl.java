/*
 RoleServiceImpl.java

 Business logic for Role. Implements the generic CRUD contract
 IService<Role, Long> plus the Role-specific operations.

 Author: Mogamat Yaseen Kannemeyer 240453182
 Date: 04 September 2026
*/

package za.ac.cput.service.identity;

import java.util.List;

import org.springframework.stereotype.Service;

import za.ac.cput.domain.enums.RoleType;
import za.ac.cput.domain.identity.Role;
import za.ac.cput.repository.identity.RoleRepository;

@Service
public class RoleServiceImpl implements IRoleService {

    private final RoleRepository repository;

    public RoleServiceImpl(RoleRepository repository) {
        this.repository = repository;
    }

    @Override
    public Role create(Role role) {
        return this.repository.save(role);
    }

    @Override
    public Role read(Long id) {
        return id == null ? null : this.repository.findById(id).orElse(null);
    }

    @Override
    public Role update(Role role) {
        return this.repository.save(role);
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
    public List<Role> getAll() {
        return this.repository.findAll();
    }

    @Override
    public Role findByName(RoleType name) {
        return this.repository.findByName(name).orElse(null);
    }

}

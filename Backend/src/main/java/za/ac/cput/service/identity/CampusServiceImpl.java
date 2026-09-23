/*
 CampusServiceImpl.java

 Business logic for Campus. Implements the generic CRUD contract
 IService<Campus, Long> plus the Campus-specific operations.

 Author: Mogamat Yaseen Kannemeyer 240453182
 Date: 04 September 2026
*/

package za.ac.cput.service.identity;

import java.util.List;

import org.springframework.stereotype.Service;

import za.ac.cput.domain.identity.Campus;
import za.ac.cput.repository.identity.CampusRepository;

@Service
public class CampusServiceImpl implements ICampusService {

    private final CampusRepository repository;

    public CampusServiceImpl(CampusRepository repository) {
        this.repository = repository;
    }

    @Override
    public Campus create(Campus campus) {
        return this.repository.save(campus);
    }

    @Override
    public Campus read(Long id) {
        return id == null ? null : this.repository.findById(id).orElse(null);
    }

    @Override
    public Campus update(Campus campus) {
        return this.repository.save(campus);
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
    public List<Campus> getAll() {
        return this.repository.findAll();
    }

    @Override
    public List<Campus> findByCity(String city) {
        return this.repository.findByCity(city);
    }

}

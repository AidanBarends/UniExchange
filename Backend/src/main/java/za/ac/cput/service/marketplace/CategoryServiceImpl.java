/*
 CategoryServiceImpl.java

 Business logic for Category. Implements the generic CRUD contract
 IService<Category, Long> plus the Category-specific operations.

 Author: Mogamat Yaseen Kannemeyer 240453182
 Date: 04 September 2026
*/

package za.ac.cput.service.marketplace;

import java.util.List;

import org.springframework.stereotype.Service;

import za.ac.cput.domain.marketplace.Category;
import za.ac.cput.repository.marketplace.CategoryRepository;

@Service
public class CategoryServiceImpl implements ICategoryService {

    private final CategoryRepository repository;

    public CategoryServiceImpl(CategoryRepository repository) {
        this.repository = repository;
    }

    @Override
    public Category create(Category category) {
        return this.repository.save(category);
    }

    @Override
    public Category read(Long id) {
        return id == null ? null : this.repository.findById(id).orElse(null);
    }

    @Override
    public Category update(Category category) {
        return this.repository.save(category);
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
    public List<Category> getAll() {
        return this.repository.findAll();
    }

    @Override
    public Category findByName(String name) {
        return this.repository.findByNameIgnoreCase(name).orElse(null);
    }

}

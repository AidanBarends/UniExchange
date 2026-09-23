/*
 UserController.java

 REST endpoints for User.

 Author: Mogamat Yaseen Kannemeyer 240453182
 Date: 04 September 2026
*/

package za.ac.cput.controller.identity;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import za.ac.cput.domain.enums.AccountStatus;
import za.ac.cput.domain.identity.User;
import za.ac.cput.dto.identity.UserRequest;
import za.ac.cput.factory.identity.UserFactory;
import za.ac.cput.service.identity.IUserService;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final IUserService service;

    public UserController(IUserService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<User> create(@RequestBody UserRequest request) {
        User created = this.service.create(UserFactory.createUser(
                request.email(), request.firstName(), request.middleName(), request.lastName(),
                request.cellPhone(), request.passwordHash(), request.dateOfBirth(), request.accountStatus(),
                request.campusId()));
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/{id}")
    public ResponseEntity<User> read(@PathVariable Long id) {
        User found = this.service.read(id);
        return found == null ? ResponseEntity.notFound().build() : ResponseEntity.ok(found);
    }

    @PutMapping("/{id}")
    public ResponseEntity<User> update(@PathVariable Long id, @RequestBody UserRequest request) {
        User existing = this.service.read(id);
        if (existing == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(this.service.update(UserFactory.updateUser(
                existing, request.email(), request.firstName(), request.middleName(), request.lastName(),
                request.cellPhone(), request.passwordHash(), request.dateOfBirth(), request.accountStatus(),
                request.campusId())));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        return this.service.delete(id)
                ? ResponseEntity.noContent().build()
                : ResponseEntity.notFound().build();
    }

    @GetMapping
    public List<User> getAll() {
        return this.service.getAll();
    }

    @GetMapping("/email/{email}")
    public ResponseEntity<User> byEmail(@PathVariable String email) {
        User found = this.service.findByEmail(email);
        return found == null ? ResponseEntity.notFound().build() : ResponseEntity.ok(found);
    }

    @GetMapping("/status/{status}")
    public List<User> byStatus(@PathVariable AccountStatus status) {
        return this.service.findByAccountStatus(status);
    }

}

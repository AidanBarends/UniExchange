/*
 UserRoleController.java

 REST endpoints for UserRole.

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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import za.ac.cput.domain.identity.UserRole;
import za.ac.cput.dto.identity.UserRoleRequest;
import za.ac.cput.factory.identity.UserRoleFactory;
import za.ac.cput.service.identity.IUserRoleService;

@RestController
@RequestMapping("/api/user-roles")
public class UserRoleController {

    private final IUserRoleService service;

    public UserRoleController(IUserRoleService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<UserRole> create(@RequestBody UserRoleRequest request) {
        UserRole created = this.service.create(UserRoleFactory.createUserRole(
                request.userId(), request.roleId()));
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserRole> read(@PathVariable Long id) {
        UserRole found = this.service.read(id);
        return found == null ? ResponseEntity.notFound().build() : ResponseEntity.ok(found);
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserRole> update(@PathVariable Long id, @RequestBody UserRoleRequest request) {
        UserRole existing = this.service.read(id);
        if (existing == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(this.service.update(UserRoleFactory.updateUserRole(
                existing, request.userId(), request.roleId())));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        return this.service.delete(id)
                ? ResponseEntity.noContent().build()
                : ResponseEntity.notFound().build();
    }

    @GetMapping
    public List<UserRole> getAll() {
        return this.service.getAll();
    }

    @GetMapping("/user/{userId}")
    public List<UserRole> byUser(@PathVariable long userId) {
        return this.service.findByUserId(userId);
    }

    @PostMapping("/assign")
    public ResponseEntity<UserRole> assign(@RequestParam long userId, @RequestParam long roleId) {
        return ResponseEntity.status(HttpStatus.CREATED).body(this.service.assignRole(userId, roleId));
    }

}

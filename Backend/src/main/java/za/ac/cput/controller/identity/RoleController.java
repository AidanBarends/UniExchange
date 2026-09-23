/*
 RoleController.java

 REST endpoints for Role.

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

import za.ac.cput.domain.enums.RoleType;
import za.ac.cput.domain.identity.Role;
import za.ac.cput.dto.identity.RoleRequest;
import za.ac.cput.factory.identity.RoleFactory;
import za.ac.cput.service.identity.IRoleService;

@RestController
@RequestMapping("/api/roles")
public class RoleController {

    private final IRoleService service;

    public RoleController(IRoleService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<Role> create(@RequestBody RoleRequest request) {
        Role created = this.service.create(RoleFactory.createRole(request.name(), request.description()));
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Role> read(@PathVariable Long id) {
        Role found = this.service.read(id);
        return found == null ? ResponseEntity.notFound().build() : ResponseEntity.ok(found);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Role> update(@PathVariable Long id, @RequestBody RoleRequest request) {
        Role existing = this.service.read(id);
        if (existing == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(this.service.update(RoleFactory.updateRole(
                existing, request.name(), request.description())));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        return this.service.delete(id)
                ? ResponseEntity.noContent().build()
                : ResponseEntity.notFound().build();
    }

    @GetMapping
    public List<Role> getAll() {
        return this.service.getAll();
    }

    @GetMapping("/name/{name}")
    public ResponseEntity<Role> byName(@PathVariable RoleType name) {
        Role found = this.service.findByName(name);
        return found == null ? ResponseEntity.notFound().build() : ResponseEntity.ok(found);
    }

}

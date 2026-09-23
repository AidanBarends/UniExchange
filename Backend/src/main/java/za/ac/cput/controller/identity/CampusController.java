/*
 CampusController.java

 REST endpoints for Campus.

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

import za.ac.cput.domain.identity.Campus;
import za.ac.cput.dto.identity.CampusRequest;
import za.ac.cput.factory.identity.CampusFactory;
import za.ac.cput.service.identity.ICampusService;

@RestController
@RequestMapping("/api/campuses")
public class CampusController {

    private final ICampusService service;

    public CampusController(ICampusService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<Campus> create(@RequestBody CampusRequest request) {
        Campus created = this.service.create(CampusFactory.createCampus(
                request.name(), request.city(), request.address()));
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Campus> read(@PathVariable Long id) {
        Campus found = this.service.read(id);
        return found == null ? ResponseEntity.notFound().build() : ResponseEntity.ok(found);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Campus> update(@PathVariable Long id, @RequestBody CampusRequest request) {
        Campus existing = this.service.read(id);
        if (existing == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(this.service.update(CampusFactory.updateCampus(
                existing, request.name(), request.city(), request.address())));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        return this.service.delete(id)
                ? ResponseEntity.noContent().build()
                : ResponseEntity.notFound().build();
    }

    @GetMapping
    public List<Campus> getAll() {
        return this.service.getAll();
    }

    @GetMapping("/city/{city}")
    public List<Campus> byCity(@PathVariable String city) {
        return this.service.findByCity(city);
    }

}

package st2apr.workshop.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import st2apr.workshop.model.Programmer;
import st2apr.workshop.service.ProgrammerService;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/programmers")
public class ProgrammerController {

    private final ProgrammerService programmerService;

    public ProgrammerController(ProgrammerService programmerService) {
        this.programmerService = programmerService;
    }

    @GetMapping
    public List<Programmer> getProgrammers() {
        return programmerService.findAll();
    }

    @GetMapping("/{id}")
    public Programmer getProgrammer(@PathVariable Long id) {
        return programmerService.findById(id);
    }

    @PostMapping
    public ResponseEntity<Programmer> createProgrammer(@RequestBody Programmer programmer) {
        Programmer saved = programmerService.create(programmer);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(saved.getId())
                .toUri();
        return ResponseEntity.created(location).body(saved);
    }

    @PutMapping("/{id}")
    public Programmer updateProgrammer(@PathVariable Long id, @RequestBody Programmer programmer) {
        return programmerService.update(id, programmer);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProgrammer(@PathVariable Long id) {
        programmerService.delete(id);
        return ResponseEntity.noContent().build();
    }
}

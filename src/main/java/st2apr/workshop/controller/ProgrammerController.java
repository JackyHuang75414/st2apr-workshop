package st2apr.workshop.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import st2apr.workshop.model.Programmer;
import st2apr.workshop.service.ProgrammerService;

import java.util.List;

@RestController
public class ProgrammerController {

    private final ProgrammerService programmerService;

    public ProgrammerController(ProgrammerService programmerService) {
        this.programmerService = programmerService;
    }

    @GetMapping("/programmers")
    public List<Programmer> getProgrammers() {
        return programmerService.findAll();
    }
}

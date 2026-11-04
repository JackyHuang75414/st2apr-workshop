package st2apr.workshop.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import st2apr.workshop.model.Programmer;

import java.math.BigDecimal;
import java.util.List;

@RestController
public class ProgrammerController {

    @GetMapping("/programmers")
    public List<Programmer> getProgrammers() {
        return List.of(
                new Programmer(1L, "Augustin", "Jacques", "18 rue des Alouettes",
                        "Java", "Clean Code", new BigDecimal("3000")),
                new Programmer(2L, "Louverture", "Toussaint", "5 place Bellecour, Lyon",
                        "C", "Le Code des Revolutions", new BigDecimal("2800"))
        );
    }
}

package st2apr.workshop.service;
import st2apr.workshop.model.Programmer;
import st2apr.workshop.repository.ProgrammerRepository;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class ProgrammerService {
private final ProgrammerRepository programmerRepository;
public ProgrammerService(ProgrammerRepository programmerRepository) {
this.programmerRepository = programmerRepository;
}
public List<Programmer> findAll() {
return programmerRepository.findAll();
}
}
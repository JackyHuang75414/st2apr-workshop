package st2apr.workshop.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import st2apr.workshop.exception.ProgrammerNotFoundException;
import st2apr.workshop.model.Programmer;
import st2apr.workshop.repository.ProgrammerRepository;

import java.util.List;

@Service
public class ProgrammerService {

    private final ProgrammerRepository programmerRepository;

    public ProgrammerService(ProgrammerRepository programmerRepository) {
        this.programmerRepository = programmerRepository;
    }

    public List<Programmer> findAll() {
        return programmerRepository.findAll();
    }

    public Programmer findById(Long id) {
        return programmerRepository.findById(id)
                .orElseThrow(() -> new ProgrammerNotFoundException(id));
    }

    @Transactional
    public Programmer create(Programmer programmer) {
        return programmerRepository.save(programmer);
    }

    @Transactional
    public Programmer update(Long id, Programmer data) {
        Programmer existing = programmerRepository.findById(id)
                .orElseThrow(() -> new ProgrammerNotFoundException(id));

        existing.setLastName(data.getLastName());
        existing.setFirstName(data.getFirstName());
        existing.setAddress(data.getAddress());
        existing.setExpertiseLanguage(data.getExpertiseLanguage());
        existing.setFavoriteBook(data.getFavoriteBook());
        existing.setSalary(data.getSalary());

        return programmerRepository.save(existing);
    }

    @Transactional
    public void delete(Long id) {
        if (!programmerRepository.existsById(id)) {
            throw new ProgrammerNotFoundException(id);
        }
        programmerRepository.deleteById(id);
    }
}

package cl.salfa.workshop.service;

import cl.salfa.workshop.dto.WorkshopRequest;
import cl.salfa.workshop.entity.Workshop;
import cl.salfa.workshop.repository.WorkshopRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class WorkshopService {

    private final WorkshopRepository workshopRepository;

    @Transactional
    public Workshop create(WorkshopRequest request) {

        Workshop workshop = Workshop.builder()
                .name(request.name().trim())
                .city(request.city())
                .address(request.address())
                .active(true)
                .build();

        return workshopRepository.save(workshop);
    }

    @Transactional(readOnly = true)
    public List<Workshop> findAll() {
        return workshopRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Workshop findById(Long id) {
        return workshopRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException(
                        "Taller no encontrado: " + id
                ));
    }
}
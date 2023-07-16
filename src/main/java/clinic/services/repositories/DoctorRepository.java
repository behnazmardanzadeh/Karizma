package clinic.services.repositories;

import clinic.models.Doctor;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DoctorRepository extends JpaRepository<Doctor, Long>, EntityRepositoryCustom {
}

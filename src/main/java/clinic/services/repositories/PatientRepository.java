package clinic.services.repositories;

import clinic.models.Patient;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PatientRepository extends JpaRepository<Patient, Long>, EntityRepositoryCustom {
}

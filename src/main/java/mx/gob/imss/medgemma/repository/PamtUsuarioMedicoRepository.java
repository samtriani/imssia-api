package mx.gob.imss.medgemma.repository;
import mx.gob.imss.medgemma.entity.PamtUsuarioMedico;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.UUID;
@Repository
public interface PamtUsuarioMedicoRepository extends JpaRepository<PamtUsuarioMedico, UUID> {
    Optional<PamtUsuarioMedico> findByNumMatriculaAndIndActivoTrue(String numMatricula);
}

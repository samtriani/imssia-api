package mx.gob.imss.medgemma.repository;
import mx.gob.imss.medgemma.entity.PamtExpedienteClinico;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.UUID;
@Repository
public interface PamtExpedienteClinicoRepository extends JpaRepository<PamtExpedienteClinico, UUID> {
    @Query("SELECT e FROM PamtExpedienteClinico e WHERE e.paciente.idPaciente=:id AND e.indActivo=true")
    Optional<PamtExpedienteClinico> findByIdPaciente(@Param("id") UUID idPaciente);
}

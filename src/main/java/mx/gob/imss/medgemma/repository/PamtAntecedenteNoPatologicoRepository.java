package mx.gob.imss.medgemma.repository;
import mx.gob.imss.medgemma.entity.PamtAntecedenteNoPatologico;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.UUID;
@Repository
public interface PamtAntecedenteNoPatologicoRepository extends JpaRepository<PamtAntecedenteNoPatologico, Integer> {
    @Query("SELECT a FROM PamtAntecedenteNoPatologico a WHERE a.expediente.idExpediente=:id")
    Optional<PamtAntecedenteNoPatologico> findByIdExpediente(@Param("id") UUID id);
}

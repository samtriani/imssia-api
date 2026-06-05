package mx.gob.imss.medgemma.repository;
import mx.gob.imss.medgemma.entity.PamtAntecedenteHeredofamiliar;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;
@Repository
public interface PamtAntecedenteHeredofamiliarRepository extends JpaRepository<PamtAntecedenteHeredofamiliar, Integer> {
    @Query("SELECT a FROM PamtAntecedenteHeredofamiliar a WHERE a.expediente.idExpediente=:id")
    List<PamtAntecedenteHeredofamiliar> findByIdExpediente(@Param("id") UUID id);
}

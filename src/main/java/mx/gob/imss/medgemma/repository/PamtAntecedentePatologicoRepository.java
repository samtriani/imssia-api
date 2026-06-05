package mx.gob.imss.medgemma.repository;
import mx.gob.imss.medgemma.entity.PamtAntecedentePatologico;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;
@Repository
public interface PamtAntecedentePatologicoRepository extends JpaRepository<PamtAntecedentePatologico, Integer> {
    @Query("SELECT a FROM PamtAntecedentePatologico a WHERE a.expediente.idExpediente=:id")
    List<PamtAntecedentePatologico> findByIdExpediente(@Param("id") UUID id);
}

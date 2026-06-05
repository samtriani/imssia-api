package mx.gob.imss.medgemma.repository;
import mx.gob.imss.medgemma.entity.PamtAntecedenteAlergia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;
@Repository
public interface PamtAntecedenteAlergiaRepository extends JpaRepository<PamtAntecedenteAlergia, Integer> {
    @Query("SELECT a FROM PamtAntecedenteAlergia a WHERE a.expediente.idExpediente=:id")
    List<PamtAntecedenteAlergia> findByIdExpediente(@Param("id") UUID id);
}

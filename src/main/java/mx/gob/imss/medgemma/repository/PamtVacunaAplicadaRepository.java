package mx.gob.imss.medgemma.repository;
import mx.gob.imss.medgemma.entity.PamtVacunaAplicada;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;
@Repository
public interface PamtVacunaAplicadaRepository extends JpaRepository<PamtVacunaAplicada, Integer> {
    @Query("SELECT v FROM PamtVacunaAplicada v WHERE v.expediente.idExpediente=:id ORDER BY v.fecAplicacion DESC")
    List<PamtVacunaAplicada> findByIdExpediente(@Param("id") UUID id);
}

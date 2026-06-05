package mx.gob.imss.medgemma.repository;
import mx.gob.imss.medgemma.entity.PamtConsultaPrescripcion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;
@Repository
public interface PamtConsultaPrescripcionRepository extends JpaRepository<PamtConsultaPrescripcion, Integer> {
    @Query("SELECT p FROM PamtConsultaPrescripcion p WHERE p.consulta.idConsulta=:id AND p.indActivo=true")
    List<PamtConsultaPrescripcion> findByIdConsulta(@Param("id") UUID idConsulta);
}

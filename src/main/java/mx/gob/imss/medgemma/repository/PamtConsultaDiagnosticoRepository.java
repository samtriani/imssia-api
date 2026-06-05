package mx.gob.imss.medgemma.repository;
import mx.gob.imss.medgemma.entity.PamtConsultaDiagnostico;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;
@Repository
public interface PamtConsultaDiagnosticoRepository extends JpaRepository<PamtConsultaDiagnostico, Integer> {
    @Query("SELECT d FROM PamtConsultaDiagnostico d WHERE d.consulta.idConsulta=:id")
    List<PamtConsultaDiagnostico> findByIdConsulta(@Param("id") UUID idConsulta);
}

package mx.gob.imss.medgemma.repository;
import mx.gob.imss.medgemma.entity.PamtConsulta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;
@Repository
public interface PamtConsultaRepository extends JpaRepository<PamtConsulta, UUID> {
    @Query(value="SELECT * FROM imss_ai.pamt_consulta WHERE id_expediente=:id AND ind_activo=true ORDER BY fec_consulta DESC LIMIT :lim", nativeQuery=true)
    List<PamtConsulta> findLastN(@Param("id") UUID idExpediente, @Param("lim") int limit);

    @Query(value="""
            SELECT c.* FROM imss_ai.pamt_consulta c
            JOIN imss_ai.pamt_expediente_clinico e ON e.id_expediente = c.id_expediente
            JOIN imss_ai.pamt_paciente p ON p.id_paciente = e.id_paciente
            WHERE p.cve_curp = :curp AND c.ind_activo = true
            ORDER BY c.fec_consulta DESC LIMIT 20
            """, nativeQuery=true)
    List<PamtConsulta> findByCurp(@Param("curp") String curp);
}

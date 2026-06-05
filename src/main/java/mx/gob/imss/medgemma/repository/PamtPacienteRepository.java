package mx.gob.imss.medgemma.repository;
import mx.gob.imss.medgemma.entity.PamtPaciente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
@Repository
public interface PamtPacienteRepository extends JpaRepository<PamtPaciente, UUID> {
    Optional<PamtPaciente> findByCveCurpAndIndActivoTrue(String cveCurp);
    Optional<PamtPaciente> findByNumNssAndIndActivoTrue(String numNss);
    @Query("SELECT p FROM PamtPaciente p WHERE UPPER(p.nomPrimerApellido) LIKE UPPER(CONCAT('%',:a,'%')) AND p.indActivo=true")
    List<PamtPaciente> findByApellido(@Param("a") String apellido);
}

package mx.gob.imss.medgemma.repository;
import mx.gob.imss.medgemma.entity.PamtRagConsultaLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.UUID;
@Repository
public interface PamtRagConsultaLogRepository extends JpaRepository<PamtRagConsultaLog, UUID> {}

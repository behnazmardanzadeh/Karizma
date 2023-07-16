package clinic.services.repositories;

import clinic.models.IEntity;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

@Repository
public class EntityRepositoryCustomImpl implements EntityRepositoryCustom{

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public void detach(IEntity entity) {
        entityManager.detach(entity);
    }
}

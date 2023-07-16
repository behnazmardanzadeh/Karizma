package clinic.services.repositories;

import clinic.models.IEntity;

public interface EntityRepositoryCustom {
    void detach(IEntity entity);
}

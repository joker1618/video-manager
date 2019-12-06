package xxx.joker.libs.repo.jpa.persistence;

import xxx.joker.libs.repo.config.RepoCtx;
import xxx.joker.libs.repo.design.RepoEntity;

import java.util.Collection;
import java.util.List;

public interface DaoHandler {

    static DaoHandler createHandler(RepoCtx ctx) {
        return new DaoHandlerImpl(ctx);
    }

    List<DaoDTO> readData();

    boolean persistData(Collection<RepoEntity> entities);

    List<DaoDTO> createDTOs(Collection<RepoEntity> entities);

}

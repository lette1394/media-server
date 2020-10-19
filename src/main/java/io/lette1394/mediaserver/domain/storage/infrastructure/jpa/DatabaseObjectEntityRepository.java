package io.lette1394.mediaserver.domain.storage.infrastructure.jpa;

import io.lette1394.mediaserver.domain.storage.infrastructure.jpa.DatabaseStorageObjectEntity.ObjectId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
interface DatabaseObjectEntityRepository extends JpaRepository<DatabaseStorageObjectEntity, ObjectId> {
}

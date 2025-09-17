package org.example.elearningbe.integration.redis;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RedisTokenRepository extends CrudRepository<RedisToken, String> {
}

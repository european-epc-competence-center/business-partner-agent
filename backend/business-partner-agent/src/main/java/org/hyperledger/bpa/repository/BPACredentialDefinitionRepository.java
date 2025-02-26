/*
 * Copyright (c) 2020-2021 - for information on the respective copyright owner
 * see the NOTICE file and/or the repository at
 * https://github.com/hyperledger-labs/business-partner-agent
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.hyperledger.bpa.repository;

import io.micronaut.core.annotation.NonNull;
import io.micronaut.data.annotation.Join;
import io.micronaut.data.jdbc.annotation.JdbcRepository;
import io.micronaut.data.model.query.builder.sql.Dialect;
import io.micronaut.data.repository.CrudRepository;
import org.hyperledger.bpa.model.BPACredentialDefinition;
import org.hyperledger.bpa.model.BPASchema;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@JdbcRepository(dialect = Dialect.POSTGRES)
public interface BPACredentialDefinitionRepository extends CrudRepository<BPACredentialDefinition, UUID> {

    @Join(value = "schema", type = Join.Type.LEFT_FETCH)
    Iterable<BPACredentialDefinition> findAll();

    @Join(value = "schema", type = Join.Type.LEFT_FETCH)
    Optional<BPACredentialDefinition> findById(@NonNull UUID id);

    @Join(value = "schema", type = Join.Type.LEFT_FETCH)
    Optional<BPACredentialDefinition> findByCredentialDefinitionId(@NonNull String credentialDefinitionId);

    @Join(value = "schema", type = Join.Type.LEFT_FETCH)
    List<BPACredentialDefinition> findBySchema(BPASchema schema);
}

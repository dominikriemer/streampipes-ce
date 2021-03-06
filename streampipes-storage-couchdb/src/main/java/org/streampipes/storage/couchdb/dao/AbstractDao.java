/*
 * Copyright 2018 FZI Forschungszentrum Informatik
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package org.streampipes.storage.couchdb.dao;

import org.lightcouch.CouchDbClient;

import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

public class AbstractDao<T> {

  protected Supplier<CouchDbClient> couchDbClientSupplier;
  protected Class<T> clazz;

  public AbstractDao(Supplier<CouchDbClient> couchDbClientSupplier, Class<T> clazz) {
    this.couchDbClientSupplier = couchDbClientSupplier;
    this.clazz = clazz;
  }

  public Boolean persist(T objToPersist) {
    DbCommand<Boolean, T> cmd = new PersistCommand<>(couchDbClientSupplier, objToPersist, clazz);
    return cmd.execute();
  }

  public Boolean delete(String key) {
    DbCommand<Boolean, T> cmd = new DeleteCommand<>(couchDbClientSupplier, key, clazz);
    return cmd.execute();
  }

  public Boolean update(T objToUpdate) {
    DbCommand<Boolean, T> cmd = new UpdateCommand<>(couchDbClientSupplier, objToUpdate, clazz);
    return cmd.execute();
  }

  public Optional<T> find(String id) {
    DbCommand<Optional<T>, T> cmd = new FindCommand<>(couchDbClientSupplier, id, clazz);
    return cmd.execute();
  }

  public List<T> findAll() {
    DbCommand<List<T>, T> cmd = new FindAllCommand<>(couchDbClientSupplier, clazz);
    return cmd.execute();
  }

  public T findWithNullIfEmpty(String id) {
    DbCommand<Optional<T>, T> cmd = new FindCommand<>(couchDbClientSupplier, id, clazz);
    return cmd.execute().get();
  }


}

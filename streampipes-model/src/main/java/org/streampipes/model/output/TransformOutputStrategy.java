/*
Copyright 2018 FZI Forschungszentrum Informatik

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/
package org.streampipes.model.output;

import org.streampipes.empire.annotations.RdfProperty;
import org.streampipes.empire.annotations.RdfsClass;
import org.streampipes.model.util.Cloner;
import org.streampipes.vocabulary.StreamPipes;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;

@RdfsClass(StreamPipes.TRANSFORM_OUTPUT_STRATEGY)
@Entity
public class TransformOutputStrategy extends OutputStrategy {

  @OneToMany(fetch = FetchType.EAGER,
          cascade = {CascadeType.ALL})
  @RdfProperty(StreamPipes.HAS_TRANSFORM_OPERATION)
  private List<TransformOperation> transformOperations;

  public TransformOutputStrategy() {
    super();
    this.transformOperations = new ArrayList<>();
  }

  public TransformOutputStrategy(TransformOutputStrategy other) {
    super(other);
    this.setTransformOperations(new Cloner().transformOperations(other.getTransformOperations()));
  }

  public List<TransformOperation> getTransformOperations() {
    return transformOperations;
  }

  public void setTransformOperations(List<TransformOperation> transformOperations) {
    this.transformOperations = transformOperations;
  }
}

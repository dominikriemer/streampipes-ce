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

package org.streampipes.wrapper.runtime;

import org.streampipes.commons.exceptions.SpRuntimeException;
import org.streampipes.model.base.InvocableStreamPipesEntity;
import org.streampipes.wrapper.params.binding.BindingParams;

import java.util.Map;

public abstract class PipelineElement<B extends BindingParams> {

  InvocableStreamPipesEntity graph;

  public PipelineElement(B params) {
    this.graph = params.getGraph();
  }

  public InvocableStreamPipesEntity getGraph() {
    return this.graph;
  }

  public abstract void onEvent(Map<String, Object> event, String sourceInfo);

  public abstract void discard() throws SpRuntimeException;
}

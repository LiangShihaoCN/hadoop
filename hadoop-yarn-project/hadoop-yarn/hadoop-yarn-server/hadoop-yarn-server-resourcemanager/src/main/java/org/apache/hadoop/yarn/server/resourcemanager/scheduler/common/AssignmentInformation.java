/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.hadoop.yarn.server.resourcemanager.scheduler.common;

import java.util.ArrayList;
import java.util.List;
import org.apache.hadoop.classification.InterfaceAudience;
import org.apache.hadoop.classification.InterfaceStability;
import org.apache.hadoop.yarn.api.records.ContainerId;
import org.apache.hadoop.yarn.api.records.Resource;

@InterfaceAudience.Private
@InterfaceStability.Unstable
public class AssignmentInformation {

  public enum Operation {
    ALLOCATION, RESERVATION;
    private static int SIZE = Operation.values().length;
    static int size() {
      return SIZE;
    }
  }

  public static class AssignmentDetails {
    public ContainerId containerId;
    public String queue;

    public AssignmentDetails(ContainerId containerId, String queue) {
      this.containerId = containerId;
      this.queue = queue;
    }
  }

  private final int[] operationCounts;
  private final Resource[] operationResources;
  private final List<AssignmentDetails>[] operationDetails;

  @SuppressWarnings("unchecked")
  public AssignmentInformation() {
    int numOps = Operation.size();
    this.operationCounts = new int[numOps];
    this.operationResources = new Resource[numOps];
    this.operationDetails = new List[numOps];
    for (int i=0; i < numOps; i++) {
      operationCounts[i] = 0;
      operationResources[i] = Resource.newInstance(0, 0);
      operationDetails[i] = new ArrayList<AssignmentDetails>();
    }
  }

  public int getNumAllocations() {
    return operationCounts[Operation.ALLOCATION.ordinal()];
  }

  public void incrAllocations() {
    increment(Operation.ALLOCATION, 1);
  }

  public void incrAllocations(int by) {
    increment(Operation.ALLOCATION, by);
  }

  public int getNumReservations() {
    return operationCounts[Operation.RESERVATION.ordinal()];
  }

  public void incrReservations() {
    increment(Operation.RESERVATION, 1);
  }

  public void incrReservations(int by) {
    increment(Operation.RESERVATION, by);
  }

  private void increment(Operation op, int by) {
    operationCounts[op.ordinal()] += by;
  }

  public Resource getAllocated() {
    return operationResources[Operation.ALLOCATION.ordinal()];
  }

  public Resource getReserved() {
    return operationResources[Operation.RESERVATION.ordinal()];
  }

  private void addAssignmentDetails(Operation op, ContainerId containerId,
      String queue) {
    getDetails(op).add(new AssignmentDetails(containerId, queue));
  }

  public void addAllocationDetails(ContainerId containerId, String queue) {
    addAssignmentDetails(Operation.ALLOCATION, containerId, queue);
  }

  public void addReservationDetails(ContainerId containerId, String queue) {
    addAssignmentDetails(Operation.RESERVATION, containerId, queue);
  }

  private List<AssignmentDetails> getDetails(Operation op) {
    return operationDetails[op.ordinal()];
  }

  public List<AssignmentDetails> getAllocationDetails() {
    return getDetails(Operation.ALLOCATION);
  }

  public List<AssignmentDetails> getReservationDetails() {
    return getDetails(Operation.RESERVATION);
  }

  private ContainerId getFirstContainerIdFromOperation(Operation op) {
    List<AssignmentDetails> assignDetails = getDetails(op);
    if (assignDetails != null && !assignDetails.isEmpty()) {
      return assignDetails.get(0).containerId;
    }
    return null;
  }

  public ContainerId getFirstAllocatedOrReservedContainerId() {
    ContainerId containerId;
    containerId = getFirstContainerIdFromOperation(Operation.ALLOCATION);
    if (null != containerId) {
      return containerId;
    }
    return getFirstContainerIdFromOperation(Operation.RESERVATION);
  }
}

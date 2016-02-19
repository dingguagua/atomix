/*
 * Copyright 2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License
 */
package io.atomix;

import io.atomix.catalyst.transport.Address;
import io.atomix.messaging.DistributedMessageBus;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Atomix message bus test.
 *
 * @author <a href="http://github.com/kuujo>Jordan Halterman</a>
 */
@Test
public class AtomixMessageBusTest extends AbstractAtomixTest {
  @BeforeClass
  protected void setupCluster() throws Throwable {
    createReplicas(3, 3, 0);
  }
  
  public void testClientMessageBusGet() throws Throwable {
    testMessageBus(
      createClient().getMessageBus("test-client-bus-get", DistributedMessageBus.options().withAddress(new Address("localhost", 6000))).get(),
      createClient().getMessageBus("test-client-bus-get", DistributedMessageBus.options().withAddress(new Address("localhost", 6001))).get()
    );
  }

  public void testReplicaMessageBusGet() throws Throwable {
    testMessageBus(
      replicas.get(0).getMessageBus("test-client-bus-get", DistributedMessageBus.options().withAddress(new Address("localhost", 6002))).get(),
      replicas.get(1).getMessageBus("test-client-bus-get", DistributedMessageBus.options().withAddress(new Address("localhost", 6003))).get()
    );
  }

  /**
   * Tests sending and receiving messages on a message bus.
   */
  private void testMessageBus(DistributedMessageBus bus1, DistributedMessageBus bus2) throws Throwable {
    bus1.<String>consumer("test", message -> {
      threadAssertEquals(message, "Hello world!");
      resume();
      return null;
    }).thenRun(() -> {
      bus2.producer("test").thenAccept(producer -> {
        producer.send("Hello world!");
      });
    });

    await(10000);
  }

}

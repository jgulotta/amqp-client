package com.github.sstone.amqp

import akka.testkit.TestProbe
import com.github.sstone.amqp.Amqp.{Binding, Delivery, Publish, _}
import com.rabbitmq.client.AMQP.BasicProperties
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

import scala.concurrent.duration._

@RunWith(classOf[JUnitRunner])
class ProducerSpec extends ChannelSpec {
  "Producers" should {
    "be able to specify custom message properties" in {
      val exchange = StandardExchanges.amqDirect
      val queue = randomQueue
      val routingKey = randomKey
      val probe = TestProbe()
      val consumer = ConnectionOwner.createChildActor(conn, Consumer.props(listener = Some(probe.ref)), timeout = 5000.millis, name = Some("ProducerSpec.consumer"))
      val producer = ConnectionOwner.createChildActor(conn, ChannelOwner.props(), timeout = 5000.millis, name = Some("ProducerSpec.producer"))
      waitForConnection(system, conn, consumer, producer).await()

      // create a queue, bind it to "my_key" and consume from it
      consumer ! AddBinding(Binding(exchange, queue, Set(routingKey)))

      fishForMessage(1.second) {
        case Amqp.Ok(AddBinding(Binding(`exchange`, `queue`, routingKeys)), _) if routingKeys == Set(routingKey) => true
        case msg => {
          println(s"unexpected $msg")
          false
        }
      }

      val message = "yo!".getBytes
      producer ! Publish(exchange.name, routingKey, message, Some(new BasicProperties.Builder().contentType("my content").build()))

      val delivery = probe.receiveOne(1.second).asInstanceOf[Delivery]
      assert(delivery.properties.getContentType === "my content")
    }
    "publish messages within an AMQP transaction" in  {
      val exchange = StandardExchanges.amqDirect
      val queue = randomQueue
      val routingKey = randomKey
      val probe = TestProbe()
      val consumer = ConnectionOwner.createChildActor(conn, Consumer.props(listener = Some(probe.ref)), timeout = 5000.millis)
      val producer = ConnectionOwner.createChildActor(conn, ChannelOwner.props())
      waitForConnection(system, conn, consumer, producer).await()

      // create a queue, bind it to our routing key and consume from it
      consumer ! AddBinding(Binding(exchange, queue, Set(routingKey)))

      fishForMessage(1.second) {
        case Amqp.Ok(AddBinding(Binding(`exchange`, `queue`, _)), _) => true
        case _ => false
      }

      val message = "yo!".getBytes
      val props = new BasicProperties.Builder().contentType("my content").contentEncoding("my encoding").build()
      producer ! Transaction(
        List(
          Publish(exchange.name, routingKey, message, properties = Some(props)),
          Publish(exchange.name, routingKey, message, properties = Some(props)),
          Publish(exchange.name, routingKey, message, properties = Some(props))))

      var received = List[Delivery]()
      probe.receiveWhile(2.seconds) {
        case message: Delivery => received = message :: received
      }
      assert(received.length === 3)
      received.foreach(m => {
        assert(m.properties.getContentEncoding === "my encoding")
        assert(m.properties.getContentType === "my content")
      })
    }
  }
}

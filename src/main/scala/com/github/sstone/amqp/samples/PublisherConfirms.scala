package com.github.sstone.amqp.samples

import akka.actor.{Actor, ActorSystem, Props}
import com.github.sstone.amqp.Amqp._
import com.github.sstone.amqp.{Amqp, ChannelOwner, ConnectionOwner}
import com.rabbitmq.client.ConnectionFactory

import scala.concurrent.duration._

object PublisherConfirms extends App {

  implicit val system = ActorSystem("mySystem")

  // create an AMQP connection
  val connFactory = new ConnectionFactory()
  connFactory.setUri("amqp://guest:guest@localhost/%2F")
  val conn = system.actorOf(ConnectionOwner.props(connFactory, 1.second))

  val producer = ConnectionOwner.createChildActor(conn, ChannelOwner.props())
  Amqp.waitForConnection(system, producer).await()

  class Foo extends Actor {

    producer ! ConfirmSelect
    producer ! AddReturnListener(self)
    producer ! AddConfirmListener(self)
    producer ! DeclareQueue(QueueParameters(name = "my_queue", passive = false, durable = false, autodelete = true))

    def receive = {
      case "publishing_fails" => {
        producer ! Publish("", "no_such_queue", "yo!".getBytes)
        producer ! Publish("", "no_such_queue", "yo!".getBytes)
        producer ! WaitForConfirms(None)
      }
      case "publishing_works" => {
        producer ! Publish("", "my_queue", "yo!".getBytes)
        producer ! Publish("", "my_queue", "yo!".getBytes)
        producer ! WaitForConfirms(None)
      }
      case msg => println(msg)
    }
  }

  val foo = system.actorOf(Props(new Foo))
  foo ! "publishing_fails"
  Thread.sleep(1000)
  foo ! "publishing_works"
}

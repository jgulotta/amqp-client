package com.github.sstone.amqp.samples

import akka.actor.ActorSystem
import akka.pattern.ask
import akka.util.Timeout
import com.github.sstone.amqp.Amqp.{ChannelParameters, Delivery, Publish, QueueParameters, _}
import com.github.sstone.amqp.RpcClient.Request
import com.github.sstone.amqp.RpcServer.{IProcessor, ProcessResult}
import com.github.sstone.amqp._
import com.rabbitmq.client.ConnectionFactory

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

object OneToAnyRpc extends App {
  import ExecutionContext.Implicits.global

  // typical "work queue" pattern, where a job can be picked up by any running node
  implicit val system = ActorSystem("mySystem")

  // create an AMQP connection
  val connFactory = new ConnectionFactory()
  connFactory.setUri("amqp://guest:guest@localhost/%2F")
  val conn = system.actorOf(ConnectionOwner.props(connFactory, 1.second))

  val queueParams = QueueParameters("my_queue", passive = false, durable = false, exclusive = false, autodelete = true)

  // create 2 equivalent servers
  val rpcServers = for (_ <- 1 to 2) yield {
    // create a "processor"
    // in real life you would use a serialization framework (json, protobuf, ....), define command messages, etc...
    // check the Akka AMQP proxies project for examples
    val processor = new IProcessor {
      def process(delivery: Delivery) = {
        // assume that the message body is a string
        val response = "response to " + new String(delivery.body)
        Future(ProcessResult(Some(response.getBytes)))
      }
      def onFailure(delivery: Delivery, e: Throwable) = ProcessResult(None) // we don't return anything
    }
    ConnectionOwner.createChildActor(conn, RpcServer.props(queueParams, StandardExchanges.amqDirect,  "my_key", processor, ChannelParameters(qos = 1)))
  }

  val rpcClient = ConnectionOwner.createChildActor(conn, RpcClient.props())

  // wait till everyone is actually connected to the broker
  Amqp.waitForConnection(system, rpcServers: _*).await()
  Amqp.waitForConnection(system, rpcClient).await()

  implicit val timeout: Timeout = 2.seconds

  for (i <- 0 to 5) {
    val request = ("request " + i).getBytes
    val f = (rpcClient ? Request(List(Publish("amq.direct", "my_key", request)))).mapTo[RpcClient.Response]
    f.onComplete {
      case Success(response) => println(new String(response.deliveries.head.body))
      case Failure(error) => println(error)
    }
  }
  // wait 10 seconds and shut down
  Thread.sleep(10000)
  system.terminate()
}

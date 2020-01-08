package com.github.sstone.amqp

import scala.concurrent.ExecutionContext
import akka.actor.ActorSystem
import akka.pattern.ask
import akka.util.Timeout
import com.github.sstone.amqp.RpcClient.Request
import com.github.sstone.amqp.Amqp.Publish
import scala.concurrent.duration._
import com.rabbitmq.client.ConnectionFactory

/**
 * Created by fabrice on 31/12/13.
 */
object Test2 extends App {
  import ExecutionContext.Implicits.global

  implicit val system = ActorSystem("mySystem")
  implicit val timeout: Timeout = 1.second
  // create an AMQP connection
  val conn = system.actorOf(ConnectionOwner.props(new ConnectionFactory(), reconnectionDelay = 5.seconds), "connection")

  val client = ConnectionOwner.createChildActor(conn, RpcClient.props(), Some("RpcClient"))

  // send 1 request every.second
  while(true) {
    println("sending request")
    (client ? Request(Publish("amq.direct", "my_key", "test".getBytes("UTF-8")))).mapTo[RpcClient.Response].map(response => {
      // we expect 1 delivery
      val delivery = response.deliveries.head
      println("reponse : " + new String(delivery.body))
    })
    Thread.sleep(1000)
  }

}

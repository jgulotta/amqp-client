package com.github.sstone.amqp

import java.util
import java.util.concurrent.TimeUnit

import akka.actor.ActorSystem
import akka.pattern.gracefulStop
import akka.testkit.{ImplicitSender, TestKit, TestProbe}
import akka.util.Timeout
import com.github.sstone.amqp.Amqp._
import com.github.sstone.amqp.ConnectionOwner.{Connected, CreateChannel, Disconnected}
import com.rabbitmq.client.{Address, Channel, ConnectionFactory, ListAddressResolver}
import org.junit.runner.RunWith
import org.scalatest.{Matchers, WordSpecLike}
import org.scalatestplus.junit.JUnitRunner

import scala.concurrent.Await
import scala.concurrent.duration._

@RunWith(classOf[JUnitRunner])
class ConnectionOwnerSpec extends TestKit(ActorSystem("TestSystem")) with WordSpecLike with Matchers with ImplicitSender {
  implicit val timeout = Timeout(5.seconds)

  "ConnectionOwner" should {
    "provide channels for many child actors" in {
      val connFactory = new ConnectionFactory()
      val uri = system.settings.config.getString("amqp-client-test.rabbitmq.uri")
      connFactory.setUri(uri)
      val conn = system.actorOf(ConnectionOwner.props(connFactory))
      Amqp.waitForConnection(system, conn).await(2, TimeUnit.SECONDS)
      val actors = 100
      for (_ <- 0 until actors) {
        val p = TestProbe()
        p.send(conn, CreateChannel)
        p.expectMsgClass(2.second, classOf[Channel])
      }
      Await.result(gracefulStop(conn, 5.seconds), 6.seconds)
    }
    "connect even if the default host is unavailable" in {
      val connFactory = new ConnectionFactory()
      val uri = system.settings.config.getString("amqp-client-test.rabbitmq.uri")
      connFactory.setUri(uri)
      val goodHost = connFactory.getHost
      connFactory.setHost("fake-host")
      val conn = system.actorOf(ConnectionOwner.props(connFactory, addressResolver = Some(new ListAddressResolver({
        val l = new util.ArrayList[Address]()
        l.add(new Address("another.fake.host"))
        l.add(new Address(goodHost))
        l
      }))))
      Amqp.waitForConnection(system, conn).await(50, TimeUnit.SECONDS)
      val actors = 100
      for (_ <- 0 until actors) {
        val p = TestProbe()
        p.send(conn, CreateChannel)
        p.expectMsgClass(2.second, classOf[Channel])
      }
      Await.result(gracefulStop(conn, 5.seconds), 6.seconds)
    }
    "send Connected/Disconnected status messages" in {
      val connFactory = new ConnectionFactory()
      val uri = system.settings.config.getString("amqp-client-test.rabbitmq.uri")
      connFactory.setUri(uri)
      val probe = TestProbe()
      val conn = system.actorOf(ConnectionOwner.props(connFactory))
      conn ! AddStatusListener(probe.ref)
      probe.expectMsg(2.seconds, Connected)
      conn ! Abort()
      probe.expectMsg(2.seconds, Disconnected)
    }
  }
}

/*
 * Copyright (C) 2017-2022 Lightbend Inc. <https://www.lightbend.com>
 */

package org.apache.pekko.discovery

import java.util.concurrent.ConcurrentHashMap
import java.util.function.{ Function => JFunction }

import scala.util.{ Failure, Success, Try }

import org.apache.pekko
import pekko.actor._
import pekko.annotation.InternalApi

final class Discovery(implicit system: ExtendedActorSystem) extends Extension {

  Discovery.checkClassPathForOldDiscovery(system)

  private val implementations = new ConcurrentHashMap[String, ServiceDiscovery]
  private val factory = new JFunction[String, ServiceDiscovery] {
    override def apply(method: String): ServiceDiscovery = createServiceDiscovery(method)
  }

  private lazy val _defaultImplMethod =
    system.settings.config.getString("pekko.discovery.method") match {
      case "<method>" =>
        throw new IllegalArgumentException(
          "No default service discovery implementation configured in " +
          "`pekko.discovery.method`. Make sure to configure this setting to your preferred implementation such as " +
          "'pekko-dns' in your application.conf (from the akka-discovery module).")
      case method => method
    }

  private lazy val defaultImpl = loadServiceDiscovery(_defaultImplMethod)

  /**
   * Default [[ServiceDiscovery]] as configured in `pekko.discovery.method`.
   */
  @throws[IllegalArgumentException]
  def discovery: ServiceDiscovery = defaultImpl

  /**
   * Create a [[ServiceDiscovery]] from configuration property.
   * The given `method` parameter is used to find configuration property
   * "pekko.discovery.[method].class".
   *
   * The `ServiceDiscovery` instance for a given `method` will be created
   * once and subsequent requests for the same `method` will return the same instance.
   */
  def loadServiceDiscovery(method: String): ServiceDiscovery = {
    implementations.computeIfAbsent(method, factory)
  }

  /**
   * INTERNAL API
   */
  @InternalApi
  private def createServiceDiscovery(method: String): ServiceDiscovery = {
    val config = system.settings.config
    val dynamic = system.dynamicAccess

    def classNameFromConfig(path: String): String = {
      if (config.hasPath(path))
        config.getString(path)
      else
        throw new IllegalArgumentException(
          s"$path must point to a FQN of a `org.apache.pekko.discovery.ServiceDiscovery` implementation")
    }

    def create(clazzName: String): Try[ServiceDiscovery] = {
      dynamic
        .createInstanceFor[ServiceDiscovery](clazzName, (classOf[ExtendedActorSystem] -> system) :: Nil)
        .recoverWith {
          case _: ClassNotFoundException | _: NoSuchMethodException =>
            dynamic.createInstanceFor[ServiceDiscovery](clazzName, (classOf[ActorSystem] -> system) :: Nil)
        }
        .recoverWith {
          case _: ClassNotFoundException | _: NoSuchMethodException =>
            dynamic.createInstanceFor[ServiceDiscovery](clazzName, Nil)
        }
    }

    val configName = s"pekko.discovery.$method.class"
    val instanceTry = create(classNameFromConfig(configName))

    instanceTry match {
      case Failure(e @ (_: ClassNotFoundException | _: NoSuchMethodException)) =>
        throw new IllegalArgumentException(
          s"Illegal [$configName] value or incompatible class! " +
          "The implementation class MUST extend org.apache.pekko.discovery.ServiceDiscovery and take an " +
          "ExtendedActorSystem as constructor argument.",
          e)
      case Failure(e)        => throw e
      case Success(instance) => instance
    }

  }

}

object Discovery extends ExtensionId[Discovery] with ExtensionIdProvider {
  override def apply(system: ActorSystem): Discovery = super.apply(system)

  override def lookup: Discovery.type = Discovery

  override def get(system: ActorSystem): Discovery = super.get(system)

  override def get(system: ClassicActorSystemProvider): Discovery = super.get(system)

  override def createExtension(system: ExtendedActorSystem): Discovery = new Discovery()(system)

  /**
   * INTERNAL API
   */
  @InternalApi
  private[pekko] def checkClassPathForOldDiscovery(system: ExtendedActorSystem): Unit = {
    try {
      system.dynamicAccess.getClassFor[Any]("org.apache.pekko.discovery.SimpleServiceDiscovery").get
      throw new RuntimeException(
        "Old version of Akka Discovery from Akka Management found on the classpath. Remove `com.lightbend.pekko.discovery:akka-discovery` from the classpath..")
    } catch {
      case _: ClassCastException =>
        throw new RuntimeException(
          "Old version of Akka Discovery from Akka Management found on the classpath. Remove `com.lightbend.pekko.discovery:akka-discovery` from the classpath..")
      case _: ClassNotFoundException =>
      // all good
    }
  }

}

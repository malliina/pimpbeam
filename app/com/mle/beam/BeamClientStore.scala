package com.mle.beam

import com.mle.util.Log
import play.api.libs.json.JsValue

import scala.collection.concurrent.TrieMap

/**
 *
 * @author mle
 */
trait BeamClientStore[U <: BeamClient] extends ClientStore[U, JsValue] with Log {
  private val clientsMap = TrieMap.empty[String, U]

  def clients: Seq[U] = clientsMap.values.toSeq

  private def clientCount = clientsMap.size

  def add(name: String, client: U): Unit = {
    clientsMap += (name -> client)
    log info s"Added client: $name. Clients in total: $clientCount."
  }

  def remove(name: String): Unit = {
    clientsMap -= name
    log info s"Removed client: $name. Clients in total: $clientCount."
  }

  def client(name: String): Option[U] = clientsMap get name

  def getClient(name: String): U = client(name)
    .getOrElse(throw new NoSuchElementException(s"Unable to find client: $name"))

//  def broadcast(msg: JsValue): Unit =
//    clientsMap.valuesIterator.foreach(_.channel push msg)

  def usercast(user: String, msg: JsValue): Unit =
    client(user).foreach(_.channel push msg)
}

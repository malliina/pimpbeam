package com.malliina.beam

import com.malliina.beam.BeamClientStore.log
import com.malliina.play.models.Username
import play.api.Logger
import play.api.libs.json.JsValue

import scala.collection.concurrent.TrieMap
import scala.concurrent.{ExecutionContext, Future}

object BeamClientStore {
  private val log = Logger(getClass)
}

class BeamClientStore[U <: BeamClient]()(implicit ec: ExecutionContext) extends ClientStore[U, JsValue] {
  private val clientsMap = TrieMap.empty[Username, U]

  def clients: Seq[U] = clientsMap.values.toSeq

  private def clientCount = clientsMap.size

  def add(name: Username, client: U): Unit = {
    clientsMap += (name -> client)
    log info s"Added client: $name. Clients in total: $clientCount."
  }

  def remove(name: Username): Unit = {
    clientsMap -= name
    log info s"Removed client: $name. Clients in total: $clientCount."
  }

  def client(name: Username): Option[U] = clientsMap get name

  def getClient(name: Username): U = client(name)
    .getOrElse(throw new NoSuchElementException(s"Unable to find client '$name'."))

  def usercast(user: Username, msg: JsValue): Unit =
    client(user).foreach(_.channel offer msg)

  override def broadcast(msg: JsValue): Unit =
    Future.traverse(clients)(_.channel.offer(msg))
}

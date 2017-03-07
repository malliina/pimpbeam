package com.malliina.beam

import com.malliina.play.models.Username

/**
  * @tparam T type of client
  * @tparam U type of message
  */
trait ClientStore[T, U] {
  def add(name: Username, client: T): Unit

  def remove(name: Username): Unit

  def client(name: Username): Option[T]

  def broadcast(msg: U): Unit

  def usercast(user: Username, msg: U): Unit
}

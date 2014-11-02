package com.mle.beam


/**
 *
 * @author mle
 */
/**
 *
 * @tparam T type of client
 * @tparam U type of message
 */
trait ClientStore[T, U] {

  def add(name: String, client: T): Unit

  def remove(name: String): Unit

  def client(name: String): Option[T]

  def broadcast(msg: U): Unit

  def usercast(user: String, msg: U): Unit
}

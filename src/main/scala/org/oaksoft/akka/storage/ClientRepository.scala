package org.oaksoft.akka.storage

import org.oaksoft.akka.domain.Client

class ClientRepository {

  private val clients = Vector[Client] (
    Client(1L, "Juan Manuel", "Carballo"),
    Client(2L, "Carla Vivani", "Carballo")
  )

  def findOne(id: Long): Option[Client] = this.clients.find(client => client.clientId == id)
}

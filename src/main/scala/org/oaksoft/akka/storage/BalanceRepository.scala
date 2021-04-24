package org.oaksoft.akka.storage

import org.oaksoft.akka.domain.Balance

class BalanceRepository {

  private var balances = Vector[Balance]()

  def save(balance: Balance): Unit =
    balances = balances :+ balance

  def findAll(accountNumber: String): Vector[Balance] =
    this.balances.filter(balance => balance.accountNumber eq accountNumber)
}

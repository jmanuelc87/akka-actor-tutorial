package org.oaksoft.akka.actors

import akka.actor.Actor
import org.oaksoft.akka.domain.Types.Money
import org.oaksoft.akka.domain.{Account, Balance}
import org.oaksoft.akka.messages.{AccountBalance, Deposit, Withdraw}
import org.oaksoft.akka.storage.{AccountRepository, BalanceRepository, ClientRepository}

import java.time.LocalDateTime

class ATMActor extends Actor {
  val accountRepository = new AccountRepository()
  val balanceRepository = new BalanceRepository()
  val clientRepository = new ClientRepository()

  override def receive: Receive = {
    case Deposit(clientId, accountNumber, amount) =>
      this.calculate(clientId, accountNumber, amount, (current: Money, actual: Money) => current + actual)

    case Withdraw(clientId, accountNumber, amount) =>
      this.calculate(clientId, accountNumber, amount, (current: Money, actual: Money) => current - actual)

    case AccountBalance(clientId, accountNumber) =>
      val printed = printBalance(clientId, accountNumber)
      println(printed)
  }

  def calculate(clientId: Long, accountNumber: String, amount: Money, op: (Money, Money) => Money): Unit = {
    val client = clientRepository.findOne(clientId)

    if (client.isDefined) {
      val account = accountRepository.findOne(client.get.clientId, accountNumber)

      if (account.isDefined) {
        val updatedAmount = op(account.get.currentMoney, amount)
        val updatedAccount = Account(account.get.reference, account.get.name, account.get.accountNumber, account.get.accountType, updatedAmount, account.get.currency, account.get.clientId)
        accountRepository.update(updatedAccount)
        val balance = Balance(LocalDateTime.now(), accountRepository.findOne(clientId, accountNumber).get.currentMoney, amount, account.get.accountNumber)
        balanceRepository.save(balance)
      }
    }
  }

  def printBalance(clientId: Long, accountNumber: String): String = {
    val client = clientRepository.findOne(clientId)
    val buffer: StringBuilder = new StringBuilder()

    if (client.isDefined) {
      val account = accountRepository.findOne(client.get.clientId, accountNumber)
      val balances = balanceRepository.findAll(account.get.accountNumber)


      buffer.append(s"Client: ${client.get.firstName} ${client.get.lastName}\n")
      buffer.append(s"Account number: ${account.get.accountNumber}\t\tAmount: ${account.get.currentMoney} ${account.get.currency.getCurrencyCode}\n\n")
      balances.map {
        case Balance(timestamp, current, amount, accountNumber) =>
          buffer.append(s"$timestamp  $current  $amount\n")
      }
    }

    buffer.toString()
  }
}

package org.oaksoft.akka.domain

import org.oaksoft.akka.domain.Types.Money

import java.time.LocalDateTime
import java.util.Currency

object Types {
  type Money = BigDecimal
}

case class Client(clientId: Long, firstName: String, lastName: String)

case class Account(reference: String, name: String, accountNumber: String, accountType: AccountType, currentMoney: Money, currency: Currency, clientId: Long)

case class Balance(timestamp: LocalDateTime, current: Money, amount: Money, accountNumber: String)

sealed trait AccountType

case class DepositAccount() extends AccountType

case class CreditAccount() extends AccountType
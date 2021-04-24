package org.oaksoft.akka.messages

import org.oaksoft.akka.domain.Types.Money

case class Withdraw(clientId: Long, accountNumber: String, amount: Money)

case class Deposit(clientId: Long, accountNumber: String, amount: Money)

case class Success(clientId: Long, message: String)

case class Failure(message: String, error: Error)

case class AccountBalance(clientId: Long, accountNumber: String)
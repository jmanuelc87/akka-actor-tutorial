package org.oaksoft.akka.storage

import org.oaksoft.akka.domain.{Account, DepositAccount}

import java.util.{Currency, Locale}

class AccountRepository {

  private var accounts = Vector[Account](
    Account("AAA", "MY ACCOUNT", "12345", DepositAccount(), 1000.0, Currency.getInstance(Locale.US), 1L),
    Account("AAA", "MY ACCOUNT", "12346", DepositAccount(), 1000.0, Currency.getInstance(Locale.US), 2L)
  )

  def findOne(clientId: Long, accountNumber: String): Option[Account] =
    this.accounts.find(account => (account.clientId == clientId) && (account.accountNumber eq accountNumber))

  def update(account: Account): Option[Account] = {
    val temp = this.findOne(account.clientId, account.accountNumber)
    val index = this.accounts.indexOf(temp.get)
    this.accounts = this.accounts.updated(index, account)
    temp
  }
}

package com.atm.ATMSystem.service;

import com.atm.ATMSystem.model.Transaction;
import com.atm.ATMSystem.model.User;
import com.atm.ATMSystem.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class TransactionService {

    @Autowired
    private TransactionRepository transactionRepository;

    public Transaction save(Transaction tx) {
        return transactionRepository.save(tx);
    }

    public List<Transaction> findByUserId(Long userId) {
        return transactionRepository.findByUserIdOrderByTimestampDesc(userId);
    }

    public Transaction getTransactionById(Long id) {
        return transactionRepository.findById(id).orElse(null);
    }

    /** Withdraw Transaction */
    public void createWithdrawTransaction(User user, double amount) {
        Transaction tx = new Transaction();
        tx.setUserId(user.getId());                  // ✅ userId instead of setUser
        tx.setType("WITHDRAW");
        tx.setAmount(amount);
        tx.setDescription("Cash withdrawal");
        tx.setTimestamp(LocalDateTime.now());
        transactionRepository.save(tx);
    }

    /** Deposit Transaction */
    public void createDepositTransaction(User user, double amount) {
        Transaction tx = new Transaction();
        tx.setUserId(user.getId());
        tx.setType("DEPOSIT");
        tx.setAmount(amount);
        tx.setDescription("Cash deposit");
        tx.setTimestamp(LocalDateTime.now());
        transactionRepository.save(tx);
    }

    /** Transfer Transaction */
    public void createTransferTransaction(User from, User to, double amount) {
        // Debit (sender)
        Transaction out = new Transaction();
        out.setUserId(from.getId());
        out.setType("TRANSFER_SENT");
        out.setAmount(amount);
        out.setDescription("Transfer to " + to.getEmail());
        out.setTimestamp(LocalDateTime.now());
        transactionRepository.save(out);

        // Credit (receiver)
        Transaction in = new Transaction();
        in.setUserId(to.getId());
        in.setType("TRANSFER_RECEIVED");
        in.setAmount(amount);
        in.setDescription("Received from " + from.getEmail());
        in.setTimestamp(LocalDateTime.now());
        transactionRepository.save(in);
    }
}

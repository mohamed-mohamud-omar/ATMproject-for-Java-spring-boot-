package com.atm.ATMSystem.controller;

import com.atm.ATMSystem.model.Transaction;
import com.atm.ATMSystem.model.User;
import com.atm.ATMSystem.service.TransactionService;
import com.atm.ATMSystem.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class TransactionController {

    @Autowired
    private UserService userService;

    @Autowired
    private TransactionService transactionService;

    // ✅ Withdraw GET
    @GetMapping("/withdraw")
    public String showWithdrawForm() {
        return "withdraw";
    }

    // ✅ Withdraw POST (PIN check + balance check)
    @PostMapping("/withdraw")
    public String processWithdraw(
            @RequestParam double amount,
            @RequestParam String pin,
            Authentication authentication,
            Model model) {

        String email = authentication.getName();
        User user = userService.findByEmail(email).orElse(null);

        if (user == null) {
            model.addAttribute("error", "User not found or not logged in!");
            return "withdraw";
        }

        if (!userService.isPinCorrect(user, pin)) {
            model.addAttribute("error", "Incorrect PIN! Try again.");
            return "withdraw";
        }

        if (amount <= 0) {
            model.addAttribute("error", "Amount must be greater than 0.");
            return "withdraw";
        }

        if (user.getBalance() < amount) {
            model.addAttribute("error", "Insufficient balance!");
            return "withdraw";
        }

        // Update balance
        user.setBalance(user.getBalance() - amount);
        userService.updateUser(user);

        // Save transaction
        transactionService.createWithdrawTransaction(user, amount);

        model.addAttribute("message", "Successfully withdrawn $" + amount);
        return "withdraw";
    }

    // ✅ Balance View
    @GetMapping("/balance")
    public String showBalance(Model model, Authentication authentication) {
        String email = authentication.getName();
        User user = userService.findByEmail(email).orElse(null);

        if (user != null) {
            model.addAttribute("balance", user.getBalance());
        }

        return "balance";
    }

    // ✅ Mini Statement
    @GetMapping("/ministatement")
    public String miniStatement(Model model, Authentication authentication) {
        String email = authentication.getName();
        User user = userService.findByEmail(email).orElse(null);

        if (user != null) {
            var allTx = transactionService.findByUserId(user.getId());
            var latest5 = allTx.stream().limit(5).toList();
            model.addAttribute("transactions", latest5);
        }

        return "ministatement";
    }

    // ✅ Deposit GET
    @GetMapping("/deposit")
    public String showDepositForm() {
        return "deposit";
    }

    // ✅ Deposit POST
    @PostMapping("/deposit")
    public String processDeposit(@RequestParam Double amount,
                                 @RequestParam String pin,
                                 Authentication authentication,
                                 Model model) {
        String email = authentication.getName();
        User user = userService.findByEmail(email).orElse(null);

        if (user == null) {
            model.addAttribute("error", "User not found.");
        } else if (amount <= 0) {
            model.addAttribute("error", "Invalid deposit amount.");
        } else if (!userService.isPinCorrect(user, pin)) {
            model.addAttribute("error", "Incorrect PIN! Try again.");
        } else {
            user.setBalance(user.getBalance() + amount);
            userService.updateUser(user);

            Transaction tx = new Transaction();
            tx.setUserId(user.getId());
            tx.setType("DEPOSIT");
            tx.setAmount(amount);
            tx.setDescription("Cash deposit");
            transactionService.save(tx);

            model.addAttribute("message", "Deposit successful.");
        }

        return "deposit";
    }


    // ✅ Transfer GET
    @GetMapping("/transfer")
    public String showTransferForm() {
        return "transfer";
    }

    // ✅ Transfer POST
//    @PostMapping("/transfer")
//    public String processTransfer(
//            @RequestParam String recipientEmail,
//            @RequestParam Double amount,
//            Authentication authentication,
//            Model model) {
//
//        String senderEmail = authentication.getName();
//        User sender = userService.findByEmail(senderEmail).orElse(null);
//        User receiver = userService.findByEmail(recipientEmail).orElse(null);
//
//        if (sender == null || receiver == null) {
//            model.addAttribute("error", "Recipient not found.");
//        } else if (amount <= 0) {
//            model.addAttribute("error", "Invalid amount.");
//        } else if (sender.getBalance() < amount) {
//            model.addAttribute("error", "Insufficient balance.");
//        } else {
//            sender.setBalance(sender.getBalance() - amount);
//            receiver.setBalance(receiver.getBalance() + amount);
//            userService.updateUser(sender);
//            userService.updateUser(receiver);
//
//            Transaction tx1 = new Transaction();
//            tx1.setUserId(sender.getId());
//            tx1.setType("TRANSFER_SENT");
//            tx1.setAmount(amount);
//            tx1.setDescription("Transfer to " + recipientEmail);
//            transactionService.save(tx1);
//
//            Transaction tx2 = new Transaction();
//            tx2.setUserId(receiver.getId());
//            tx2.setType("TRANSFER_RECEIVED");
//            tx2.setAmount(amount);
//            tx2.setDescription("Received from " + senderEmail);
//            transactionService.save(tx2);
//
//            model.addAttribute("message", "Transfer successful.");
//        }
//
//        return "transfer";
//    }

    // ✅ PIN Change GET
    @GetMapping("/pinchange")
    public String showPinChangeForm() {
        return "changepin";
    }

    // ✅ PIN Change POST
    @PostMapping("/pinchange")
    public String processPinChange(
            @RequestParam String currentPin,
            @RequestParam String newPin,
            @RequestParam String confirmPin,
            Authentication authentication,
            Model model) {

        String email = authentication.getName();
        User user = userService.findByEmail(email).orElse(null);

        if (user == null) {
            model.addAttribute("error", "User not found.");
        } else if (user.getPin() == null || !user.getPin().equals(currentPin)) {
            model.addAttribute("error", "Current PIN is incorrect.");
        } else if (!newPin.equals(confirmPin)) {
            model.addAttribute("error", "New PINs do not match.");
        } else if (newPin.length() < 4 || newPin.length() > 6) {
            model.addAttribute("error", "PIN must be 4 to 6 digits.");
        } else {
            user.setPin(newPin);
            userService.updateUser(user);
            model.addAttribute("message", "PIN changed successfully.");
        }

        return "dashboard";
    }

    // ✅ Bill Payment GET
    @GetMapping("/billpayment")
    public String showBillPaymentForm() {
        return "billpayment";
    }

    // ✅ Bill Payment POST
    @PostMapping("/billpayment")
    public String processBillPayment(
            @RequestParam String billType,
            @RequestParam Double amount,
            Authentication authentication,
            Model model) {

        String email = authentication.getName();
        User user = userService.findByEmail(email).orElse(null);

        if (user == null) {
            model.addAttribute("error", "User not found.");
        } else if (amount <= 0) {
            model.addAttribute("error", "Invalid amount.");
        } else if (user.getBalance() < amount) {
            model.addAttribute("error", "Insufficient balance.");
        } else {
            user.setBalance(user.getBalance() - amount);
            userService.updateUser(user);

            Transaction tx = new Transaction();
            tx.setUserId(user.getId());
            tx.setType("BILL_PAYMENT");
            tx.setAmount(amount);
            tx.setDescription("Bill Payment: " + billType);
            transactionService.save(tx);

            model.addAttribute("message", "Bill payment successful.");
        }

        return "billpayment";
    }

    // ✅ Receipt View
    @GetMapping("/receipt/{id}")
    public String viewReceipt(@PathVariable Long id, Model model, Authentication authentication) {
        String email = authentication.getName();
        User user = userService.findByEmail(email).orElse(null);

        Transaction tx = transactionService.getTransactionById(id);
        if (user == null || tx == null || !tx.getUserId().equals(user.getId())) {
            model.addAttribute("error", "Transaction not found or access denied.");
            return "redirect:/dashboard";
        }

        model.addAttribute("transaction", tx);
        return "receipt";
    }

    @PostMapping("/transfer")
    public String processTransfer(
            @RequestParam String recipientEmail,
            @RequestParam Double amount,
            @RequestParam String pin,
            Authentication authentication,
            Model model) {

        String senderEmail = authentication.getName();
        User sender = userService.findByEmail(senderEmail).orElse(null);
        User receiver = userService.findByEmail(recipientEmail).orElse(null);

        if (sender == null || receiver == null) {
            model.addAttribute("error", "Recipient not found.");
        } else if (amount <= 0) {
            model.addAttribute("error", "Invalid amount.");
        } else if (!userService.isPinCorrect(sender, pin)) {
            model.addAttribute("error", "Incorrect PIN! Try again.");
        } else if (sender.getBalance() < amount) {
            model.addAttribute("error", "Insufficient balance.");
        } else {
            sender.setBalance(sender.getBalance() - amount);
            receiver.setBalance(receiver.getBalance() + amount);
            userService.updateUser(sender);
            userService.updateUser(receiver);

            Transaction tx1 = new Transaction();
            tx1.setUserId(sender.getId());
            tx1.setType("TRANSFER_SENT");
            tx1.setAmount(amount);
            tx1.setDescription("Transfer to " + recipientEmail);
            transactionService.save(tx1);

            Transaction tx2 = new Transaction();
            tx2.setUserId(receiver.getId());
            tx2.setType("TRANSFER_RECEIVED");
            tx2.setAmount(amount);
            tx2.setDescription("Received from " + senderEmail);
            transactionService.save(tx2);

            model.addAttribute("message", "Transfer successful.");
        }

        return "transfer";
    }
}

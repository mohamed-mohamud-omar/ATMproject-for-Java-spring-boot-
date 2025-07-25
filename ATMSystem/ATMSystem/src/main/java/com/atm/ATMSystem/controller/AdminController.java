package com.atm.ATMSystem.controller;

import com.atm.ATMSystem.model.User;
import com.atm.ATMSystem.service.TransactionService;
import com.atm.ATMSystem.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    @Autowired
    private UserService userService;

    @Autowired
    private TransactionService transactionService;

    // ✅ Admin Dashboard
    @GetMapping("/dashboard")
    public String adminHome(Model model) {
        model.addAttribute("users", userService.getAllUsers());
        return "admin-dashboard";
    }

    // ✅ View User Details
    @GetMapping("/user/{id}")
    public String viewUser(@PathVariable Long id, Model model) {
        User user = userService.findById(id);
        if (user == null) {
            return "redirect:/admin/dashboard";
        }
        model.addAttribute("user", user);
        model.addAttribute("transactions", transactionService.findByUserId(id));
        return "admin-user";
    }

    // ✅ Add User Form
    @GetMapping("/user/add")
    public String showAddUserForm(Model model) {
        model.addAttribute("user", new User());
        return "admin-add-user";
    }

    // ✅ Save New User
    @PostMapping("/user/add")
    public String saveUser(@ModelAttribute User user) {
        userService.register(user);
        return "redirect:/admin/dashboard";
    }

    // ✅ Edit User Form
    @GetMapping("/user/edit/{id}")
    public String showEditUserForm(@PathVariable Long id, Model model) {
        User user = userService.findById(id);
        if (user == null) {
            return "redirect:/admin/dashboard";
        }
        model.addAttribute("user", user);
        return "admin-edit-user";
    }

    // ✅ Update User
    @PostMapping("/user/edit/{id}")
    public String updateUser(@PathVariable Long id, @ModelAttribute User user) {
        User existingUser = userService.findById(id);
        if (existingUser == null) {
            return "redirect:/admin/dashboard";
        }

        existingUser.setFullName(user.getFullName());
        existingUser.setEmail(user.getEmail());
        existingUser.setBalance(user.getBalance());
        existingUser.setRole(user.getRole());

        if (user.getPassword() != null && !user.getPassword().isEmpty()) {
            existingUser.setPassword(user.getPassword());
        }

        userService.updateUser(existingUser);
        return "redirect:/admin/dashboard";
    }

    // ✅ Delete User
    @GetMapping("/user/delete/{id}")
    public String deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return "redirect:/admin/dashboard";
    }

//    @GetMapping("/dashboard")
//    public String adminHome(@RequestParam(value = "keyword", required = false) String keyword, Model model) {
//        if (keyword != null && !keyword.trim().isEmpty()) {
//            model.addAttribute("users", userService.searchUsers(keyword));
//            model.addAttribute("keyword", keyword);
//        } else {
//            model.addAttribute("users", userService.getAllUsers());
//            model.addAttribute("keyword", "");
//        }
//        return "admin-dashboard";
//    }

}

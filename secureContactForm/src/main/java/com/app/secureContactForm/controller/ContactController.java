package com.app.secureContactForm.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/contacts")
public class ContactController {

    @GetMapping
    //@PreAuthorize(hasRole("user"))
    public String getContacts()
    {
        return "Returning All Contacts";
    }
   // @PreAuthorize(hasRole("user"))
    @PostMapping
    public String addContact() {
        return "New contact added!";
    }

    @DeleteMapping("/{id}")
    public String deleteContact(@PathVariable int id) {
        return "Contact " + id + " deleted!";
    }

    @GetMapping("/public/info")
    public String publicInfo() {
        return "This is a public endpoint";
    }


}

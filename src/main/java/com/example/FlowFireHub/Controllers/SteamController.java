package com.example.FlowFireHub.Controllers;

import com.example.FlowFireHub.Domains.Role;
import com.example.FlowFireHub.Domains.Steam;
import com.example.FlowFireHub.Domains.User;
import com.example.FlowFireHub.Respositories.RoleRepository;
import com.example.FlowFireHub.Respositories.SteamRepository;
import com.example.FlowFireHub.Utilities.SteamManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import javax.servlet.http.HttpServletRequest;
import java.util.Optional;

@RestController
@RequestMapping(value = "/steam", path = "/steam")
public class SteamController {

    @Autowired
    SteamRepository steamRepository;
    @Autowired
    SteamManager steamManager;
    @Autowired
    RoleRepository roleRepository;


    @GetMapping("/getAllUsers")
    public Iterable<Steam> getAllUsers() {
        Iterable<Steam> steam = steamRepository.findAll();
        return steam;
    }

    @GetMapping("/getUser/{id}")
    public ResponseEntity<Steam> getUserById(@PathVariable("id") Long id) {
        Optional<Steam> steamEntity =  steamRepository.findById(id);
        if(steamEntity.isPresent()) {
            Steam steam = steamEntity.get();
            return new ResponseEntity<>(steam, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @DeleteMapping("/deleteUser/{id}")
    public ResponseEntity<Steam> deleteUser(@PathVariable("id") Long id) {
        try {
            steamRepository.deleteUserById(id);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/addUser")
    public ResponseEntity<Steam> addSteamUser(@RequestBody Steam steam) {
        Optional<Steam> addSteamUser = steamRepository.findByUsernameOrSteamId(steam.getUsername(), steam.getSteamid());
        if(!addSteamUser.isPresent()) {
            String steamData = steamManager.getSteamData(steam.getSteamid());
            String steamid = steamManager.getKey(steamData, "steamid");
            String username = steamManager.getKey(steamData, "personaname");
            steam.setUsername(username);
            steam.setSteamid(steamid);
            Role role = roleRepository.findByName("Admin");
            steam.setUser(new User(username, role));
            return new ResponseEntity<Steam>(steamRepository.save(steam), HttpStatus.OK);
        } else {
            return new ResponseEntity<Steam>(steam, HttpStatus.NOT_ACCEPTABLE);
        }
    }

    @GetMapping("/OpenId")
    public String getURLValue(HttpServletRequest request){
        String test = request.getQueryString();
        String test2 = request.getParameter("openid.identity");
        String test3 = test2.substring(test2.lastIndexOf("/")+1);
        System.out.println(test3);
        System.out.println(test2);
        System.out.println(test);
        return "redirect:chat";
    }

}

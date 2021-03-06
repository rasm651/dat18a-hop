package com.example.FlowFireHub.Controllers;

import com.example.FlowFireHub.Domains.Role;
import com.example.FlowFireHub.Domains.Steam;
import com.example.FlowFireHub.Domains.User;
import com.example.FlowFireHub.Repositories.RoleRepository;
import com.example.FlowFireHub.Repositories.SteamRepository;
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
    private SteamRepository steamRepository;
    @Autowired
    private SteamManager steamManager;
    @Autowired
    private RoleRepository roleRepository;


    @GetMapping("/getAllUsers")
    public Iterable<Steam> getAllUsers() {
        Iterable<Steam> steam = steamRepository.findAll();
        return steam;
    }

    @GetMapping("/steam/login")
    public ResponseEntity<Steam> steamLogin(HttpServletRequest request) {
        String steam_openid = request.getParameter("openid.identity");
        String steamData = steamManager.getSteamData(steam_openid);
        String steamid = steamManager.getKey(steamData, "steamid");
        String username = steamManager.getKey(steamData, "personaname");
        Optional<Steam> addSteamUser = steamRepository.findByUsernameOrSteamId(username, steamid);
        if(!addSteamUser.isPresent()) {
            Steam steamuser = new Steam();
            steamuser.setUsername(username);
            steamuser.setSteamid(steamid);
            Role role = roleRepository.findByName("User");
            steamuser.setUser(new User(username, role));
            return new ResponseEntity<Steam>(steamRepository.save(steamuser), HttpStatus.OK);
        } else {
            Steam steam = addSteamUser.get();
            return new ResponseEntity<Steam>(steam, HttpStatus.NOT_ACCEPTABLE);
        }
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

}

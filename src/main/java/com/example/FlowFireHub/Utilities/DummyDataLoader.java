package com.example.FlowFireHub.Utilities;

import com.example.FlowFireHub.Domains.ChatRoom;
import com.example.FlowFireHub.Domains.FlowFire;
import com.example.FlowFireHub.Domains.Role;
import com.example.FlowFireHub.Domains.User;
import com.example.FlowFireHub.Repositories.ChatRoomRepository;
import com.example.FlowFireHub.Repositories.FlowFireRepository;
import com.example.FlowFireHub.Repositories.RoleRepository;
import com.example.FlowFireHub.Repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

//@Profile({"dev", "test"})
@Component
public class DummyDataLoader implements CommandLineRunner {

    @Autowired
    UserRepository userRepository;

    @Autowired
    FlowFireRepository flowFireRepository;

    @Autowired
    ChatRoomRepository chatRoomRepository;

    @Autowired
    RoleRepository roleRepository;

    @Value("${spring.datasource.driverClassName:NONE}")
    String dbDriver;

    @Override
    public void run(String... args) throws Exception {
        userRepository.deleteAll();
        flowFireRepository.deleteAll();
        roleRepository.deleteAll();
        chatRoomRepository.deleteAll();

        Role role = new Role();
        role.setName("Administrator");
        roleRepository.save(role);

        {
            User user = new User();
            user.setUsername("Rasmus");
            user.setRole(role);

            FlowFire fireFlow = new FlowFire();
            fireFlow.setPassword("$2a$10$Wpn3AIuaRU4/975BYY8pLepwwV9phOah65k6uh3Kk2rAJn3ghq4Li");
            fireFlow.setUsername("Rasmus");
            fireFlow.setUser(user);
            flowFireRepository.save(fireFlow);
        }

        {
            User user = new User();
            user.setUsername("Peter");
            user.setRole(role);

            FlowFire fireFlow = new FlowFire();
            fireFlow.setPassword("$2a$10$Wpn3AIuaRU4/975BYY8pLepwwV9phOah65k6uh3Kk2rAJn3ghq4Li");
            fireFlow.setUsername("Peter");
            fireFlow.setUser(user);
            flowFireRepository.save(fireFlow);
        }

        {
            ChatRoom room = new ChatRoom("general", false);
            chatRoomRepository.save(room);
        }

        {
            ChatRoom room = new ChatRoom("synthetik", false);
            chatRoomRepository.save(room);
        }

        {
            ChatRoom room = new ChatRoom("offtopic", false);
            chatRoomRepository.save(room);
        }
    }
}

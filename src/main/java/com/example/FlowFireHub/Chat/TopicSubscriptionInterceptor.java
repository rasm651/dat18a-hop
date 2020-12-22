package com.example.FlowFireHub.Chat;

import com.example.FlowFireHub.Auth.AuthToken;
import com.example.FlowFireHub.Auth.IValues;
import com.example.FlowFireHub.Auth.JwtAuthFilter;
import com.example.FlowFireHub.Domains.ChatMessage;
import com.example.FlowFireHub.Domains.ChatRoom;
import com.example.FlowFireHub.Domains.User;
import com.example.FlowFireHub.Respositories.ChatMessageRepository;
import com.example.FlowFireHub.Respositories.ChatRoomRepository;
import com.example.FlowFireHub.Respositories.UserRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwt;
import io.jsonwebtoken.Jwts;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import java.security.Principal;
import java.util.ArrayList;
import java.util.List;

@Component
public class TopicSubscriptionInterceptor implements ChannelInterceptor {

    private static UserRepository userRepository;

    @Autowired
    public void setUserRepository(UserRepository userRepository) {
        TopicSubscriptionInterceptor.userRepository = userRepository;
    }

    private static ChatRoomRepository chatRoomRepository;

    @Autowired
    public void setChatRoomRepository(ChatRoomRepository chatRoomRepository) {
        TopicSubscriptionInterceptor.chatRoomRepository = chatRoomRepository;
    }

    private static ChatMessageRepository chatMessageRepository;

    @Autowired
    public void setChatMessageRepository(ChatMessageRepository chatMessageRepository) {
        TopicSubscriptionInterceptor.chatMessageRepository = chatMessageRepository;
    }

    public User authenticateToken(String token) throws ServletException {
        User result = null;

        SecurityContext context = SecurityContextHolder.getContext();
        Jws<Claims> claims = Jwts.parser().requireIssuer(IValues.ISSUER).setSigningKey(IValues.SECRET_KEY).parseClaimsJws(token);
        String user = (String) claims.getBody().get("usr");
        String roles = (String) claims.getBody().get("rol");
        List<GrantedAuthority> authority = new ArrayList<GrantedAuthority>();
        for (String role : roles.split(","))
            authority.add(new SimpleGrantedAuthority(role));

        AuthToken authenticationTkn = new AuthToken(user, null, authority);
        result = userRepository.findByUsername(user).get();
        context.setAuthentication(authenticationTkn);

        return result;
    }

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(message);
        System.out.println(headerAccessor.getCommand());

        String token = null;
        if (headerAccessor.containsNativeHeader("Bearer")) {
            token = headerAccessor.getNativeHeader("Bearer").get(0);
            System.out.println(token);
        }

        if (token != null) {
            try {
                User user = authenticateToken(token);

                switch (headerAccessor.getCommand()) {
                    case SUBSCRIBE: {
                        long roomId = (headerAccessor.containsNativeHeader("room")) ? Long.parseLong(headerAccessor.getNativeHeader("room").get(0)) : -1;
                        if (!verifyRoomSubscription(roomId, user)) {
                            throw new IllegalArgumentException("No permission to subscribe to chatroom");
                        }
                        break;
                    }
                    case SEND: {
                        long roomId = (headerAccessor.containsNativeHeader("room")) ? Long.parseLong(headerAccessor.getNativeHeader("room").get(0)) : -1;
                        if (!verifyRoomSubscription(roomId, user)) {
                            throw new IllegalArgumentException("No permission to send to chatroom");
                        }
                        break;
                    }
                }
            } catch (ServletException e) {
                throw new IllegalArgumentException("Unauthorized token");
            }
        } else {

            // TODO (rhoe) better exception handling when there is no token?
            // User jwt token not accepted
            throw new IllegalArgumentException("Invalid token");
        }

        return message;
    }

    private boolean verifyRoomSubscription(long roomId, User user) {
        ChatRoom chatRoom = chatRoomRepository.findById(roomId).get();
        if (chatRoom == null)
            return false;
        if (chatRoom.isPrivate() && !chatRoom.hasUser(user)) {
            // User doesn't have access to chatroom
            return false;
        }
        return true;
    }

}
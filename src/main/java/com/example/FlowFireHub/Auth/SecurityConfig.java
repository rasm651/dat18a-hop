package com.example.FlowFireHub.Auth;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

// Spring boot security configuration class.
@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    // The Jwt token authentication filter. This filter will intercept all the requests other than the “/token” uri.
    // The class is created to fetch the authentication token from the request, parse and validate the jwt token for further processing.
    @Bean
    public JwtAuthFilter jwtAuthenticationFilter() {
        return new JwtAuthFilter();
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .authorizeRequests()
                .antMatchers("/token")
                .permitAll()
                .and().authorizeRequests().antMatchers("/me/*").permitAll() //access("hasAuthority('Administrator') or hasAuthority('Bruger')")
                .and().authorizeRequests().antMatchers("/friend/*/*").permitAll()
                .and().authorizeRequests().antMatchers("/admin/*").hasAuthority("Administrator")
                .and().authorizeRequests().antMatchers("/users/*/*").permitAll()
                .anyRequest()
                .authenticated()
                .and()
                .csrf().disable()
                .addFilterBefore(jwtAuthenticationFilter(),
                        UsernamePasswordAuthenticationFilter.class);
    }

//    @Override
//    protected void configure(HttpSecurity http) throws Exception {
//        http.authorizeRequests().antMatchers("/").permitAll().and().csrf().disable();
//    }
//
//    @Override
//    public void configure(WebSecurity http) throws Exception {
//        http
//                .ignoring()
//                .antMatchers("/h2-console/**");
//    }
}
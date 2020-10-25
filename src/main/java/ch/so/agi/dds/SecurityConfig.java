package ch.so.agi.dds;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Configuration
public class SecurityConfig extends WebSecurityConfigurerAdapter {
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    @Value("${app.uploadUser}")
    private String uploadUser;

    @Value("${app.uploadPassword}")
    private String uploadPassword;

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
//            .csrf().disable()
//            .authorizeRequests()
//            .antMatchers("/ping").permitAll()
//            .anyRequest().authenticated().and().httpBasic();
        .csrf().disable()
        .authorizeRequests()
        .antMatchers("/upload").authenticated()
        .anyRequest().permitAll().and().httpBasic();

    }

    @Autowired
    public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
        auth.inMemoryAuthentication().withUser(uploadUser).password(passwordEncoder().encode(uploadPassword)).roles("USER");
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}

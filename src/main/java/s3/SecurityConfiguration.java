package s3;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        
    	http
			//HTTP Basic authentication
			.httpBasic()
			.and()
			.authorizeRequests()
				.anyRequest()
				.authenticated()
			.and()
			.csrf().disable()
			.formLogin().disable();
	}

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        
    	// Enable default password encoder (mandatory since Spring Security 5 to avoid storing passwords in plain text)
    	PasswordEncoder encoder = PasswordEncoderFactories.createDelegatingPasswordEncoder();
    	
    	// User
        auth.inMemoryAuthentication().withUser("user").password(encoder.encode("pass")).roles("USER");
    }

}

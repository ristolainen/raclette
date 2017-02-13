package se.racasse.raclette;

import com.ullink.slack.simpleslackapi.SlackSession;
import com.ullink.slack.simpleslackapi.impl.SlackSessionFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class App {
    public static void main(String[] args) {
        SpringApplication.run(App.class, args);
    }

    @Configuration
    @ComponentScan("se.racasse.raclette")
    static class Config {

        @Bean(initMethod = "connect", destroyMethod = "disconnect")
        public SlackSession slackSession() {
            return SlackSessionFactory.createWebSocketSlackSession("xoxb-139115468736-ttSK40EAITeLCjiVA8uDu14G");
        }
    }
}

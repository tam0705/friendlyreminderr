package com.friendly.bot;

import com.linecorp.bot.client.LineMessagingClient;
import com.linecorp.bot.model.ReplyMessage;
import com.linecorp.bot.model.event.Event;
import com.linecorp.bot.model.event.MessageEvent;
import com.linecorp.bot.model.event.message.TextMessageContent;
import com.linecorp.bot.model.event.source.Source;
import com.linecorp.bot.model.message.TextMessage;
import com.linecorp.bot.model.message.Message;
import com.linecorp.bot.model.response.BotApiResponse;
import com.linecorp.bot.spring.boot.annotation.EventMapping;
import com.linecorp.bot.spring.boot.annotation.LineMessageHandler;
import org.apache.commons.dbcp2.BasicDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.sql.*;
import javax.annotation.PostConstruct;
import javax.sql.DataSource;

@SpringBootApplication
@LineMessageHandler
@ComponentScan(basePackages = "com.friendly.bot")
public class FriendlyReminder extends SpringBootServletInitializer {
    String[] lastEditorId = new String[2];
    String[] lastEditorName = new String[2];

    @Bean
    public static BasicDataSource dataSource() throws URISyntaxException {
        URI dbUri = new URI(System.getenv("DATABASE_URL"));

        String username = dbUri.getUserInfo().split(":")[0];
        String password = dbUri.getUserInfo().split(":")[1];
        String dbUrl = "jdbc:postgresql://" + dbUri.getHost() + ':' + dbUri.getPort() + dbUri.getPath() + "?sslmode=require";

        BasicDataSource basicDataSource = new BasicDataSource();
        basicDataSource.setUrl(dbUrl);
        basicDataSource.setUsername(username);
        basicDataSource.setPassword(password);

        return basicDataSource;
    }

    @Autowired
    private LineMessagingClient lineMessagingClient;

    @Autowired
    private DataSource dataSource;

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
        return builder.sources(FriendlyReminder.class);
    }

    public static void main(String[] args) {
        SpringApplication.run(FriendlyReminder.class, args);
    }

    @PostConstruct
    public void myRealMainMethod() throws SQLException {
        Statement stmt = dataSource.getConnection().createStatement();
        /*stmt.executeUpdate("DROP TABLE IF EXISTS ticks");
        stmt.executeUpdate("CREATE TABLE ticks (tick timestamp)");
        stmt.executeUpdate("INSERT INTO ticks VALUES (now())");
        ResultSet rs = stmt.executeQuery("SELECT tick FROM ticks");
        while (rs.next()) {
            System.out.println("Read from DB: " + rs.getTimestamp("tick"));
        }*/
    }

    @EventMapping
    public void handleTextMessageEvent(MessageEvent<TextMessageContent> event) throws Exception {
        TextMessageContent message = event.getMessage();
        handleTextContent(event.getReplyToken(), event, message);
    }

    private void reply(String replyToken, Message message) {
        reply(replyToken, Collections.singletonList(message));
    }

    private void reply(String replyToken, List<Message> messages) {
        try {
            BotApiResponse apiResponse = lineMessagingClient
                    .replyMessage(new ReplyMessage(replyToken, messages))
                    .get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    private void replyText(String replyToken, String message) {
        if (replyToken.isEmpty()) {
            throw new IllegalArgumentException("replyToken must not be empty");
        }
        if (message.length() > 1000) {
            message = message.substring(0, 1000 - 2) + "...";
        }
        this.reply(replyToken, new TextMessage(message));
    }

    private void handleTextContent(String replyToken, Event event, TextMessageContent content)
            throws Exception {
        String text = content.getText();
        String userId = event.getSource().getUserId();
        switch (text) {
            case "/reminder tomorrow":
            case "/reminder 0":
            case "/rm tomorrow":
            case "/rm 0":
                this.showReminder(0,replyToken);
                break;
            case "/reminder week":
            case "/reminder 1":
            case "/rm week":
            case "/rm 1":
                this.showReminder(1,replyToken);
                break;
            case "/rmedit tomorrow":
            case "/rmedit 0":
                this.editReminder(0,replyToken,userId);
                break;
            case "/rmedit week":
            case "/rmedit 1":
                this.editReminder(1,replyToken,userId);
                break;
        }
    }

    private void showReminder(Integer param, String replyToken) {
        String constAnswer0 = " ";
        if (lastEditorId[param] == null) {
            lastEditorId[param] = "U0000";
        }
        if (lastEditorName[param] == null) {
            lastEditorName[param] = "unknown";
        }
        //A line can only has max. 112 chars, so shorterners are needed
        String[] shorterner = new String[2];
        shorterner[0] = lastEditorName[param];
        shorterner[1] = lastEditorId[param].substring(0,5);
        String constAnswer1 = "Recently edited by " + shorterner[0] + " [" + shorterner[1] + "]";
        if (param == 0) {
            constAnswer0 = "What to do for tomorrow is..";
        } else if (param == 1) {
            constAnswer0 = "This week's todo list is..";
        }
        this.reply(replyToken,Arrays.asList(new TextMessage(constAnswer0),new TextMessage(constAnswer1)));
    }

    private void editReminder(Integer param, String replyToken, String userId) {
        lastEditorId[param] = "U0000";
        lastEditorName[param] = "unknown";
        if (userId != null) {
                    lineMessagingClient
                            .getProfile(userId)
                            .whenComplete((profile, throwable) -> {
                                if (throwable != null) {
                                    this.replyText(replyToken, throwable.getMessage());
                                    return;
                                }
                                lastEditorId[param] = userId;
                                lastEditorName[param] = profile.getDisplayName();
                            });
        }
        this.replyText(replyToken,"Successfully edited!");
    }
}

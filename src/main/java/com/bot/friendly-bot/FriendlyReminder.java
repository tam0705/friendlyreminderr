package com.friendly.bot;

import com.linecorp.bot.client.LineMessagingClient;
<<<<<<< HEAD
import com.linecorp.bot.model.ReplyMessage;
import com.linecorp.bot.model.event.Event;
import com.linecorp.bot.model.event.MessageEvent;
import com.linecorp.bot.model.event.message.TextMessageContent;
import com.linecorp.bot.model.event.source.Source;
import com.linecorp.bot.model.message.TextMessage;
import com.linecorp.bot.model.message.Message;
=======
import com.linecorp.bot.model.ReplyMessage;
import com.linecorp.bot.model.event.Event;
import com.linecorp.bot.model.event.MessageEvent;
import com.linecorp.bot.model.event.message.TextMessageContent;
import com.linecorp.bot.model.event.source.Source;
import com.linecorp.bot.model.message.TextMessage;
import com.linecorp.bot.model.message.Message;
>>>>>>> e3618004175984486a16a7015ab49c434f06043c
import com.linecorp.bot.model.response.BotApiResponse;
import com.linecorp.bot.spring.boot.annotation.EventMapping;
import com.linecorp.bot.spring.boot.annotation.LineMessageHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
<<<<<<< HEAD

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
=======

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
>>>>>>> e3618004175984486a16a7015ab49c434f06043c

@SpringBootApplication
@LineMessageHandler
public class FriendlyReminder extends SpringBootServletInitializer {
<<<<<<< HEAD
    String[] lastEditorId = new String[2];
    String[] lastEditorName = new String[2];
=======
    String[] lastEditorId = new String[2];
    String[] lastEditorName = new String[2];
>>>>>>> e3618004175984486a16a7015ab49c434f06043c

    @Autowired
    private LineMessagingClient lineMessagingClient;

    @Autowired
    private DataSource dataSource;

    @Autowired
    private DataSource dataSource;

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
        return builder.sources(FriendlyReminder.class);
    }

    public static void main(String[] args) {
        SpringApplication.run(FriendlyReminder.class, args);
    }

<<<<<<< HEAD
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

    private void showReminder(Integer param, String replyToken) throws SQLException {
        //Set helper variables
        String constAnswer0 = " ";
        if (lastEditorId[param] == null) {
            lastEditorId[param] = "U0000";
        }
        if (lastEditorName[param] == null) {
            lastEditorName[param] = "Unknown";
        }
        String tableName = " ";
        if (param == 0) {
            tableName = "tomorrow_editor";
            constAnswer0 = "What to do for tomorrow is..";
        } else if (param == 1) {
            tableName = "week_editor";
            constAnswer0 = "This week's ToDo list is..";
        }

        //Access the database to refresh last editor infos
        Statement stmt = dataSource.getConnection().createStatement();
        ResultSet rs = stmt.executeQuery("SELECT user_id,user_name FROM " + tableName);
        while (rs.next()) {
            lastEditorId[param] = rs.getString("user_id");
            lastEditorName[param] = rs.getString("user_name");
        }
        String constAnswer1 = "Recently edited by " + lastEditorName[param] + " [" + lastEditorId[param] + "]";
        rs.close();
        stmt.close();

        //Give response to the user
        this.reply(replyToken,Arrays.asList(new TextMessage(constAnswer0),new TextMessage(constAnswer1)));
    }

    private void editReminder(Integer param, String replyToken, String userId) throws SQLException {
        //Get editor infos
        lastEditorId[param] = "U0000";
        lastEditorName[param] = "Unknown";
        if (userId != null) {
                    lineMessagingClient
                            .getProfile(userId)
                            .whenComplete((profile, throwable) -> {
                                if (throwable != null) {
                                    this.replyText(replyToken, throwable.getMessage());
                                    return;
                                }
                                lastEditorId[param] = userId.substring(0,5);
                                lastEditorName[param] = profile.getDisplayName();
                            });
        }

        //Give response to the user
        this.reply(replyToken,new TextMessage("Successfully edited!"));

        //Set helper variables
        String tableName = " ";
        String shortener0 = " (user_id varchar(5) not null,user_name varchar(20) not null);";
        String shortener1 = "(user_id,user_name) VALUES (";
        if (param == 0) {
            tableName = "tomorrow_editor";
        } else if (param == 1) {
            tableName = "week_editor";
        }

        //Access the database to write new infos
        Statement stmt = dataSource.getConnection().createStatement();
        stmt.executeUpdate("DROP TABLE IF EXISTS " + tableName);
        stmt.executeUpdate("CREATE TABLE " + tableName + shortener0);
        stmt.executeUpdate("INSERT INTO " + tableName + shortener1 + "'" + lastEditorId[param] + "','" + lastEditorName[param] +"')");
        stmt.close();
=======
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

    private void showReminder(Integer param, String replyToken) throws SQLException {
        //Set helper variables
        String constAnswer0 = " ";
        if (lastEditorId[param] == null) {
            lastEditorId[param] = "U0000";
        }
        if (lastEditorName[param] == null) {
            lastEditorName[param] = "Unknown";
        }
        String tableName = " ";
        if (param == 0) {
            tableName = "tomorrow_editor";
            constAnswer0 = "What to do for tomorrow is..";
        } else if (param == 1) {
            tableName = "week_editor";
            constAnswer0 = "This week's ToDo list is..";
        }

        //Access the database to refresh last editor infos
        Statement stmt = dataSource.getConnection().createStatement();
        ResultSet rs = stmt.executeQuery("SELECT user_id,user_name FROM " + tableName);
        while (rs.next()) {
            lastEditorId[param] = rs.getString("user_id");
            lastEditorName[param] = rs.getString("user_name");
        }
        String constAnswer1 = "Recently edited by " + lastEditorName[param] + " [" + lastEditorId[param] + "]";
        rs.close();
        stmt.close();

        //Give response to the user
        this.reply(replyToken,Arrays.asList(new TextMessage(constAnswer0),new TextMessage(constAnswer1)));
    }

    private void editReminder(Integer param, String replyToken, String userId) throws SQLException {
        //Get editor infos
        lastEditorId[param] = "U0000";
        lastEditorName[param] = "Unknown";
        if (userId != null) {
                    lineMessagingClient
                            .getProfile(userId)
                            .whenComplete((profile, throwable) -> {
                                if (throwable != null) {
                                    this.replyText(replyToken, throwable.getMessage());
                                    return;
                                }
                                lastEditorId[param] = userId.substring(0,5);
                                lastEditorName[param] = profile.getDisplayName();
                            });
        }

        //Give response to the user
        this.reply(replyToken,new TextMessage("Successfully edited!"));

        //Set helper variables
        String tableName = " ";
        String shortener0 = " (user_id varchar(5) not null,user_name varchar(20) not null);";
        String shortener1 = "(user_id,user_name) VALUES (";
        if (param == 0) {
            tableName = "tomorrow_editor";
        } else if (param == 1) {
            tableName = "week_editor";
        }

        //Access the database
        Statement stmt = dataSource.getConnection().createStatement();
        stmt.executeUpdate("DROP TABLE IF EXISTS " + tableName);
        stmt.executeUpdate("CREATE TABLE " + tableName + shortener0);
        stmt.executeUpdate("INSERT INTO " + tableName + shortener1 + lastEditorId[param] + "," + lastEditorName[param] +")");
        stmt.close();
>>>>>>> e3618004175984486a16a7015ab49c434f06043c
    }
}

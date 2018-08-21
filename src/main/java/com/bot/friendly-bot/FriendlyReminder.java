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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.ArrayList<E>;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.sql.*;
import javax.annotation.PostConstruct;
import javax.sql.DataSource;

@SpringBootApplication
@LineMessageHandler
public class FriendlyReminder extends SpringBootServletInitializer {
    private String lastEditorId;
    private String lastEditorName;

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

    private void handleTextContent(String replyToken, Event event, TextMessageContent content)
            throws Exception {
        //Set helper variables
        String text = content.getText();
        String userId = event.getSource().getUserId();
        String[] keywords = text.split(" ");

        //Detect the command and give response
        switch (keywords[0]) {
            case "/reminder": case "/rm":
                if (keywords.length == 2) {
                    Integer param = -1;
                    switch (keywords[1]) {
                        case "tomorrow": case "0":
                            param = 0;
                            break;
                        case "week": case "1":
                            param = 1;
                            break;
                        case "all": case "2":
                            param = 2;
                            break;
                    }
                    this.showReminder(param,replyToken);
                }
                break;
            case "/rmadd":
                if (keywords.length > 2) {
                    String title = keywords[1];
                    String dueDate = keywords[2];
                    String taskContent = "";
                    if (keywords.length == 3) {
                        taskContent = keywords[3];
                    }
                    addTask(userId,replyToken,title,dueDate,taskContent);
                }
                break;
        }
    }

    private void showReminder(Integer param, String replyToken) throws SQLException {
        //Initialise helper variables
        String constAnswer0 = ""; //Final form of the response
        String constAnswer1 = ""; //Opening sentence
        String constAnswer2 = "Nothing."; //Content of the ToDo list
        String constAnswer3 = ""; //Recent editor infos
        lastEditorId = "U0000";
        lastEditorName = "Unknown";
        String editTime = "unknown";

        //Access the database to get datas
        Statement stmt = dataSource.getConnection().createStatement();
        ResultSet rsEditor = stmt.executeQuery("SELECT user_id,user_name,edit_time FROM last_editor");
        while (rsEditor.next()) {
            lastEditorId = rsEditor.getString("user_id");
            lastEditorName = rsEditor.getString("user_name");
            editTime = rsEditor.getString("edit_time"); 
        }
        rsEditor.close();

        List<String> taskTitles = new ArrayList<String>();
        List<String> dueDates = new ArrayList<String>();
        ResultSet rsTask = stmt.executeQuery("SELECT title,due_date FROM todo_list");
        while (rsTask.next()) {
            taskTitles.add(rsTask.getString("title"));
            dueDates.add(rsTask.getString("due_date"));
        }
        rsTask.close();
        stmt.close();

        //Set response variables
        if (param == 0) {
            constAnswer1 = "What to do for tomorrow is..";
        } else if (param == 1) {
            constAnswer1 = "This week's ToDo list is..";
        } else if (param == 2) {
            constAnswer1 = "What is in the ToDo list is..";
        }

        if (taskTitles.size() > 0) {
            constAnswer2 = "";
            for (Integer i = 0; i < taskTitles.size(); i++) {
                constAnswer2 += "- " + taskTitles.get(i) + "(" + dueDates.get(i) + ")\n";
            }
        }

        if (editTime != "unknown") {
            constAnswer3 = "Recently edited by " + lastEditorName + " [" + lastEditorId + "] " + editTime;
        }

        constAnswer0 = constAnswer1 + "\n\n" + constAnswer2 + "\n" + constAnswer3;

        //Give response to the user
        this.reply(replyToken,new TextMessage(constAnswer0));
    }

    private String getUsername(String userId) {
        if (userId != null) {
                    lineMessagingClient
                            .getProfile(userId)
                            .whenComplete((profile, throwable) -> {
                                if (throwable != null) {
                                    return "";
                                }
                                return profile.getDisplayName();
                            });
        } else {
            return "Unknown";
        }
    }

    private String getCurrentTime() throws SQLException {
        String editTime = "unknown";
        Statement stmt = dataSource.getConnection().createStatement();
        ResultSet rs = stmt.executeQuery("SELECT TIMESTAMP WITH TIME ZONE 'now()' AT TIME ZONE 'WAST';");
        while (rs.next()) {
            Timestamp time = rs.getTimestamp("timezone");
            editTime = new SimpleDateFormat("'at' HH:mm 'on' dd/MM/yyyy").format(time);
        }
        rs.close();
        stmt.close();
        return editTime;
    }

    private void saveEditorInfos(String userId, String username, String editTime) throws SQLException {
        //Initialise helper variables
        String tableName = "last_editor";
        String shortener0 = " (user_id varchar(5) not null,user_name varchar(20) not null,edit_time varchar(255) not null);";
        String shortener1 = "(user_id,user_name,edit_time) VALUES ('";

        //Access the database and save the infos
        Statement stmt = dataSource.getConnection().createStatement();
        stmt.executeUpdate("DROP TABLE IF EXISTS " + tableName);
        stmt.executeUpdate("CREATE TABLE " + tableName + shortener0);
        stmt.executeUpdate("INSERT INTO " + tableName + shortener1 + userId + "','" + username + "','" + editTime + "')");
        stmt.close();
    }

    private void addTask(String userId, String replyToken, String title, String dueDate, String content) throws SQLException {
        //Initialise helper variables
        if (userId == null) {
            lastEditorId = "U0000";
        } else {
            lastEditorId = userId.substring(0,5);
        }
        lastEditorName = getUsername(userId);
        String editTime = getCurrentTime();
        String shortener0 = "'" + title + "','" + dueDate + "','" + content + "','" + lastEditorId + "','" + lastEditorName +"','" + editTime + "'";

        //Store information about the editor
        saveEditorInfos(lastEditorId,lastEditorName,editTime);

        //Access the database
        Statement stmt = dataSource.getConnection().createStatement();
        stmt.executeUpdate("INSERT INTO todo_list VALUES (" + shortener0 + ")");
        stmt.close();
    }    
}

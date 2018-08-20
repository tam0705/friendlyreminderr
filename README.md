# Friendly Reminder


Friendly reminder is a LINE Bot Project. As the bot's name suggests, Friendly Reminder functions as a ToDo reminder, where you can ask him to remind you what to do next by sending "Hello Reminder?"



Currently, this bot is targeted to serve only <b>a specific LINE Group</b> which identity we won't tell.


Here comes the documentation of available commands:
```
User commands:
/help <string:command> - Show help of using a command. Leave blank to show list of commands.
/reminder <(string/int):parameter> - Show the ToDo list.
/rm <(string/int):parameter> - Behaves similar to /reminder.
/rmadd <string:unique_name> <string:due_date> <string:content> - Add a task to the ToDo list.
/rmdel <string:parameter> - Delete a task in the ToDo list.
```
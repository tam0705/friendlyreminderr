# Friendly Reminder


Friendly reminder is a LINE Bot Project. As the bot's name suggests, Friendly Reminder functions as a ToDo reminder, where you can ask him to remind you what to do next by sending "Hello Reminder?"



Currently, this bot is targeted to serve only <b>a specific LINE Group</b> which identity we won't tell.


Here comes the documentation of available commands:
```
User commands:
/help <string:command> - Shows help of using a command. Leave blank to show list of user commands.
/reminder <(string/int):parameter> - Shows the ToDo list.
/rm <(string/int):parameter> - Behaves similar to /reminder.
/rmadd <string:title> <string:due_date> <(optional)string:content> - Adds a task to the ToDo list.
/rmdel <string:title> - Deletes an existing task.
/rmedit <string:title> <string:property_name> <string:new_property> - Edits an existing task.
/rminfo <string:title> - Shows brief information of an existing task.

Admin commands:
/helpadmin - Shows the list of admin commands.
```
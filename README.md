# Getting Started

### Launching the bot locally

You need to create an own Telegram bot to poll it from local machine. Once done, start the project as a normal Spring
application. Pass the following environment variables:

* **BOT_NAME**: Username of your Telegram bot
* **BOT_TOKEN**: Token to access HTTP API
* **PORT**: Port where to bind REST API of this backend
* **DATASOURCE_URL**: URL to your local PostgreSQL database
* **DATASOURCE_USERNAME**: Username to access the local database
* **DATASOURCE_PASSWORD**: Password to access the local database


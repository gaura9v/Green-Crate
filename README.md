# GreenCrate E-Commerce Platform

GreenCrate is a grocery storefront with a Java/Spring Boot commerce backend and an optional Python AI shopping assistant.

## Architecture

- **Commerce backend**: Java 25+, Spring Boot, REST API (`http://localhost:8081`)
- **Frontend**: Static HTML, CSS, and JavaScript (`static/`)
- **Database**: SQLite (`db.sqlite3`); existing products, carts, orders, and feedback are reused
- **AI chatbot**: Python FastAPI and Google Gemini (`http://localhost:8000`); the chatbot is the only Python application component

The Java backend creates the commerce tables if needed and seeds the 29 sample products only when the catalog is empty. The root SQLite database is retained, so existing commerce records remain available.

## Start the commerce application

Install Java 25 or newer and Maven, then run from the project root:

```bash
mvn spring-boot:run
```

The storefront is available at [http://localhost:8081/](http://localhost:8081/). Other pages include:

- [Products](http://localhost:8081/products.html)
- [Cart](http://localhost:8081/cart.html)
- [Orders](http://localhost:8081/orders.html)
- [Feedback](http://localhost:8081/feedback.html)
- [Feedback dashboard](http://localhost:8081/admin-feedback.html)

To package and test the Java application:

```bash
mvn test
mvn package
```

The API keeps the existing `/api/products`, `/api/cart`, `/api/orders`, and `/api/feedback` routes used by the storefront. Checkout, order status changes, product management, and feedback notifications are handled by Java.

### Optional email notifications

Copy `.env.example` to `.env` and set your SMTP credentials. The Java server reads these settings when it starts. Without SMTP credentials, feedback is still saved and the notification is skipped.

## Run the AI chatbot

Python is used only for the AI assistant in `chatbot/`. In a separate terminal:

```bash
cd chatbot
python3 -m venv .venv
source .venv/bin/activate
pip install -r requirements.txt
export GEMINI_API_KEY="your-google-ai-api-key"
uvicorn main:app --reload --port 8000
```

The chatbot reads current products from the Java API at `http://localhost:8081/api/products`. Start the Java backend before using product-aware chat responses.

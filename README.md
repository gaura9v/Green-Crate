# GreenCrate E-Commerce Platform

GreenCrate is a full-featured e-commerce platform with a Python Django REST backend, static frontend, and AI shopping assistant.

<img width="2014" height="994" alt="image" src="https://github.com/user-attachments/assets/27838733-94f7-4209-8b77-6cf89fb180da" />

## Architecture

- **Backend**: Python 3 + Django + Django REST Framework (`http://localhost:8081`)
- **Frontend**: Vanilla HTML5, CSS3, JavaScript (served directly by Django or standalone)
- **AI Chatbot**: FastAPI + Google Gemini AI (`http://localhost:8000`)
- **Database**: SQLite (`db.sqlite3` with auto-seeded product catalog)

---

## Getting Started

### 1. Set Up Python Virtual Environment
```bash
# Activate existing virtual environment
source .venv/bin/activate

# Or install dependencies
pip install -r requirements.txt
```

### 2. Run Database Migrations
Migrations automatically load all 29 initial sample products into the database:
```bash
python manage.py migrate
```

If you ever need to reload sample products manually:
```bash
python manage.py load_sample_data --force
```

### 3. Run the Django Backend
Run the backend on port **8081** (matching the frontend configuration):
```bash
python manage.py runserver 8081
```

The application is now live at:
- **Home**: [http://localhost:8081/](http://localhost:8081/) or [http://localhost:8081/home.html](http://localhost:8081/home.html)
- **Products Catalog**: [http://localhost:8081/products.html](http://localhost:8081/products.html)
- **Shopping Cart**: [http://localhost:8081/cart.html](http://localhost:8081/cart.html)
- **Orders History**: [http://localhost:8081/orders.html](http://localhost:8081/orders.html)
- **Customer Feedback**: [http://localhost:8081/feedback.html](http://localhost:8081/feedback.html)
- **Admin Feedback Dashboard**: [http://localhost:8081/admin-feedback.html](http://localhost:8081/admin-feedback.html)
- **Django Admin Portal**: [http://localhost:8081/admin/](http://localhost:8081/admin/)  
  *(Default credentials: Username: `admin` | Password: `admin123`)*

---

## 📧 Email Notifications for Feedback

When customers submit feedback via [feedback.html](file:///Users/gaurav/Downloads/EcommerceApp/ecommerce/src/main/resources/static/feedback.html), GreenCrate automatically triggers an email notification to the store administrator.

- **Development Mode (Default)**: Emails are formatted and printed directly to your terminal console, allowing instant testing without credentials.
- **Production Mode (Real Email)**: Copy `.env.example` to `.env` and enter your SMTP details (e.g., Gmail with App Password):
  ```bash
  cp .env.example .env
  ```
  Fill in `.env`:
  ```ini
  EMAIL_HOST=smtp.gmail.com
  EMAIL_PORT=587
  EMAIL_USE_TLS=True
  EMAIL_HOST_USER=your-email@gmail.com
  EMAIL_HOST_PASSWORD=your-16-character-app-password
  FEEDBACK_NOTIFICATION_EMAIL=your-email@gmail.com
  ```

---

## API Endpoints Reference

RESTful JSON API documentation:

### Products API
| Method | Endpoint | Description |
|---|---|---|
| `GET` | `/api/products` | Retrieve all products |
| `GET` | `/api/products/{id}` | Retrieve single product by ID |
| `POST` | `/api/products` | Create a new product |
| `PUT` | `/api/products/{id}` | Update product details |
| `DELETE` | `/api/products/{id}` | Delete product |
| `GET` | `/api/products/category/{category}` | Filter products by category |

### Cart API
| Method | Endpoint | Description |
|---|---|---|
| `POST` | `/api/cart` | Create a new cart |
| `GET` | `/api/cart/{cartId}` | Get cart and its items |
| `POST` | `/api/cart/{cartId}/add?productId={id}&quantity={qty}` | Add product to cart |
| `DELETE` | `/api/cart/{cartId}/item/{itemId}` | Remove item from cart |

### Orders API
| Method | Endpoint | Description |
|---|---|---|
| `POST` | `/api/orders/checkout/{cartId}` | Checkout cart into an order |
| `GET` | `/api/orders` | List all orders |
| `GET` | `/api/orders/{orderId}` | Get order details |
| `PUT` | `/api/orders/{orderId}/status?status={status}` | Update order status |
| `DELETE` | `/api/orders/{orderId}` | Delete order |

### Feedback API
| Method | Endpoint | Description |
|---|---|---|
| `GET` | `/api/feedback` | List all customer feedback |
| `POST` | `/api/feedback` | Submit feedback (`email`, `rating`, `comments`) |

---

## Running the Automated Test Suite

To verify all endpoints, database transactions, cart logic, order checkouts, and static asset serving:
```bash
python manage.py test
```

---

## Running the AI Chatbot (Optional)

The AI chatbot assistant runs on FastAPI and Gemini:
```bash
cd chatbot
uvicorn main:app --reload --port 8000
```

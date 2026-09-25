from django.test import TestCase, Client
from .models import Product, Cart, CartItem, Order, OrderItem, Feedback

class EcommerceApiTests(TestCase):
    def setUp(self):
        self.client = Client()
        self.product = Product.objects.create(
            name="Organic Bananas",
            description="Fresh sweet organic bananas",
            price=60.0,
            category="Fruits",
            stock_quantity=100
        )

    # -----------------------------
    # PRODUCT TESTS
    # -----------------------------
    def test_get_all_products(self):
        response = self.client.get('/api/products')
        self.assertEqual(response.status_code, 200)
        data = response.json()
        self.assertIsInstance(data, list)
        self.assertTrue(any(p['name'] == 'Organic Bananas' for p in data))

    def test_get_product_by_id(self):
        response = self.client.get(f'/api/products/{self.product.id}')
        self.assertEqual(response.status_code, 200)
        data = response.json()
        self.assertEqual(data['name'], 'Organic Bananas')
        self.assertEqual(data['stockQuantity'], 100)
        self.assertEqual(data['price'], 60.0)

    def test_create_product(self):
        payload = {
            "name": "Almond Milk",
            "description": "Unsweetened 1L",
            "price": 180.0,
            "category": "Dairy",
            "stockQuantity": 20
        }
        response = self.client.post('/api/products', payload, content_type='application/json')
        self.assertEqual(response.status_code, 200)
        data = response.json()
        self.assertEqual(data['name'], 'Almond Milk')
        self.assertEqual(data['stockQuantity'], 20)

    def test_update_product(self):
        payload = {
            "name": "Organic Bananas Premium",
            "price": 75.0,
            "stockQuantity": 80
        }
        response = self.client.put(f'/api/products/{self.product.id}', payload, content_type='application/json')
        self.assertEqual(response.status_code, 200)
        data = response.json()
        self.assertEqual(data['name'], 'Organic Bananas Premium')
        self.assertEqual(data['price'], 75.0)

    def test_delete_product(self):
        response = self.client.delete(f'/api/products/{self.product.id}')
        self.assertEqual(response.status_code, 200)
        self.assertFalse(Product.objects.filter(id=self.product.id).exists())

    def test_get_products_by_category(self):
        response = self.client.get('/api/products/category/fruits')
        self.assertEqual(response.status_code, 200)
        data = response.json()
        self.assertTrue(len(data) >= 1)
        self.assertTrue(all(p['category'].lower() == 'fruits' for p in data))

    # -----------------------------
    # CART TESTS
    # -----------------------------
    def test_create_and_get_cart(self):
        response = self.client.post('/api/cart')
        self.assertEqual(response.status_code, 200)
        cart_data = response.json()
        self.assertIn('id', cart_data)
        cart_id = cart_data['id']

        # Get cart
        get_response = self.client.get(f'/api/cart/{cart_id}')
        self.assertEqual(get_response.status_code, 200)
        self.assertEqual(get_response.json()['id'], cart_id)

    def test_add_and_remove_cart_item(self):
        cart_response = self.client.post('/api/cart')
        cart_id = cart_response.json()['id']

        # Add item using query params as app.js does
        add_response = self.client.post(f'/api/cart/{cart_id}/add?productId={self.product.id}&quantity=2')
        self.assertEqual(add_response.status_code, 200)
        cart_data = add_response.json()
        self.assertEqual(len(cart_data['items']), 1)
        self.assertEqual(cart_data['items'][0]['quantity'], 2)
        self.assertEqual(cart_data['items'][0]['product']['name'], 'Organic Bananas')

        # Add more of the same product
        add_more = self.client.post(f'/api/cart/{cart_id}/add?productId={self.product.id}&quantity=1')
        self.assertEqual(add_more.status_code, 200)
        self.assertEqual(add_more.json()['items'][0]['quantity'], 3)

        # Remove item
        item_id = add_more.json()['items'][0]['id']
        remove_response = self.client.delete(f'/api/cart/{cart_id}/item/{item_id}')
        self.assertEqual(remove_response.status_code, 200)
        self.assertEqual(len(remove_response.json()['items']), 0)

    # -----------------------------
    # ORDER TESTS
    # -----------------------------
    def test_checkout_and_orders(self):
        # Create cart and add product
        cart_response = self.client.post('/api/cart')
        cart_id = cart_response.json()['id']
        self.client.post(f'/api/cart/{cart_id}/add?productId={self.product.id}&quantity=2')

        # Checkout
        checkout_response = self.client.post(f'/api/orders/checkout/{cart_id}')
        self.assertEqual(checkout_response.status_code, 200)
        order_data = checkout_response.json()
        self.assertEqual(order_data['totalAmount'], 120.0)
        self.assertEqual(order_data['status'], 'PENDING')
        self.assertEqual(len(order_data['items']), 1)

        # Cart should now be empty
        cart_get = self.client.get(f'/api/cart/{cart_id}')
        self.assertEqual(len(cart_get.json()['items']), 0)

        # Get all orders
        orders_response = self.client.get('/api/orders')
        self.assertEqual(orders_response.status_code, 200)
        self.assertTrue(len(orders_response.json()) >= 1)

        # Update order status
        order_id = order_data['id']
        status_response = self.client.put(f'/api/orders/{order_id}/status?status=COMPLETED')
        self.assertEqual(status_response.status_code, 200)
        self.assertEqual(status_response.json()['status'], 'COMPLETED')

        # Delete order
        delete_response = self.client.delete(f'/api/orders/{order_id}')
        self.assertEqual(delete_response.status_code, 200)
        self.assertFalse(Order.objects.filter(id=order_id).exists())

    # -----------------------------
    # FEEDBACK TESTS
    # -----------------------------
    def test_feedback_flow(self):
        payload = {
            "email": "customer@example.com",
            "rating": 5,
            "comments": "Amazing service and fresh organic produce!"
        }
        post_response = self.client.post('/api/feedback', payload, content_type='application/json')
        self.assertEqual(post_response.status_code, 200)
        fb_data = post_response.json()
        self.assertEqual(fb_data['email'], 'customer@example.com')
        self.assertEqual(fb_data['rating'], 5)
        self.assertIn('createdAt', fb_data)

        get_response = self.client.get('/api/feedback')
        self.assertEqual(get_response.status_code, 200)
        self.assertTrue(any(fb['email'] == 'customer@example.com' for fb in get_response.json()))

        # Verify email was dispatched
        from django.core import mail
        self.assertEqual(len(mail.outbox), 1)
        self.assertIn("customer@example.com", mail.outbox[0].body)
        self.assertIn("5 Stars", mail.outbox[0].subject)

    # -----------------------------
    # FRONTEND STATIC SERVING TESTS
    # -----------------------------
    def test_frontend_serving(self):
        # Root should serve home.html
        root_resp = self.client.get('/')
        self.assertEqual(root_resp.status_code, 200)
        content = b"".join(root_resp.streaming_content)
        self.assertIn(b'GreenCrate', content)

        # home.html
        home_resp = self.client.get('/home.html')
        self.assertEqual(home_resp.status_code, 200)

        # styles.css
        css_resp = self.client.get('/styles.css')
        self.assertEqual(css_resp.status_code, 200)

        # app.js
        js_resp = self.client.get('/app.js')
        self.assertEqual(js_resp.status_code, 200)

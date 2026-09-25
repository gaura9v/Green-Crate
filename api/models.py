from django.db import models

class Product(models.Model):
    name = models.CharField(max_length=255)
    description = models.TextField(blank=True, null=True)
    price = models.FloatField(default=0.0)
    category = models.CharField(max_length=100, blank=True, null=True)
    stock_quantity = models.IntegerField(default=0)

    class Meta:
        db_table = 'products'
        ordering = ['id']

    def __str__(self):
        return f"{self.name} - ₹{self.price}"


class Cart(models.Model):
    created_at = models.DateTimeField(auto_now_add=True)

    class Meta:
        db_table = 'carts'

    def __str__(self):
        return f"Cart #{self.id}"


class CartItem(models.Model):
    cart = models.ForeignKey(Cart, related_name='items', on_delete=models.CASCADE)
    product = models.ForeignKey(Product, on_delete=models.CASCADE)
    quantity = models.IntegerField(default=1)

    class Meta:
        db_table = 'cart_items'
        ordering = ['id']

    def __str__(self):
        return f"{self.quantity}x {self.product.name} in Cart #{self.cart_id}"


class Order(models.Model):
    total_amount = models.FloatField(default=0.0)
    status = models.CharField(max_length=50, default='PENDING')
    created_at = models.DateTimeField(auto_now_add=True)

    class Meta:
        db_table = 'orders'
        ordering = ['-id']

    def __str__(self):
        return f"Order #{self.id} - {self.status} (₹{self.total_amount})"


class OrderItem(models.Model):
    order = models.ForeignKey(Order, related_name='items', on_delete=models.CASCADE)
    product = models.ForeignKey(Product, on_delete=models.CASCADE)
    quantity = models.IntegerField(default=1)
    price = models.FloatField(default=0.0)

    class Meta:
        db_table = 'order_items'
        ordering = ['id']

    def __str__(self):
        return f"{self.quantity}x {self.product.name} @ ₹{self.price}"


class Feedback(models.Model):
    email = models.CharField(max_length=255)
    rating = models.IntegerField()
    comments = models.TextField(blank=True, null=True)
    created_at = models.DateTimeField(auto_now_add=True)

    class Meta:
        db_table = 'feedbacks'
        ordering = ['-id']

    def __str__(self):
        return f"Feedback from {self.email} ({self.rating} stars)"

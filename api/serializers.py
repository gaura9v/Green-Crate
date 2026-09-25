from rest_framework import serializers
from .models import Product, Cart, CartItem, Order, OrderItem, Feedback


class ProductSerializer(serializers.ModelSerializer):
    stockQuantity = serializers.IntegerField(source='stock_quantity', required=False, default=0)

    class Meta:
        model = Product
        fields = ['id', 'name', 'description', 'price', 'category', 'stockQuantity']


class CartItemSerializer(serializers.ModelSerializer):
    product = ProductSerializer(read_only=True)

    class Meta:
        model = CartItem
        fields = ['id', 'product', 'quantity']


class CartSerializer(serializers.ModelSerializer):
    items = CartItemSerializer(many=True, read_only=True)

    class Meta:
        model = Cart
        fields = ['id', 'items']


class OrderItemSerializer(serializers.ModelSerializer):
    product = ProductSerializer(read_only=True)

    class Meta:
        model = OrderItem
        fields = ['id', 'product', 'quantity', 'price']


class OrderSerializer(serializers.ModelSerializer):
    items = OrderItemSerializer(many=True, read_only=True)
    totalAmount = serializers.FloatField(source='total_amount', required=False)

    class Meta:
        model = Order
        fields = ['id', 'items', 'totalAmount', 'status']


class FeedbackSerializer(serializers.ModelSerializer):
    createdAt = serializers.DateTimeField(source='created_at', format='%Y-%m-%dT%H:%M:%S', read_only=True)

    class Meta:
        model = Feedback
        fields = ['id', 'email', 'rating', 'comments', 'createdAt']

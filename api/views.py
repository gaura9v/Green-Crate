import os
from django.conf import settings
from django.core.mail import send_mail
from django.http import HttpResponse, FileResponse, Http404
from django.shortcuts import get_object_or_404
from rest_framework import status
from rest_framework.decorators import api_view
from rest_framework.response import Response

from .models import Product, Cart, CartItem, Order, OrderItem, Feedback
from .serializers import (
    ProductSerializer,
    CartSerializer,
    OrderSerializer,
    FeedbackSerializer,
)


# ==========================================
# PRODUCT CONTROLLER REPLICATION
# ==========================================

@api_view(['GET', 'POST'])
def product_list_create(request):
    """
    GET  /api/products - Returns list of all products
    POST /api/products - Creates a new product
    """
    if request.method == 'GET':
        products = Product.objects.all()
        serializer = ProductSerializer(products, many=True)
        return Response(serializer.data, status=status.HTTP_200_OK)

    elif request.method == 'POST':
        serializer = ProductSerializer(data=request.data)
        if serializer.is_valid():
            serializer.save()
            return Response(serializer.data, status=status.HTTP_200_OK)
        return Response(serializer.errors, status=status.HTTP_400_BAD_REQUEST)


@api_view(['GET', 'PUT', 'DELETE'])
def product_detail(request, pk):
    """
    GET    /api/products/{id} - Get product by id
    PUT    /api/products/{id} - Update product details
    DELETE /api/products/{id} - Delete product
    """
    product = get_object_or_404(Product, pk=pk)

    if request.method == 'GET':
        serializer = ProductSerializer(product)
        return Response(serializer.data, status=status.HTTP_200_OK)

    elif request.method == 'PUT':
        serializer = ProductSerializer(product, data=request.data, partial=True)
        if serializer.is_valid():
            serializer.save()
            return Response(serializer.data, status=status.HTTP_200_OK)
        return Response(serializer.errors, status=status.HTTP_400_BAD_REQUEST)

    elif request.method == 'DELETE':
        product.delete()
        return Response(status=status.HTTP_200_OK)


@api_view(['GET'])
def products_by_category(request, category):
    """
    GET /api/products/category/{category} - Get products matching category (case-insensitive)
    """
    products = Product.objects.filter(category__iexact=category)
    serializer = ProductSerializer(products, many=True)
    return Response(serializer.data, status=status.HTTP_200_OK)


# ==========================================
# CART CONTROLLER REPLICATION
# ==========================================

@api_view(['POST'])
def cart_create(request):
    """
    POST /api/cart - Create a new empty cart
    """
    cart = Cart.objects.create()
    serializer = CartSerializer(cart)
    return Response(serializer.data, status=status.HTTP_200_OK)


@api_view(['GET'])
def cart_detail(request, cart_id):
    """
    GET /api/cart/{cartId} - Get cart by id (creates new cart if not found, matching Java logic)
    """
    cart = Cart.objects.filter(id=cart_id).first()
    if not cart:
        cart = Cart.objects.create()
    serializer = CartSerializer(cart)
    return Response(serializer.data, status=status.HTTP_200_OK)


@api_view(['POST'])
def cart_add_product(request, cart_id):
    """
    POST /api/cart/{cartId}/add?productId={productId}&quantity={quantity}
    Adds a product to the cart, or increases the quantity if already present.
    """
    cart = Cart.objects.filter(id=cart_id).first()
    if not cart:
        cart = Cart.objects.create(id=cart_id)

    # Read from query params first (as app.js uses ?productId=...&quantity=1), fallback to body
    product_id = request.query_params.get('productId') or request.data.get('productId') or request.data.get('product_id')
    raw_quantity = request.query_params.get('quantity') or request.data.get('quantity') or 1

    if not product_id:
        return Response({'error': 'productId is required'}, status=status.HTTP_400_BAD_REQUEST)

    try:
        quantity = int(raw_quantity)
    except (ValueError, TypeError):
        quantity = 1

    product = get_object_or_404(Product, id=product_id)

    # Check if product is already in cart
    cart_item = CartItem.objects.filter(cart=cart, product=product).first()
    if cart_item:
        cart_item.quantity += quantity
        cart_item.save()
    else:
        CartItem.objects.create(cart=cart, product=product, quantity=quantity)

    cart.refresh_from_db()
    serializer = CartSerializer(cart)
    return Response(serializer.data, status=status.HTTP_200_OK)


@api_view(['DELETE'])
def cart_remove_item(request, cart_id, item_id):
    """
    DELETE /api/cart/{cartId}/item/{itemId} - Remove an item from the cart
    """
    cart = get_object_or_404(Cart, id=cart_id)
    CartItem.objects.filter(cart=cart, id=item_id).delete()
    cart.refresh_from_db()
    serializer = CartSerializer(cart)
    return Response(serializer.data, status=status.HTTP_200_OK)


# ==========================================
# ORDER CONTROLLER REPLICATION
# ==========================================

@api_view(['POST'])
def order_checkout(request, cart_id):
    """
    POST /api/orders/checkout/{cartId} - Checkout cart and create an order
    """
    cart = Cart.objects.filter(id=cart_id).first()
    if not cart or not cart.items.exists():
        return Response({'error': 'Cart is empty'}, status=status.HTTP_400_BAD_REQUEST)

    cart_items = list(cart.items.select_related('product').all())
    total_amount = sum(item.product.price * item.quantity for item in cart_items)

    order = Order.objects.create(total_amount=total_amount, status='PENDING')

    for item in cart_items:
        OrderItem.objects.create(
            order=order,
            product=item.product,
            quantity=item.quantity,
            price=item.product.price
        )

    # Clear cart items after creating order
    cart.items.all().delete()

    serializer = OrderSerializer(order)
    return Response(serializer.data, status=status.HTTP_200_OK)


@api_view(['GET'])
def order_list(request):
    """
    GET /api/orders - Get list of all orders
    """
    orders = Order.objects.all()
    serializer = OrderSerializer(orders, many=True)
    return Response(serializer.data, status=status.HTTP_200_OK)


@api_view(['GET', 'DELETE'])
def order_detail(request, order_id):
    """
    GET    /api/orders/{orderId} - Get order details
    DELETE /api/orders/{orderId} - Delete order
    """
    order = get_object_or_404(Order, id=order_id)

    if request.method == 'GET':
        serializer = OrderSerializer(order)
        return Response(serializer.data, status=status.HTTP_200_OK)

    elif request.method == 'DELETE':
        order.delete()
        return Response(status=status.HTTP_200_OK)


@api_view(['PUT'])
def order_update_status(request, order_id):
    """
    PUT /api/orders/{orderId}/status?status={status} - Update order status
    """
    order = get_object_or_404(Order, id=order_id)
    new_status = request.query_params.get('status') or request.data.get('status')
    if new_status:
        order.status = new_status
        order.save()
    serializer = OrderSerializer(order)
    return Response(serializer.data, status=status.HTTP_200_OK)


# ==========================================
# FEEDBACK CONTROLLER REPLICATION
# ==========================================

def send_feedback_notification_email(feedback):
    """
    Sends an email notification to the administrator when a customer submits feedback.
    Falls back gracefully if SMTP is not configured or on network error.
    """
    recipient = getattr(settings, 'FEEDBACK_NOTIFICATION_EMAIL', '') or getattr(settings, 'EMAIL_HOST_USER', '')
    if not recipient:
        recipient = 'admin@greencrate.com'

    stars = '★' * feedback.rating + '☆' * (5 - feedback.rating)
    subject = f"[GreenCrate] New Customer Feedback ({feedback.rating} Stars) from {feedback.email}"
    
    timestamp = feedback.created_at.strftime('%Y-%m-%d %H:%M:%S') if feedback.created_at else 'Just now'
    message = f"""Hello Admin,

You have received new customer feedback on GreenCrate!

==================================================
CUSTOMER DETAILS & RATING
==================================================
Customer Email : {feedback.email}
Rating         : {feedback.rating}/5 ({stars})
Submitted At   : {timestamp}

Feedback Comments:
{feedback.comments or 'No additional comments provided.'}

==================================================
VIEW ALL FEEDBACKS:
- Live Dashboard : http://localhost:8081/admin-feedback.html
- Django Admin   : http://localhost:8081/admin/api/feedback/
==================================================
"""
    try:
        send_mail(
            subject=subject,
            message=message,
            from_email=settings.DEFAULT_FROM_EMAIL,
            recipient_list=[recipient],
            fail_silently=False,
        )
        return True
    except Exception as e:
        print(f"[EMAIL NOTIFICATION WARNING] Could not send feedback email: {e}")
        return False


@api_view(['GET', 'POST'])
def feedback_list_create(request):
    """
    GET  /api/feedback - List all feedback
    POST /api/feedback - Submit new feedback and trigger email notification
    """
    if request.method == 'GET':
        feedbacks = Feedback.objects.all().order_by('id')
        serializer = FeedbackSerializer(feedbacks, many=True)
        return Response(serializer.data, status=status.HTTP_200_OK)

    elif request.method == 'POST':
        serializer = FeedbackSerializer(data=request.data)
        if serializer.is_valid():
            feedback = serializer.save()
            send_feedback_notification_email(feedback)
            return Response(serializer.data, status=status.HTTP_200_OK)
        return Response(serializer.errors, status=status.HTTP_400_BAD_REQUEST)


# ==========================================
# FRONTEND STATIC FILES SERVING
# ==========================================

def serve_frontend(request, path=''):
    """
    Serves static HTML, CSS, JS files directly from src/main/resources/static
    so that frontend behaves identically to Spring Boot's static resource hosting.
    """
    static_root = os.path.join(settings.BASE_DIR, 'src', 'main', 'resources', 'static')

    if not path or path == '/':
        target_path = os.path.join(static_root, 'home.html')
    else:
        clean_path = path.lstrip('/')
        target_path = os.path.join(static_root, clean_path)
        if not os.path.exists(target_path) and os.path.exists(target_path + '.html'):
            target_path = target_path + '.html'

    if os.path.exists(target_path) and os.path.isfile(target_path):
        content_type = 'text/plain'
        if target_path.endswith('.html'):
            content_type = 'text/html'
        elif target_path.endswith('.css'):
            content_type = 'text/css'
        elif target_path.endswith('.js'):
            content_type = 'application/javascript'
        elif target_path.endswith('.json'):
            content_type = 'application/json'
        elif target_path.endswith('.png'):
            content_type = 'image/png'
        elif target_path.endswith('.jpg') or target_path.endswith('.jpeg'):
            content_type = 'image/jpeg'
        elif target_path.endswith('.svg'):
            content_type = 'image/svg+xml'

        return FileResponse(open(target_path, 'rb'), content_type=content_type)

    raise Http404(f"File {path} not found")
